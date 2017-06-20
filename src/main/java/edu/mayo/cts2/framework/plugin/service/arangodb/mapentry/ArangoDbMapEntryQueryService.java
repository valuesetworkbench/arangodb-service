package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry;

import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.core.util.EncodingUtils;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.mapversion.MapEntry;
import edu.mayo.cts2.framework.model.mapversion.MapEntryDirectoryEntry;
import edu.mayo.cts2.framework.model.mapversion.MapEntryListEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryQuery;
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryQueryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbMapEntryQueryService extends AbstractArangoDbDefaultQueryService<MapEntry, MapEntryListEntry, MapEntryDirectoryEntry, MapEntryQuery> implements MapEntryQueryService {

    @Resource
    private UrlConstructor urlConstructor;

    @Resource
    private MapEntryStorageInfo mapEntryStorageInfo;

    protected MapEntryDirectoryEntry toDirectoryEntry(VertexEntity<MapEntry> node) {
        MapEntry mapEntry = node.getEntity();

        MapEntryDirectoryEntry directoryEntry = new MapEntryDirectoryEntry();
        directoryEntry.setAssertedBy(mapEntry.getAssertedBy());
        directoryEntry.setMapFrom(mapEntry.getMapFrom());
        directoryEntry.setHref(this.urlConstructor.createMapEntryUrl(
                mapEntry.getAssertedBy().getMap().getContent(),
                mapEntry.getAssertedBy().getMapVersion().getContent(),
                EncodingUtils.encodeScopedEntityName(mapEntry.getMapFrom())));

        return directoryEntry;
    }

    @Override
    protected Transformer<MapEntry, MapEntryDirectoryEntry> getSummarizer() {
        return new Transformer<MapEntry, MapEntryDirectoryEntry>() {
            @Override
            public MapEntryDirectoryEntry toSummary(VertexEntity<MapEntry> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<MapEntry, MapEntryListEntry> getLister() {
        return new Transformer<MapEntry, MapEntryListEntry>() {
            @Override
            public MapEntryListEntry toSummary(VertexEntity<MapEntry> fullResource) {
                MapEntry mapEntry = fullResource.getEntity();

                MapEntryListEntry entry = new MapEntryListEntry();
                entry.setEntry(mapEntry);

                entry.setHref(MapEntryUtils.getHref(mapEntry, getUrlConstructor()));

                return entry;
            }
        };
    }

    @Override
    protected <T> AqlDirectoryBuilder<MapEntry, T> addState(MapEntryQuery query, AqlDirectoryBuilder<MapEntry, T> builder, Page page) {
        String mapVersion = query.getRestrictions().getMapVersion().getName();

        Map<String,Object> parameters = Maps.newHashMap();
        parameters.put("assertedBy", mapVersion);

        builder.addState(
                new AqlDirectoryBuilder.SimpleAqlClause(
                        this.getCollection(),
                        new AqlDirectoryBuilder.AqlFilter("x.assertedBy.mapVersion.content == @assertedBy", parameters)));

        return builder;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filter, MapEntryQuery query, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Class<MapEntry> getStorageClass() {
        return MapEntry.class;
    }

    @Override
    public StorageInfo<MapEntry> getStorageInfo() {
        return this.mapEntryStorageInfo;
    }

    @Override
    protected String getUriPath() {
        return MapEntryConstants.URI_PATH;
    }

}
