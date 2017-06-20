package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry;

import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.mapversion.MapEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticSearchIndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.IndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MapEntryStorageInfo extends AbstractStorageInfo<MapEntry> {

    @Override
    public String getCollection() {
        return ArangoDbServiceConstants.MAP_ENTRY_COLLECTION;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.DOCUMENT;
    }

    @Override
    public Set<IndexSpecification> getIndexSpecifications() {
        return Sets.newHashSet();
    }

    @Override
    public Set<ElasticSearchIndexSpecification> getElasticSearchIndexSpecifications() {
        return Sets.newHashSet();
    }

    @Override
    public Set<? extends TypeAdapterProvider> getTypeAdapters() {
        return Sets.newHashSet();
    }

    @Override
    protected String doGetArangoDbKey(MapEntry resource) {
        return resource.getMapFrom().getUri() + resource.getAssertedBy().getMapVersion().getContent();
    }

}
