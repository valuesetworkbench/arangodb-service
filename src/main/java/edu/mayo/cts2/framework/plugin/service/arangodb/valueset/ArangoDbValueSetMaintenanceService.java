package edu.mayo.cts2.framework.plugin.service.arangodb.valueset;

import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetMaintenanceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ArangoDbValueSetMaintenanceService extends AbstractArangoDbDefaultMaintenanceService<ValueSetCatalogEntry,NameOrURI> implements ValueSetMaintenanceService {

    @Resource
    private ValueSetStorageInfo valueSetStorageInfo;

    @Resource
    private ArangoDbValueSetReadService readService;

    @Override
    public void updateChangeableMetadata(NameOrURI identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected IndexDocument<ValueSetCatalogEntry> getIndexDocument(String handle, ValueSetCatalogEntry document) {
        return null;
    }

    @Override
    protected String getAbout(ValueSetCatalogEntry resource) {
        return resource.getAbout();
    }

    @Override
    protected String getAbout(NameOrURI identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<ValueSetCatalogEntry, NameOrURI> getDocumentReader() {
        return this.readService;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.valueSetStorageInfo;
    }

    @Override
    protected String getHref(ValueSetCatalogEntry resource) {
        return this.getUrlConstructor().createValueSetUrl(resource.getValueSetName());
    }

    @Override
    public Class<ValueSetCatalogEntry> getStorageClass() {
        return ValueSetCatalogEntry.class;
    }

}
