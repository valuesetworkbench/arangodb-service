package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry;

import edu.mayo.cts2.framework.model.mapversion.MapEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryMaintenanceService;
import edu.mayo.cts2.framework.service.profile.mapentry.name.MapEntryReadId;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ArangoDbMapEntryMaintenanceService extends AbstractArangoDbDefaultMaintenanceService<MapEntry,MapEntryReadId> implements MapEntryMaintenanceService {

    @Resource
    private MapEntryStorageInfo mapEntryStorageInfo;

    @Resource
    private ArangoDbMapEntryReadService arangoDbMapEntryReadService;

    @Override
    public void updateChangeableMetadata(MapEntryReadId identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected IndexDocument<MapEntry> getIndexDocument(String handle, MapEntry document) {
        return null;
    }

    @Override
    protected String getAbout(MapEntry resource) {
        return resource.getMapFrom().getUri();
    }

    @Override
    protected String getAbout(MapEntryReadId identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<MapEntry, MapEntryReadId> getDocumentReader() {
        return this.arangoDbMapEntryReadService;
    }

    @Override
    public StorageInfo<MapEntry> getStorageInfo() {
        return this.mapEntryStorageInfo;
    }

    @Override
    protected String getUriPath() {
        return MapEntryConstants.URI_PATH;
    }

    @Override
    protected String getHref(MapEntry resource) {
        return MapEntryUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    public Class<MapEntry> getStorageClass() {
        return MapEntry.class;
    }

}
