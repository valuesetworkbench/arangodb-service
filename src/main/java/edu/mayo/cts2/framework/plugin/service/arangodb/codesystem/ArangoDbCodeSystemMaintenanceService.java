package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem;

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.codesystem.CodeSystemMaintenanceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ArangoDbCodeSystemMaintenanceService extends AbstractArangoDbDefaultMaintenanceService<CodeSystemCatalogEntry,NameOrURI> implements CodeSystemMaintenanceService {

    @Resource
    private CodeSystemStorageInfo codeSystemStorageInfo;

    @Resource
    private ArangoDbCodeSystemReadService readService;

    @Override
    public void updateChangeableMetadata(NameOrURI identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected IndexDocument<CodeSystemCatalogEntry> getIndexDocument(String handle, CodeSystemCatalogEntry document) {
        return null;
    }

    @Override
    protected String getAbout(CodeSystemCatalogEntry resource) {
        return resource.getAbout();
    }

    @Override
    protected String getAbout(NameOrURI identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<CodeSystemCatalogEntry, NameOrURI> getDocumentReader() {
        return this.readService;
    }

    @Override
    public StorageInfo<CodeSystemCatalogEntry> getStorageInfo() {
        return this.codeSystemStorageInfo;
    }

    @Override
    protected String getHref(CodeSystemCatalogEntry resource) {
        return CodeSystemUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    public Class<CodeSystemCatalogEntry> getStorageClass() {
        return CodeSystemCatalogEntry.class;
    }


}
