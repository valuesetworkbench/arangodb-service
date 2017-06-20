package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion;

import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticSearchIndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.IndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MapVersionStorageInfo extends AbstractStorageInfo<MapVersion> {

    @Override
    public String getCollection() {
        return ArangoDbServiceConstants.MAP_VERSION_COLLECTION;
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
        return Sets.newHashSet(new ElasticSearchIndexSpecification(ArangoDbServiceConstants.MAP_VERSION_COLLECTION, "mappings/MapVersion.json"));
    }

    @Override
    public Set<? extends TypeAdapterProvider> getTypeAdapters() {
        return Sets.newHashSet();
    }

    @Override
    protected String doGetArangoDbKey(MapVersion resource) {
        return resource.getAbout();
    }

}
