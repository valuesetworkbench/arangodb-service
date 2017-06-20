package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticSearchIndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.IndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AssociationStorageInfo extends AbstractStorageInfo<LocalIdAssociation> {

    @Override
    public String getCollection() {
        return ArangoDbServiceConstants.ASSOCIATION_COLLECTION;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.EDGE;
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
        return Sets.newHashSet(new AssociationTypeAdapterProvider());
    }

    @Override
    protected String doGetArangoDbKey(LocalIdAssociation resource) {
        return resource.getLocalID();
    }

}
