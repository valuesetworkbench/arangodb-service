package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;

import java.util.Set;

public interface StorageInfo<R> {

    enum StorageType {DOCUMENT, EDGE}

    String getCollection();

    StorageType getStorageType();

    Set<IndexSpecification> getIndexSpecifications();

    Set<ElasticSearchIndexSpecification> getElasticSearchIndexSpecifications();

    Set<? extends TypeAdapterProvider> getTypeAdapters();

    String getArangoDbKey(R resource);

}
