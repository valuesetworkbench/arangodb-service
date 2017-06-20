package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem;

import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticSearchIndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.IndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CodeSystemStorageInfo extends AbstractStorageInfo<CodeSystemCatalogEntry> {

    @Override
    public String getCollection() {
        return ArangoDbServiceConstants.CODE_SYSTEM_COLLECTION;
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
    protected String doGetArangoDbKey(CodeSystemCatalogEntry resource) {
        return resource.getAbout();
    }

}
