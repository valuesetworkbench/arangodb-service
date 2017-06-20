package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionListEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbLocalIdQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticsearchDao;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticsearchUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.TransformUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQuery;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQueryService;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbValueSetDefinitionQueryService extends AbstractArangoDbLocalIdQueryService<LocalIdValueSetDefinition, ValueSetDefinition, ValueSetDefinitionListEntry, ValueSetDefinitionDirectoryEntry, ValueSetDefinitionQuery> implements ValueSetDefinitionQueryService {

    @Resource
    private ElasticsearchDao elasticsearchDao;

    @Resource
    private ValueSetDefinitionStorageInfo valueSetDefinitionStorageInfo;

    private Set<String> indexSearchFields = Sets.newHashSet(
            "resourceSynopsis",
            "formalName",
            "valueSetAnalyzed",
            "officialResourceVersionId"
    );

    protected ValueSetDefinitionDirectoryEntry toDirectoryEntry(VertexEntity<LocalIdValueSetDefinition> node) {
        ValueSetDefinition fullEntity = node.getEntity().getResource();

        ValueSetDefinitionDirectoryEntry directoryEntry = new ValueSetDefinitionDirectoryEntry();

        directoryEntry = TransformUtils.baseTransformResourceVersion(directoryEntry, fullEntity);

        directoryEntry.setDefinedValueSet(fullEntity.getDefinedValueSet());
        directoryEntry.setVersionTag(fullEntity.getVersionTag());

        directoryEntry.setHref(ValueSetDefinitionUtils.getHref(node.getEntity(), this.getUrlConstructor()));

        return directoryEntry;
    }


    @Override
    protected <T> AqlDirectoryBuilder<LocalIdValueSetDefinition, T> addState(ValueSetDefinitionQuery query, AqlDirectoryBuilder<LocalIdValueSetDefinition, T> builder, Page page) {
        if(query != null && CollectionUtils.isEmpty(query.getFilterComponent()) && query.getRestrictions() != null && query.getRestrictions().getValueSet() != null) {
            Map<String,Object> params = Maps.newHashMap();
            params.put("valueSet", query.getRestrictions().getValueSet().getName());

            builder.addState(new AqlDirectoryBuilder.SimpleAqlClause(this.getCollection(), new AqlDirectoryBuilder.AqlFilter("x.definedValueSet.content == @valueSet", params)));
        }

        return super.addState(query, builder, page);
    }

    @Override
    protected List<String> getDefaultSortFields() {
        return Arrays.asList("definedValueSet.content", "changeableElementGroup.changeDescription.changeDate");
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filters, ValueSetDefinitionQuery query, Page page) {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

        for(ResolvedFilter filter : filters) {
            queryBuilder = ElasticsearchUtils.buildQueryFromFilter(queryBuilder, filter, this.indexSearchFields);
        }

        if(query != null && query.getRestrictions() != null && query.getRestrictions().getValueSet() != null) {
            BoolQueryBuilder overall = QueryBuilders.boolQuery();
            overall.must(queryBuilder);
            overall.must(QueryBuilders.termQuery("valueSet", query.getRestrictions().getValueSet().getName()));

            queryBuilder = overall;
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).
                withPageable(new PageRequest(page.getStart(), page.getMaxToReturn())).
                withTypes(this.getCollection()).build();

        return new AqlDirectoryBuilder.LuceneQuery(elasticsearchDao, searchQuery, ArangoDbServiceConstants.VALUE_SET_DEFINITION_COLLECTION, IndexedValueSetDefinition.class);
    }

    @Override
    protected Transformer<LocalIdValueSetDefinition, ValueSetDefinitionDirectoryEntry> getSummarizer() {
        return new Transformer<LocalIdValueSetDefinition, ValueSetDefinitionDirectoryEntry>() {
            @Override
            public ValueSetDefinitionDirectoryEntry toSummary(VertexEntity<LocalIdValueSetDefinition> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<LocalIdValueSetDefinition, ValueSetDefinitionListEntry> getLister() {
        return new Transformer<LocalIdValueSetDefinition, ValueSetDefinitionListEntry>() {
            @Override
            public ValueSetDefinitionListEntry toSummary(VertexEntity<LocalIdValueSetDefinition> fullResource) {
                ValueSetDefinitionListEntry entry = new ValueSetDefinitionListEntry();
                entry.addEntry(fullResource.getEntity().getResource());

                return entry;
            }
        };
    }

    @Override
    public Class<LocalIdValueSetDefinition> getStorageClass() {
        return LocalIdValueSetDefinition.class;
    }

    @Override
    public StorageInfo<LocalIdValueSetDefinition> getStorageInfo() {
        return this.valueSetDefinitionStorageInfo;
    }

}
