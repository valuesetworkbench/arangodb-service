package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion;

import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbNameOrUriReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionHistoryService;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionReadService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ArangoDbMapVersionReadService extends AbstractArangoDbNameOrUriReadService<MapVersion> implements MapVersionReadService, MapVersionHistoryService {

    @Resource
    private MapVersionStorageInfo mapVersionStorageInfo;

    @Override
    public Class<MapVersion> getStorageClass() {
        return MapVersion.class;
    }

    @Override
    public MapVersion readByTag(NameOrURI parentIdentifier, VersionTagReference tag, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
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
        return "mapVersionName";
    }

    @Override
    public StorageInfo<MapVersion> getStorageInfo() {
        return this.mapVersionStorageInfo;
    }

}
