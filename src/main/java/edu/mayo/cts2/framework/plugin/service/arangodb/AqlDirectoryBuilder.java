package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.VertexCursor;
import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.filter.directory.AbstractStateBuildingDirectoryBuilder;
import edu.mayo.cts2.framework.filter.match.StateAdjustingComponentReference;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AqlDirectoryBuilder<F,T> extends AbstractStateBuildingDirectoryBuilder<List<AqlDirectoryBuilder.AqlState>, T> {

    public interface AqlState {
        String getAql();

        Map<String,Object> getParameters();
    }

    public static class AqlFilter {

        private String state;

        private Map<String, Object> parameters;

        public AqlFilter(String state, Map<String, Object> parameters) {
            this.state = state;
            this.parameters = parameters;
        }

        private String getState() {
            return state;
        }

        private Map<String, Object> getParameters() {
            return parameters;
        }
    }

    public static class CompositeAql implements AqlState {

        private List<AqlState> aqlStates;

        public CompositeAql() {
            this.aqlStates = new ArrayList<AqlState>();
        }

        public CompositeAql(AqlState... aqlStates) {
            this.aqlStates = Arrays.asList(aqlStates);
        }

        public void add(AqlState state) {
            this.aqlStates.add(state);
        }

        @Override
        public String getAql() {
            StringBuilder sb = new StringBuilder();
            for(AqlState state : this.aqlStates) {
                sb.append(state.getAql());
                sb.append("\n");
            }

            return sb.toString();
        }

        @Override
        public Map<String, Object> getParameters() {
            Map<String, Object> returnMap = Maps.newHashMap();
            for(AqlState state : this.aqlStates) {
                returnMap.putAll(state.getParameters());
            }

            return returnMap;
        }
    }

    public static class LuceneQuery implements AqlState {

        private String aql;
        private Map<String,Object> parameters;

        public LuceneQuery(ElasticsearchDao elasticsearchDao, SearchQuery searchQuery, String collection, Class<? extends IndexDocument<?>> indexClazz) {
            AqlDirectoryBuilder.CompositeAql state = new AqlDirectoryBuilder.CompositeAql();

            int index = 0;
            FlattenBuilder flattenBuilder = new FlattenBuilder();

            for(IndexDocument indexedEntityDescription : elasticsearchDao.query(searchQuery, indexClazz)) {
                index = index + 1;

                String collectionVarName = "ecollection" + index;
                String entityIdVarName = "eid" + index;

                String aql = "for returnDoc in " + collection + "\n" +
                        "       filter returnDoc._id == @" + entityIdVarName +
                        "           return returnDoc";

                Map<String,Object> params = Maps.newHashMap();
                params.put(entityIdVarName, indexedEntityDescription.getArangoDbId());

                state.add(new AqlDirectoryBuilder.NamedAqlClauseDecorator(collectionVarName, new AqlDirectoryBuilder.RawAql(aql, params)));

                flattenBuilder.add(collectionVarName);
            }

            state.add(new AqlDirectoryBuilder.RawAql(flattenBuilder.toAql()));

            this.aql = state.getAql();
            this.parameters = state.getParameters();
        }

        @Override
        public String getAql() {
            return this.aql;
        }

        @Override
        public Map<String, Object> getParameters() {
            return this.parameters;
        }
    }

    private static class FlattenBuilder {

        private List<String> vars = Lists.newArrayList();

        private String toAql() {

            if(vars.size() == 1) {
                return "for e in " + this.vars.get(0) + " return e";
            } else {
                return "for e in flatten([" + StringUtils.join(this.vars, ",")+ "]) return e";
            }
        }

        private void add(String var) {
            this.vars.add(var);
        }
    }

    public static class RawAql implements AqlState {

        private String aql;
        private Map<String,Object> parameters;

        public RawAql(String aql) {
            this.aql = aql;
            this.parameters = new HashMap<String,Object>();
        }

        public RawAql(String aql, Map<String,Object> parameters) {
            this.aql = aql;
            this.parameters = parameters;
        }

        @Override
        public String getAql() {
            return this.aql;
        }

        @Override
        public Map<String, Object> getParameters() {
            return this.parameters;
        }
    }

    public static class NamedAqlClauseDecorator implements AqlState {

        private String variableName;
        private AqlState state;

        public NamedAqlClauseDecorator(String variableName, AqlState state) {
            this.variableName = variableName;
            this.state = state;
        }


        @Override
        public String getAql() {
            return "LET " + this.variableName + " = (" + this.state.getAql() + ")";
        }

        @Override
        public Map<String, Object> getParameters() {
            return this.state.getParameters();
        }
    }

    public static class SimpleAqlClause implements AqlState {

        private List<String> sortFields;
        private List<AqlFilter> state;
        private String collection;
        private Integer offset;
        private Integer limit;

        public SimpleAqlClause(String collection, AqlFilter... state) {
            this(collection, null, null, null, Arrays.asList(state));
        }

        public SimpleAqlClause(String collection, Integer limit, Integer offset, AqlFilter... state) {
            this(collection, limit, offset, null, Arrays.asList(state));
        }

        public SimpleAqlClause(String collection, Integer limit, Integer offset, List<String> sortFields, List<AqlFilter> state) {
            this.state = state;
            this.limit = limit;
            this.offset = offset;
            this.collection = collection;
            this.sortFields = sortFields;
        }

        @Override
        public String getAql() {
            String stateString;

            if(CollectionUtils.isNotEmpty(this.state)) {
                List<String> aql = Lists.newArrayList();

                for(AqlFilter aqlFilter : this.state) {
                    aql.add(aqlFilter.getState());
                }

                stateString = "filter " + StringUtils.join(aql, " and ");
            } else {
                stateString = "";
            }

            String limitOffsetString;
            if(this.shouldLimit(this.limit, this.offset)) {
                limitOffsetString = AqlUtils.getLimitOffsetAql();
            } else {
                limitOffsetString = "";
            }

            String sort = "";
            if(CollectionUtils.isNotEmpty(this.sortFields)) {
                List<String> fields = new ArrayList<String>();
                for (int i = 0; i < this.sortFields.size(); i++) {
                    fields.add("x." + this.sortFields.get(i));
                }

                sort = "sort " + StringUtils.join(fields, ",") + " ASC";
            }

            String aql = "for x in " + this.collection + " " + stateString + " " + sort + " " + limitOffsetString + " return x";

            return aql;
        }

        private boolean shouldLimit(Integer limit, Integer offset) {
            return limit != null && offset != null && limit >= 0;
        }

        @Override
        public Map<String, Object> getParameters() {
            Map<String,Object> parameters = Maps.newHashMap();

            if(CollectionUtils.isNotEmpty(this.state)) {
                for(AqlFilter aqlFilter : this.state) {
                    parameters.putAll(aqlFilter.getParameters());
                }
            }

            if(this.shouldLimit(this.limit, this.offset)) {
                AqlUtils.addLimitOffsetParams(this.limit, this.offset, parameters);
            }

            return parameters;
        }
    }

    public AqlDirectoryBuilder(
            ArangoDao arangoDao,
            String collection,
            String resourcePath,
            String uriPath,
            Transformer<F,T> transformer,
            Class<F> fullClass,
            Set<MatchAlgorithmReference> matchAlgorithmReferences,
            Set<StateAdjustingComponentReference<List<AqlState>>> stateAdjustingPropertyReferences,
            List<String> sortFields) {
        super(new ArrayList<AqlState>(), new AqlCallback<F, T>(arangoDao, collection, uriPath, resourcePath, fullClass, transformer, sortFields), matchAlgorithmReferences, stateAdjustingPropertyReferences);
    }

    public void addState(AqlState state) {
        this.getState().add(state);
    }

    private static class AqlCallback<F,S> implements Callback<List<AqlState>, S> {

        private ArangoDao arangoDao;
        private String collection;
        private String uriPath;
        private String resourcePath;
        private Class<F> fullClass;
        private Transformer<F,S> transformer;
        private List<String> sortFields;

        private AqlCallback(ArangoDao arangoDao, String collection, String uriPath, String resourcePath, Class<F> fullClass, Transformer<F,S> transformer, List<String> sortFields) {
            this.arangoDao = arangoDao;
            this.collection = collection;
            this.resourcePath = resourcePath;
            this.uriPath = uriPath;
            this.fullClass = fullClass;
            this.transformer = transformer;
            this.sortFields = sortFields;
        }

        private AqlQuery buildAqlQuery(List<AqlState> state, Integer start, Integer maxResults) {
            String aql = "";
            Map<String,Object> parameters = Maps.newHashMap();

            if(CollectionUtils.isEmpty(state)) {
                List<AqlState> defaultState = Lists.newArrayList();
                defaultState.add(new SimpleAqlClause(this.collection, start, maxResults, this.sortFields, null));

                state = defaultState;
            }

            for(AqlState aqlState : state) {
                aql += "\n" + aqlState.getAql();
                parameters.putAll(aqlState.getParameters());
            }

            aql = "LET results = (\n " + aql + " )\n for result in results return result";

            return new AqlQuery(aql, parameters);
        }

        @Override
        public DirectoryResult<S> execute(List<AqlState> state, int start, int maxResults) {
            AqlQuery query = this.buildAqlQuery(state, maxResults, start);

            String aql = query.getAql();
            Map<String,Object> parameters = query.getParameters();

            VertexCursor<F> cursor = this.arangoDao.query(aql, parameters, this.fullClass);

            List<S> theList = new ArrayList<>();

            for(VertexEntity<F> result : cursor.asList()) {
                theList.add(this.transformer.toSummary(result));
            }

            return new DirectoryResult<S>(theList, true);
        }

        @Override
        public int executeCount(List<AqlState> state) {
            AqlQuery query = this.buildAqlQuery(state, null, null);

            String aql = query.getAql();
            aql = "LET count = (\n " + aql + " )\n return {count: LENGTH(count)}";
            Map<String,Object> parameters = query.getParameters();

            return ((Double) this.arangoDao.query(aql, parameters, Map.class).iterator().next().getEntity().get("count")).intValue();
        }

    }

}
