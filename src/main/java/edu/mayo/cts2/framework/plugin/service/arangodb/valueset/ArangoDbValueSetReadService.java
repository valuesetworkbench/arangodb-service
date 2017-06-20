package edu.mayo.cts2.framework.plugin.service.arangodb.valueset;

import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbNameOrUriReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ArangoDbValueSetReadService extends AbstractArangoDbNameOrUriReadService<ValueSetCatalogEntry> implements ValueSetReadService {

    @Resource
    private ValueSetStorageInfo valueSetStorageInfo;

    @Override
    protected String getNamePath() {
        return "valueSetName";
    }

    @Override
    public Class<ValueSetCatalogEntry> getStorageClass() {
        return ValueSetCatalogEntry.class;
    }

    @Override
    public StorageInfo<ValueSetCatalogEntry> getStorageInfo() {
        return this.valueSetStorageInfo;
    }

}
