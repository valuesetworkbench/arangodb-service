package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion;

import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.EntityReferenceList;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.model.mapversion.MapVersionDirectoryEntry;
import edu.mayo.cts2.framework.model.mapversion.MapVersionListEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.mapversion.types.MapRole;
import edu.mayo.cts2.framework.model.service.mapversion.types.MapStatus;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticsearchDao;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticsearchUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.TransformUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionQuery;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionQueryService;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbMapVersionQueryService extends AbstractArangoDbDefaultQueryService<MapVersion, MapVersionListEntry, MapVersionDirectoryEntry, MapVersionQuery> implements MapVersionQueryService {

    @Resource
    private MapVersionStorageInfo mapVersionStorageInfo;

    @Resource
    private ElasticsearchDao elasticsearchDao;

    private Set<String> indexSearchFields = Sets.newHashSet(
            "resourceSynopsis",
            "formalName",
            "mapVersionName",
            "mapAnalyzed",
            "officialResourceVersionId"
    );

    protected MapVersionDirectoryEntry toDirectoryEntry(VertexEntity<MapVersion> node) {
        MapVersion mapVersion = node.getEntity();

        MapVersionDirectoryEntry directoryEntry = new MapVersionDirectoryEntry();
        directoryEntry = TransformUtils.baseTransformResourceVersion(directoryEntry, mapVersion);

        directoryEntry.setMapVersionName(mapVersion.getMapVersionName());
        directoryEntry.setVersionOf(mapVersion.getVersionOf());
        directoryEntry.setVersionTag(mapVersion.getVersionTag());

        directoryEntry.setHref(MapVersionUtils.getHref(mapVersion, this.getUrlConstructor()));

        return directoryEntry;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filters, MapVersionQuery query, Page page) {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

        for(ResolvedFilter filter : filters) {
            queryBuilder = ElasticsearchUtils.buildQueryFromFilter(queryBuilder, filter, this.indexSearchFields);
        }

        if(query != null && query.getRestrictions() != null && query.getRestrictions().getMap() != null) {
            BoolQueryBuilder overall = QueryBuilders.boolQuery();
            overall.must(queryBuilder);
            overall.must(QueryBuilders.termQuery("map", query.getRestrictions().getMap().getName()));

            queryBuilder = overall;
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).
                withPageable(new PageRequest(page.getStart(), page.getMaxToReturn())).
                withTypes(this.getCollection()).build();

        return new AqlDirectoryBuilder.LuceneQuery(elasticsearchDao, searchQuery, ArangoDbServiceConstants.MAP_VERSION_COLLECTION, IndexedMapVersion.class);
    }

    @Override
    protected <T> AqlDirectoryBuilder<MapVersion,T> addState(MapVersionQuery query, AqlDirectoryBuilder<MapVersion, T> builder, Page page) {

        if(query != null && CollectionUtils.isEmpty(query.getFilterComponent()) && query.getRestrictions() != null && query.getRestrictions().getMap() != null) {
            Map<String,Object> params = Maps.newHashMap();
            params.put("map", query.getRestrictions().getMap().getName());

            builder.addState(new AqlDirectoryBuilder.SimpleAqlClause(this.getCollection(), new AqlDirectoryBuilder.AqlFilter("x.versionOf.content == @map", params)));
        }

        return super.addState(query, builder, page);
    }

    @Override
    protected Transformer<MapVersion, MapVersionDirectoryEntry> getSummarizer() {
        return new Transformer<MapVersion, MapVersionDirectoryEntry>() {
            @Override
            public MapVersionDirectoryEntry toSummary(VertexEntity<MapVersion> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<MapVersion, MapVersionListEntry> getLister() {
        return new Transformer<MapVersion, MapVersionListEntry>() {
            @Override
            public MapVersionListEntry toSummary(VertexEntity<MapVersion> fullResource) {
                MapVersionListEntry entry = new MapVersionListEntry();
                entry.setEntry(fullResource.getEntity());

                return entry;
            }
        };
    }

    @Override
    public DirectoryResult<EntityDirectoryEntry> mapVersionEntities(NameOrURI mapVersion, MapRole mapRole, MapStatus mapStatus, EntityDescriptionQuery query, SortCriteria sort, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public DirectoryResult<EntityDescription> mapVersionEntityList(NameOrURI mapVersion, MapRole mapRole, MapStatus mapStatus, EntityDescriptionQuery query, SortCriteria sort, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public EntityReferenceList mapVersionEntityReferences(NameOrURI mapVersion, MapRole mapRole, MapStatus mapStatus, EntityDescriptionQuery query, SortCriteria sort, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Class<MapVersion> getStorageClass() {
        return MapVersion.class;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.mapVersionStorageInfo;
    }

}
