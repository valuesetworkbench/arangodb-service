package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem;

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbNameOrUriReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.codesystem.CodeSystemReadService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ArangoDbCodeSystemReadService extends AbstractArangoDbNameOrUriReadService<CodeSystemCatalogEntry> implements CodeSystemReadService {

    @Resource
    private CodeSystemStorageInfo codeSystemStorageInfo;

    @Override
    protected String getNamePath() {
        return "codeSystemName";
    }

    @Override
    public Class<CodeSystemCatalogEntry> getStorageClass() {
        return CodeSystemCatalogEntry.class;
    }

    @Override
    public StorageInfo<CodeSystemCatalogEntry> getStorageInfo() {
        return this.codeSystemStorageInfo;
    }

}
