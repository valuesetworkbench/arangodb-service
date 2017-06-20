package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription;

import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
import edu.mayo.cts2.framework.model.core.EntityReferenceList;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.entity.Designation;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDescriptionBase;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.entity.types.DesignationRole;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURIList;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticsearchDao;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQueryService;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbEntityDescriptionQueryService extends AbstractArangoDbDefaultQueryService<EntityDescription, EntityListEntry, EntityDirectoryEntry, EntityDescriptionQuery> implements EntityDescriptionQueryService {

    @Resource
    private ElasticsearchDao elasticsearchDao;

    @Resource
    private EntityDescriptionStorageInfo entityDescriptionStorageInfo;

    protected EntityDirectoryEntry toDirectoryEntry(VertexEntity<EntityDescription> node) {
        EntityDescriptionBase baseEntityDescription = node.getEntity().getNamedEntity();

        EntityDirectoryEntry entry = new EntityDirectoryEntry();
        entry.setAbout(baseEntityDescription.getAbout());
        entry.setName(baseEntityDescription.getEntityID());

        DescriptionInCodeSystem descriptionInCodeSystem = this.getDescriptionInCodeSystemVersion(baseEntityDescription);

        entry.addKnownEntityDescription(descriptionInCodeSystem);

        entry.setHref(descriptionInCodeSystem.getHref());

        return entry;
    }

    private Designation getPreferredDesignation(EntityDescriptionBase entity){
        if(entity.getDesignationCount() == 1){
            return entity.getDesignation(0);
        }

        for(Designation designation : entity.getDesignation()){
            if(designation.getDesignationRole().equals(DesignationRole.PREFERRED)){
                return designation;
            }
        }

        // just grab the first one if nothing is PREFERRED
        if(entity.getDesignationCount() > 0) {
            return entity.getDesignation(0);
        }

        return null;
    }

    private DescriptionInCodeSystem getDescriptionInCodeSystemVersion(
            EntityDescriptionBase baseEntityDescription) {
        String codeSystemName =
                baseEntityDescription.getDescribingCodeSystemVersion().getCodeSystem().getContent();

        String codeSystemURI = baseEntityDescription.getDescribingCodeSystemVersion().getCodeSystem().getUri();

        String codeSystemVersionName =
                baseEntityDescription.getDescribingCodeSystemVersion().getVersion().getContent();

        String codeSystemVersionURI = baseEntityDescription.getDescribingCodeSystemVersion().getVersion().getUri();

        Designation designation = this.getPreferredDesignation(baseEntityDescription);

        DescriptionInCodeSystem description = new DescriptionInCodeSystem();

        if(designation != null && designation.getValue() != null){
            description.setDesignation(designation.getValue().getContent());
        }

        description.setDescribingCodeSystemVersion(
                this.buildCodeSystemVersionReference(codeSystemName, codeSystemURI, codeSystemVersionName, codeSystemVersionURI));

        description.setHref(this.getUrlConstructor().createEntityUrl(codeSystemName, codeSystemVersionName, baseEntityDescription.getEntityID()));

        return description;
    }

    protected CodeSystemVersionReference buildCodeSystemVersionReference(String codeSystemName, String codeSystemURI, String codeSystemVersionName,
                                                                         String codeSystemVersionURI){
        CodeSystemVersionReference ref = new CodeSystemVersionReference();

        ref.setCodeSystem(this.buildCodeSystemReference(codeSystemName, codeSystemURI));

        NameAndMeaningReference version = new NameAndMeaningReference();
        version.setContent(codeSystemVersionName);
        version.setUri(codeSystemVersionURI);
        version.setHref(this.getUrlConstructor().createCodeSystemVersionUrl(codeSystemName, codeSystemVersionName));

        ref.setVersion(version);

        return ref;
    }

    protected CodeSystemReference buildCodeSystemReference(String codeSystemName, String codeSystemURI){
        CodeSystemReference codeSystemReference = new CodeSystemReference();
        String codeSystemPath = this.getUrlConstructor().createCodeSystemUrl(codeSystemName);

        codeSystemReference.setContent(codeSystemName);
        codeSystemReference.setHref(codeSystemPath);
        codeSystemReference.setUri(codeSystemURI);

        return codeSystemReference;
    }

    @Override
    protected Transformer<EntityDescription, EntityDirectoryEntry> getSummarizer() {
        return new Transformer<EntityDescription, EntityDirectoryEntry>() {
            @Override
            public EntityDirectoryEntry toSummary(VertexEntity<EntityDescription> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<EntityDescription, EntityListEntry> getLister() {
        return new Transformer<EntityDescription, EntityListEntry>() {
            @Override
            public EntityListEntry toSummary(VertexEntity<EntityDescription> fullResource) {
                EntityListEntry entry = new EntityListEntry();
                entry.setEntry(fullResource.getEntity());

                return entry;
            }
        };
    }

    @Override
    public boolean isEntityInSet(EntityNameOrURI entity, EntityDescriptionQuery restrictions, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public EntityReferenceList resolveAsEntityReferenceList(EntityDescriptionQuery restrictions, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public EntityNameOrURIList intersectEntityList(Set<EntityNameOrURI> entities, EntityDescriptionQuery restrictions, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<? extends VersionTagReference> getSupportedTags() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected String getResourcePath() {
        return "namedEntity";
    }

    @Override
    protected <T> AqlDirectoryBuilder<EntityDescription, T> addState(EntityDescriptionQuery query, AqlDirectoryBuilder<EntityDescription, T> builder, Page page) {
        builder = super.addState(query, builder, page);

        if(query != null && CollectionUtils.isNotEmpty(query.getFilterComponent())) {
            return builder;
        }

        if(query != null && query.getRestrictions() != null && query.getRestrictions().getHierarchyRestriction() != null) {
            if (query.getRestrictions().getHierarchyRestriction() != null) {
                EntityDescriptionQueryServiceRestrictions.HierarchyRestriction hierarchyRestrictions = query.getRestrictions().getHierarchyRestriction();
                String entityName = hierarchyRestrictions.getEntity().getEntityName().getName();

                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("entityName", entityName);
                parameters.put("codeSystemVersion", query.getRestrictions().getCodeSystemVersions().iterator().next().getName());

                AqlDirectoryBuilder.AqlState entity =
                        new AqlDirectoryBuilder.NamedAqlClauseDecorator(
                                "entities", new AqlDirectoryBuilder.SimpleAqlClause(this.getCollection(),
                                    new AqlDirectoryBuilder.AqlFilter("x.namedEntity.describingCodeSystemVersion.version.content == @codeSystemVersion", parameters),
                                    new AqlDirectoryBuilder.AqlFilter("x.namedEntity.entityID.name == @entityName", parameters)));

                int depth;
                String direction;
                switch (hierarchyRestrictions.getHierarchyType()) {
                    case CHILDREN: depth = 1; direction = "outbound"; break;
                    case ANCESTORS: depth = -1; direction = "inbound"; break;
                    case DESCENDANTS: depth = -1; direction = "outbound"; break;
                    default: throw new IllegalStateException();
                }

                parameters.put("depth", depth);
                parameters.put("direction", direction);

                String aql =
                        "let children = (\n" +
                        "    for i in entities\n" +
                        "       for e in GRAPH_NEIGHBORS(\"Association\", i._id, \n" +
                        "           {maxDepth: @depth, direction: @direction, includeData:false, edgeExamples : [{isHierarchy: true}]}) " + AqlUtils.getLimitOffset(page, parameters) + " return e\n" +
                        ")\n" +
                        "for entity in EntityDescription\n" +
                        "    filter entity._id IN children\n" +
                        "       return entity";

                builder.addState(entity);
                builder.addState(new AqlDirectoryBuilder.RawAql(aql, parameters));
            }
        } else {
            if(query != null && query.getRestrictions() != null && CollectionUtils.isNotEmpty(query.getRestrictions().getCodeSystemVersions())) {
                List<AqlDirectoryBuilder.AqlFilter> filters = Lists.newArrayList();

                Set<NameOrURI> codeSystemVersions = query.getRestrictions().getCodeSystemVersions();
                Set<String> csvNames = Sets.newHashSet();

                for(NameOrURI nameOrURI : codeSystemVersions) {
                    csvNames.add(nameOrURI.getName());
                }

                Map<String, Object> params = Maps.newHashMap();
                params.put("codeSystemVersion", csvNames);

                filters.add(
                        new AqlDirectoryBuilder.AqlFilter("x.namedEntity.describingCodeSystemVersion.version.content IN @codeSystemVersion", params));

                if(CollectionUtils.isNotEmpty(query.getRestrictions().getEntities())) {
                    Set<String> entityUris = Sets.newHashSet();

                    for(EntityNameOrURI nameOrURI : query.getRestrictions().getEntities()) {
                        entityUris.add(nameOrURI.getUri());
                    }

                    params.put("entity", entityUris);
                    filters.add(
                            new AqlDirectoryBuilder.AqlFilter("x.namedEntity.about IN @entity", params));
                }

                builder.addState(new AqlDirectoryBuilder.SimpleAqlClause(
                        this.getCollection(),
                        page.getMaxToReturn(),
                        page.getStart(),
                        null,
                        filters));
            }
        }

        return builder;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filters, EntityDescriptionQuery query, Page page) {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

        for(ResolvedFilter filter : filters) {
            queryBuilder.must(QueryBuilders.matchQuery("designations", filter.getMatchValue()));
        }

        if(query != null && query.getRestrictions() != null && CollectionUtils.isNotEmpty(query.getRestrictions().getCodeSystemVersions())) {
            BoolQueryBuilder csvQuery = QueryBuilders.boolQuery();
            csvQuery.minimumNumberShouldMatch(1);
            for (NameOrURI codeSystemVersion : query.getRestrictions().getCodeSystemVersions()) {
                csvQuery.should(QueryBuilders.termQuery("codeSystemVersion", codeSystemVersion.getName()));
            }

            BoolQueryBuilder overall = QueryBuilders.boolQuery();
            overall.must(queryBuilder);
            overall.must(csvQuery);

            queryBuilder = overall;
        }

        if(query != null && query.getRestrictions() != null && CollectionUtils.isNotEmpty(query.getRestrictions().getEntities())) {
            BoolQueryBuilder csvQuery = QueryBuilders.boolQuery();
            csvQuery.minimumNumberShouldMatch(1);
            for (EntityNameOrURI entityNameOrURI : query.getRestrictions().getEntities()) {
                csvQuery.should(QueryBuilders.termQuery("uri", entityNameOrURI.getUri()));
            }

            BoolQueryBuilder overall = QueryBuilders.boolQuery();
            overall.must(queryBuilder);
            overall.must(csvQuery);

            queryBuilder = overall;
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).
                withPageable(new PageRequest(page.getStart(), page.getMaxToReturn())).
                withTypes(this.getCollection()).build();

        return new AqlDirectoryBuilder.LuceneQuery(elasticsearchDao, searchQuery, ArangoDbServiceConstants.ENTITY_DESCRIPTION_COLLECTION, IndexedEntityDescription.class);
    }

    @Override
    public Class<EntityDescription> getStorageClass() {
        return EntityDescription.class;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.entityDescriptionStorageInfo;
    }

    @Override
    protected String getUriPath() {
        return EntityDescriptionConstants.URI_PATH;
    }

}
