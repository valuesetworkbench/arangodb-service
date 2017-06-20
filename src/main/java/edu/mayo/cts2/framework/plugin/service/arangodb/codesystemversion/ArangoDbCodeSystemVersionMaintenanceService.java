package edu.mayo.cts2.framework.plugin.service.arangodb.codesystemversion;

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionMaintenanceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ArangoDbCodeSystemVersionMaintenanceService extends AbstractArangoDbDefaultMaintenanceService<CodeSystemVersionCatalogEntry,NameOrURI> implements CodeSystemVersionMaintenanceService {

    @Resource
    private CodeSystemVersionStorageInfo codeSystemVersionStorageInfo;

    @Resource
    private ArangoDbCodeSystemVersionReadService readService;

    @Override
    public void updateChangeableMetadata(NameOrURI identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected IndexDocument<CodeSystemVersionCatalogEntry> getIndexDocument(String handle, CodeSystemVersionCatalogEntry document) {
        return null;
    }

    @Override
    protected String getAbout(CodeSystemVersionCatalogEntry resource) {
        return resource.getAbout();
    }

    @Override
    protected String getAbout(NameOrURI identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<CodeSystemVersionCatalogEntry, NameOrURI> getDocumentReader() {
        return this.readService;
    }

    @Override
    public StorageInfo<CodeSystemVersionCatalogEntry> getStorageInfo() {
        return this.codeSystemVersionStorageInfo;
    }

    @Override
    protected String getHref(CodeSystemVersionCatalogEntry resource) {
        return CodeSystemVersionUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    public Class<CodeSystemVersionCatalogEntry> getStorageClass() {
        return CodeSystemVersionCatalogEntry.class;
    }

}
