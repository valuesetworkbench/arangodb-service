package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import com.arangodb.VertexCursor;
import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.core.util.EncodingUtils;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.ComponentReference;
import edu.mayo.cts2.framework.model.core.EntityReferenceList;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader;
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDao;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionQueryService;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntitiesFromAssociationsQuery;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbValueSetDefinitionResolutionService extends AbstractArangoDbService implements ValueSetDefinitionResolutionService {

    private static final String RETURN_STATEMENT = "e";

    @Resource
    private ArangoDao arangoDao;

    @Resource
    private UrlConstructor urlConstructor;

    @Resource
    private ArangoDbValueSetDefinitionReadService readService;

    @Resource
    private ArangoDbEntityDescriptionQueryService entityQueryService;

    @Resource
    private ValueSetDefinitionAqlCompiler valueSetDefinitionAqlCompiler;

    public static class UriResult {
        private String uri;
        private CodeSystemVersionReference codeSystemVersionReference;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public CodeSystemVersionReference getCodeSystemVersionReference() {
            return codeSystemVersionReference;
        }

        public void setCodeSystemVersionReference(CodeSystemVersionReference codeSystemVersionReference) {
            this.codeSystemVersionReference = codeSystemVersionReference;
        }
    }

    @Override
    public ResolvedValueSetResult<URIAndEntityName> resolveDefinition(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, NameOrURI tag, SortCriteria sortCriteria, ResolvedReadContext readContext, Page page) {
        List<URIAndEntityName> returnList;

        LocalIdValueSetDefinition localIdValueSetDefinition = this.readService.read(definitionId, readContext);
        if(localIdValueSetDefinition == null) {
            return null;
        }

        ValueSetDefinition valueSetDefinition = localIdValueSetDefinition.getResource();

        if(this.isOnlySpecificEntities(valueSetDefinition)) {
            List<URIAndEntityName> specificEntityList = Lists.newArrayList();

            Map<String,URIAndEntityName> needsHrefMap = Maps.newHashMap();

            for (ValueSetDefinitionEntry entry : valueSetDefinition.getEntry()) {
                SpecificEntityList foundSpecificEntityList = entry.getEntityList();

                if (foundSpecificEntityList != null) {
                    for (URIAndEntityName entity : foundSpecificEntityList.getReferencedEntity()) {
                        specificEntityList.add(entity);
                        if(entity.getHref() == null) {
                            needsHrefMap.put(entity.getUri(), entity);
                        }
                    }
                }
            }

            Map<String, Object> params = Maps.newHashMap();

            int i = 0;
            for(String uri : needsHrefMap.keySet()) {
                String var = "entity" + Integer.toString(i++);
                params.put(var, uri);
            }

            Set<String> varNames = Sets.newHashSet();
            for(String key : params.keySet()) {
                varNames.add('@' + key);
            }
            String inClause = "[" + StringUtils.join(varNames, ',') + "]";

            String aql = "for e in EntityDescription filter e.namedEntity.about IN " + inClause + " return {uri: e.namedEntity.about, codeSystemVersionReference: e.namedEntity.describingCodeSystemVersion}";

            for(VertexEntity<UriResult> result : this.arangoDao.query(aql, params, UriResult.class)) {
                UriResult uriResult = result.getEntity();
                URIAndEntityName entityName = needsHrefMap.get(uriResult.getUri());
                entityName.setHref(
                        this.urlConstructor.createEntityUrl(
                                uriResult.getCodeSystemVersionReference().getCodeSystem().getContent(),
                                uriResult.getCodeSystemVersionReference().getVersion().getContent(),
                                EncodingUtils.encodeScopedEntityName(entityName)));
            }

            returnList = specificEntityList;
        } else {
            List<URIAndEntityName> associatedEntityList = Lists.newArrayList();

            AqlDirectoryBuilder.AqlState queryState = this.valueSetDefinitionAqlCompiler.toAql(valueSetDefinition, codeSystemVersions);

            Map<String,Object> parameters = Maps.newHashMap();

            String limitOffset = AqlUtils.getLimitOffset(page, parameters);

            AqlDirectoryBuilder.AqlState returnState = new AqlDirectoryBuilder.RawAql(
                    "for r in results " + limitOffset + "\n" +
                            "   for e in EntityDescription \n" +
                            "       filter r == e._id\n" +
                            "       RETURN " + RETURN_STATEMENT, parameters);

            AqlDirectoryBuilder.AqlState aqlState = new AqlDirectoryBuilder.CompositeAql(queryState, returnState);

            VertexCursor<EntityDescription> result = this.arangoDao.query(aqlState.getAql(), aqlState.getParameters(), EntityDescription.class);
            for (EntityDescription entityDescription : result.asEntityList()) {
                URIAndEntityName uriAndEntityName = new URIAndEntityName();

                String codeSystemName = entityDescription.getNamedEntity().getDescribingCodeSystemVersion().getCodeSystem().getContent();
                String codeSystemVersionName = entityDescription.getNamedEntity().getDescribingCodeSystemVersion().getVersion().getContent();

                uriAndEntityName.setName(entityDescription.getNamedEntity().getEntityID().getName());
                uriAndEntityName.setNamespace(entityDescription.getNamedEntity().getEntityID().getNamespace());
                uriAndEntityName.setUri(entityDescription.getNamedEntity().getAbout());
                uriAndEntityName.setDesignation(entityDescription.getNamedEntity().getDesignation(0).getValue().getContent());
                uriAndEntityName.setHref(this.urlConstructor.createEntityUrl(
                        codeSystemName,
                        codeSystemVersionName,
                        entityDescription.getNamedEntity().getEntityID()));

                associatedEntityList.add(uriAndEntityName);
            }

            returnList = associatedEntityList;
        }

        return new ResolvedValueSetResult<URIAndEntityName>(new ResolvedValueSetHeader(), returnList, true);
    }

    private boolean isOnlySpecificEntities(ValueSetDefinition valueSetDefinition) {
        for(ValueSetDefinitionEntry entry : valueSetDefinition.getEntry()) {
            SpecificEntityList foundSpecificEntityList = entry.getEntityList();

            if(foundSpecificEntityList == null) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAssociationEntry(ValueSetDefinition valueSetDefinition) {
        for(ValueSetDefinitionEntry entry : valueSetDefinition.getEntry()) {
            if(entry.getAssociatedEntities() != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public EntityReferenceList contains(ValueSetDefinitionReadId definitionId, Set<EntityNameOrURI> entities) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResolvedValueSetResult<EntityDirectoryEntry> resolveDefinitionAsEntityDirectory(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, NameOrURI tag, final ResolvedValueSetResolutionEntityQuery query, SortCriteria sortCriteria, ResolvedReadContext readContext, Page page) {
        final LocalIdValueSetDefinition localIdValueSetDefinition = this.readService.read(definitionId, readContext);
        if(localIdValueSetDefinition == null) {
            return null;
        }

        // special case... can only handle this so far...
        if(localIdValueSetDefinition.getResource().getEntry().length == 1 &&
                localIdValueSetDefinition.getResource().getEntry()[0].getCompleteCodeSystem() != null) {

            final NameAndMeaningReference csv = localIdValueSetDefinition.getResource().getEntry()[0].getCompleteCodeSystem().getCodeSystemVersion().getVersion();
            EntityDescriptionQuery entityQuery = new EntityDescriptionQuery() {
                @Override
                public EntitiesFromAssociationsQuery getEntitiesFromAssociationsQuery() {
                    return null;
                }

                @Override
                public EntityDescriptionQueryServiceRestrictions getRestrictions() {
                    EntityDescriptionQueryServiceRestrictions restrictions = new EntityDescriptionQueryServiceRestrictions();
                    restrictions.setCodeSystemVersions(Sets.newHashSet(ModelUtils.nameOrUriFromName(csv.getContent())));

                    return restrictions;
                }

                @Override
                public Query getQuery() {
                    return null;
                }

                @Override
                public Set<ResolvedFilter> getFilterComponent() {
                    return query.getFilterComponent();
                }

                @Override
                public ResolvedReadContext getReadContext() {
                    return null;
                }
            };

            DirectoryResult<EntityDirectoryEntry> result = entityQueryService.getResourceSummaries(entityQuery, sortCriteria, page);

            return new ResolvedValueSetResult<EntityDirectoryEntry>(new ResolvedValueSetHeader(), result.getEntries(), result.isAtEnd());
        } else {

            AqlDirectoryBuilder.AqlState state = valueSetDefinitionAqlCompiler.toAql(localIdValueSetDefinition.getResource(), null);

            Map<String, Object> parameters = Maps.newHashMap();

            String limitOffset = AqlUtils.getLimitOffset(page, parameters);

            AqlDirectoryBuilder.AqlState returnState = new AqlDirectoryBuilder.RawAql(
                    "for r in results " + limitOffset + "\n" +
                            "   for e in EntityDescription \n" +
                            "       filter r == e._id\n" +
                            "       RETURN {uri: e.namedEntity.about, csv: e.namedEntity.describingCodeSystemVersion.version.content}", parameters);

            AqlDirectoryBuilder.AqlState aqlState = new AqlDirectoryBuilder.CompositeAql(state, returnState);

            final Set<String> csvUris = Sets.newHashSet();
            final Set<NameOrURI> csvs = Sets.newHashSet();
            final Set<String> entityUris = Sets.newHashSet();
            final Set<EntityNameOrURI> entities = Sets.newHashSet();

            VertexCursor<Uri> uris = this.arangoDao.query(aqlState.getAql(), aqlState.getParameters(), Uri.class);

            for(Uri uri : uris.asEntityList()) {
                csvUris.add(uri.getCsv());
                entityUris.add(uri.getUri());
            }

            for(String csvUri : csvUris) {
                csvs.add(ModelUtils.nameOrUriFromName(csvUri));
            }

            for(String entityUri : entityUris) {
                entities.add(ModelUtils.entityNameOrUriFromUri(entityUri));
            }

            EntityDescriptionQuery entityQuery = new EntityDescriptionQuery() {
                @Override
                public EntitiesFromAssociationsQuery getEntitiesFromAssociationsQuery() {
                    return null;
                }

                @Override
                public EntityDescriptionQueryServiceRestrictions getRestrictions() {
                    EntityDescriptionQueryServiceRestrictions restrictions = new EntityDescriptionQueryServiceRestrictions();
                    restrictions.setCodeSystemVersions(csvs);
                    restrictions.setEntities(entities);

                    return restrictions;
                }

                @Override
                public Query getQuery() {
                    return null;
                }

                @Override
                public Set<ResolvedFilter> getFilterComponent() {
                    if(query != null) {
                        return query.getFilterComponent();
                    } else {
                        return null;
                    }
                }

                @Override
                public ResolvedReadContext getReadContext() {
                    return null;
                }
            };

            DirectoryResult<EntityDirectoryEntry> result = entityQueryService.getResourceSummaries(entityQuery, sortCriteria, page);

            return new ResolvedValueSetResult<EntityDirectoryEntry>(new ResolvedValueSetHeader(), result.getEntries(), result.isAtEnd());
        }
    }

    public static class Uri {
        private String uri;
        private String csv;

        public String getCsv() {
            return csv;
        }

        public void setCsv(String csv) {
            this.csv = csv;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    @Override
    public ResolvedValueSet resolveDefinitionAsCompleteSet(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, NameOrURI tag, SortCriteria sortCriteria, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
        Set<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();

        returnSet.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
        returnSet.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference());
        returnSet.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference());

        return returnSet;
    }

    @Override
    public Set<? extends ComponentReference> getSupportedSearchReferences() {
        Set<ComponentReference> returnSet = new HashSet<ComponentReference>();

        returnSet.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference());
        returnSet.add(StandardModelAttributeReference.ABOUT.getComponentReference());
        returnSet.add(StandardModelAttributeReference.RESOURCE_NAME.getComponentReference());
        returnSet.add(StandardModelAttributeReference.DESIGNATION.getComponentReference());
        returnSet.add(StandardModelAttributeReference.KEYWORD.getComponentReference());

        return returnSet;
    }

    @Override
    public Set<? extends ComponentReference> getSupportedSortReferences() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<PredicateReference> getKnownProperties() {
        throw new UnsupportedOperationException("not implemented");
    }

}
