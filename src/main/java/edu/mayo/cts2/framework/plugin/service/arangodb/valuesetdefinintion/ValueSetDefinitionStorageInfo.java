package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticSearchIndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.IndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ValueSetDefinitionStorageInfo extends AbstractStorageInfo<LocalIdValueSetDefinition> {

    @Override
    public String getCollection() {
        return ArangoDbServiceConstants.VALUE_SET_DEFINITION_COLLECTION;
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
        return Sets.newHashSet(new ElasticSearchIndexSpecification(ArangoDbServiceConstants.VALUE_SET_DEFINITION_COLLECTION, "mappings/ValueSetDefinition.json"));
    }

    @Override
    public Set<? extends TypeAdapterProvider> getTypeAdapters() {
        return Sets.newHashSet(new ValueSetDefinitionTypeAdapterProvider());
    }

    @Override
    protected String doGetArangoDbKey(LocalIdValueSetDefinition resource) {
        return resource.getLocalID();
    }

}
