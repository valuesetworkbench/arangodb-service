package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.core.types.AssociationDirection;
import edu.mayo.cts2.framework.model.core.types.SetOperator;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valuesetdefinition.AssociatedEntitiesReference;
import edu.mayo.cts2.framework.model.valuesetdefinition.CompleteCodeSystemReference;
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ValueSetDefinitionAqlCompiler {


    @Autowired
    private ArangoDbEntityDescriptionReadService entityDocumentReader;

    private NameOrURI getCodeSystemVersion(AssociatedEntitiesReference reference, Set<NameOrURI> codeSystemVersions) {
        if(CollectionUtils.isNotEmpty(codeSystemVersions)) {
            if(codeSystemVersions.size() > 1) {
                throw new RuntimeException("Cannot specify more than 1 CodeSystemVersion on a ValueSetDefinition resolution.");
            }

            return codeSystemVersions.iterator().next();
        } else {
            if(reference.getCodeSystemVersion() == null ||
                    reference.getCodeSystemVersion().getVersion() == null ||
                    StringUtils.isBlank(reference.getCodeSystemVersion().getVersion().getContent())) {
                throw new RuntimeException("An AssociatedEntitiesReference must specify a CodeSystemVersion reference either in the AssociatedEntitiesReference itself or as part of the Resolution parameters.");
            }

            return ModelUtils.nameOrUriFromEither(reference.getCodeSystemVersion().getVersion().getContent());
        }
    }

    public AqlDirectoryBuilder.AqlState toAql(ValueSetDefinition valueSetDefinition, Set<NameOrURI> codeSystemVersions) {

        AqlDirectoryBuilder.CompositeAql state = new AqlDirectoryBuilder.CompositeAql();
        SetOperatorBuilder setOperatorBuilder = new SetOperatorBuilder();

        ValueSetDefinitionEntry[] entries = valueSetDefinition.getEntry();
        for(int i=0;i<entries.length;i++) {
            ValueSetDefinitionEntry entry = entries[i];

            AssociatedEntitiesReference associatedEntitiesReference = entry.getAssociatedEntities();
            if(associatedEntitiesReference != null) {

                URIAndEntityName root = associatedEntitiesReference.getReferencedEntity();

                NameOrURI codeSystemVersion = this.getCodeSystemVersion(associatedEntitiesReference, codeSystemVersions);

                String handle = this.entityDocumentReader.readDocument(
                        new EntityDescriptionReadId(root.getUri(), codeSystemVersion), null).getDocumentHandle();

                String direction = associatedEntitiesReference.getDirection() == null || associatedEntitiesReference.getDirection().equals(AssociationDirection.SOURCE_TO_TARGET) ? "outbound" : "inbound";

                String aql =
                                "LET children" + i + " = (for e in GRAPH_NEIGHBORS(\"Association\", @root" + i + ", \n" +
                                "        {maxDepth: @depth" + i + ", direction: @direction" + i + ", includeData:false, edgeExamples : [{isHierarchy: true}]}) return e\n" +
                                ")\n";

                Map<String,Object> params = Maps.newHashMap();
                params.put("depth" + i, -1);
                params.put("direction" + i, direction);
                params.put("root" + i, handle);

                AqlDirectoryBuilder.AqlState clause = new AqlDirectoryBuilder.RawAql(aql, params);

                state.add(clause);

                setOperatorBuilder.add(new SetOperatorBuilderTuple("children" + i, entry.getOperator() != null ? entry.getOperator() : SetOperator.UNION));
            }

            SpecificEntityList specificEntityList = entry.getEntityList();
            if(specificEntityList != null) {
                URIAndEntityName[] uriAndEntityNames = specificEntityList.getReferencedEntity();

                Map<String, Object> params = Maps.newHashMap();

                for(int j=0;j<uriAndEntityNames.length;j++) {
                    URIAndEntityName entity = uriAndEntityNames[j];

                    String var = "entity" + Integer.toString(i) + Integer.toString(j);
                    params.put(var, entity.getUri());
                }

                Set<String> varNames = Sets.newHashSet();
                for(String key : params.keySet()) {
                    varNames.add('@' + key);
                }
                String inClause = "[" + StringUtils.join(varNames, ',') + "]";

                String aql = "LET entity" + i + " = (for e in EntityDescription filter e.namedEntity.about IN " + inClause + " return e._id)\n";

                AqlDirectoryBuilder.AqlState clause = new AqlDirectoryBuilder.RawAql(aql, params);

                state.add(clause);

                setOperatorBuilder.add(new SetOperatorBuilderTuple("entity" + i, entry.getOperator() != null ? entry.getOperator() : SetOperator.UNION));
            }

            CompleteCodeSystemReference completeCodeSystemReference = entry.getCompleteCodeSystem();
            if(completeCodeSystemReference != null) {
                String limitOffsetOptimization = "";
                if(entries.length == 1) {
                    limitOffsetOptimization = AqlUtils.getLimitOffsetAql();
                }

                Map<String, Object> params = Maps.newHashMap();
                params.put("codeSystemVersion", completeCodeSystemReference.getCodeSystemVersion().getVersion().getContent());

                String aql = "LET completeCodeSystem" + i + " = (for e in EntityDescription filter e.namedEntity.describingCodeSystemVersion.version.content == @codeSystemVersion " + limitOffsetOptimization + " return e._id)\n";

                AqlDirectoryBuilder.AqlState clause = new AqlDirectoryBuilder.RawAql(aql, params);

                state.add(clause);

                setOperatorBuilder.add(new SetOperatorBuilderTuple("completeCodeSystem" + i, entry.getOperator() != null ? entry.getOperator() : SetOperator.UNION));
            }
        }

        state.add(new AqlDirectoryBuilder.RawAql(setOperatorBuilder.toAql()));

        return state;
    }

    private class SetOperatorBuilderTuple {
        private String var;
        private SetOperator setOperator;

        public SetOperatorBuilderTuple(String var, SetOperator setOperator) {
            this.var = var;
            this.setOperator = setOperator;
        }
    }

    private class SetOperatorBuilder {

        private List<SetOperatorBuilderTuple> vars = Lists.newArrayList();

        private String toAql() {
            List<SetOperatorBuilderTuple> clonedVars = Lists.newArrayList(this.vars);

            SetOperatorBuilderTuple first = clonedVars.remove(0);

            String aql;
            if(vars.size() == 1) {
                aql = first.var;
            } else {
                aql = first.var;

                for (SetOperatorBuilderTuple state : clonedVars) {
                    aql = toAqlSetOp(state.setOperator) + "(" + state.var + ", " + aql + ")";
                }
            }

            return
                "LET results = " + aql + "\n";

        }

        private void add(SetOperatorBuilderTuple setOperatorBuilderTuple) {
            this.vars.add(setOperatorBuilderTuple);
        }

        private String toAqlSetOp(SetOperator setOperator) {
            switch (setOperator) {
                case INTERSECT: return "INTERSECTION";
                case UNION: return "UNION_DISTINCT";
                case SUBTRACT: return "MINUS";
                default: throw new IllegalStateException();
            }
        }
    }

}
