package edu.mayo.cts2.framework.plugin.service.arangodb.codesystemversion;

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbNameOrUriReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionReadService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ArangoDbCodeSystemVersionReadService extends AbstractArangoDbNameOrUriReadService<CodeSystemVersionCatalogEntry> implements CodeSystemVersionReadService {

    @Resource
    private CodeSystemVersionStorageInfo codeSystemVersionStorageInfo;

    @Override
    public boolean existsVersionId(NameOrURI codeSystem, String officialResourceVersionId) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CodeSystemVersionCatalogEntry getCodeSystemByVersionId(NameOrURI codeSystem, String officialResourceVersionId, ResolvedReadContext readContext) {
        return null;
    }

    @Override
    public CodeSystemVersionCatalogEntry readByTag(NameOrURI parentIdentifier, VersionTagReference tag, ResolvedReadContext readContext) {
        return null;
    }

    @Override
    public boolean existsByTag(NameOrURI parentIdentifier, VersionTagReference tag, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<VersionTagReference> getSupportedTags() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected String getNamePath() {
        return "codeSystemVersionName";
    }

    @Override
    public Class<CodeSystemVersionCatalogEntry> getStorageClass() {
        return CodeSystemVersionCatalogEntry.class;
    }

    @Override
    public StorageInfo<CodeSystemVersionCatalogEntry> getStorageInfo() {
        return this.codeSystemVersionStorageInfo;
    }

}
