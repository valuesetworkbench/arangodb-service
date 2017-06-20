package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion;

import com.arangodb.VertexCursor;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.core.MapVersionReference;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.types.FinalizableState;
import edu.mayo.cts2.framework.model.mapversion.MapEntry;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.plugin.service.arangodb.mapentry.ArangoDbMapEntryMaintenanceService;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionMaintenanceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class ArangoDbMapVersionMaintenanceService extends AbstractArangoDbDefaultMaintenanceService<MapVersion,NameOrURI> implements MapVersionMaintenanceService {

    @Resource
    private MapVersionStorageInfo mapVersionStorageInfo;

    @Resource
    private ArangoDbMapVersionReadService readService;

    @Resource
    private ArangoDbMapEntryMaintenanceService mapEntryMaintenanceService;

    @Override
    public void updateChangeableMetadata(NameOrURI identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void beforeUpdate(DocumentEntity<MapVersion> oldResource, MapVersion newResource) {
        if(oldResource.getEntity().getState() == FinalizableState.FINAL) {
            //TODO: Relax this for now
            //throw new ResourceIsNotOpen();
        }
    }

    @Override
    protected void deleteDependencies(NameOrURI resource) {
        String mapVersion = resource.getName();

        Map<String,Object> parameters = Maps.newHashMap();
        parameters.put("assertedBy", mapVersion);

        AqlDirectoryBuilder.AqlState state = new AqlDirectoryBuilder.SimpleAqlClause(
                ArangoDbServiceConstants.MAP_ENTRY_COLLECTION,
                new AqlDirectoryBuilder.AqlFilter("x.assertedBy.mapVersion.content == @assertedBy", parameters));

        VertexCursor<MapEntry> mapEntries = this.getArangoDao().query(state.getAql(), state.getParameters(), MapEntry.class);

        this.mapEntryMaintenanceService.moveDocumentsToHistory(mapEntries.asEntityList());
    }

    @Override
    protected void cloneDependencies(NameOrURI resourceToClone, MapVersion newResource) {
        String mapVersion = resourceToClone.getName();

        Map<String,Object> parameters = Maps.newHashMap();
        parameters.put("assertedBy", mapVersion);

        AqlDirectoryBuilder.AqlState state = new AqlDirectoryBuilder.SimpleAqlClause(
                ArangoDbServiceConstants.MAP_ENTRY_COLLECTION,
                new AqlDirectoryBuilder.AqlFilter("x.assertedBy.mapVersion.content == @assertedBy", parameters));

        VertexCursor<MapEntry> mapEntries = this.getArangoDao().query(state.getAql(), state.getParameters(), MapEntry.class);

        List<MapEntry> mapEntryList = Lists.newArrayList();

        for(VertexEntity<MapEntry> mapEntry : mapEntries) {
            MapEntry entry = mapEntry.getEntity();
            entry.setAssertedBy(this.toMapVersionReference(newResource));
            mapEntryList.add(entry);
        }

        mapEntryMaintenanceService.importResources(mapEntryList);
    }

    private MapVersionReference toMapVersionReference(MapVersion mapVersion) {
        MapVersionReference reference = new MapVersionReference();
        reference.setMap(mapVersion.getVersionOf());

        NameAndMeaningReference nameAndMeaningReference = new NameAndMeaningReference();
        nameAndMeaningReference.setContent(mapVersion.getMapVersionName());
        nameAndMeaningReference.setUri(mapVersion.getAbout());

        reference.setMapVersion(nameAndMeaningReference);

        return reference;
    }

    @Override
    protected IndexDocument<MapVersion> getIndexDocument(String handle, MapVersion document) {
        return new IndexedMapVersion(handle, document);
    }

    @Override
    protected String getAbout(MapVersion resource) {
        return resource.getAbout();
    }

    @Override
    protected String getAbout(NameOrURI identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<MapVersion, NameOrURI> getDocumentReader() {
        return this.readService;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.mapVersionStorageInfo;
    }

    @Override
    protected String getHref(MapVersion resource) {
        return MapVersionUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    public Class<MapVersion> getStorageClass() {
        return MapVersion.class;
    }

}
