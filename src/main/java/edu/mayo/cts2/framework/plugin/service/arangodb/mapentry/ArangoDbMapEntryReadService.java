package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry;

import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.mapversion.MapEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlQuery;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryReadService;
import edu.mayo.cts2.framework.service.profile.mapentry.name.MapEntryReadId;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ArangoDbMapEntryReadService extends AbstractArangoDbDefaultReadService<MapEntry, MapEntryReadId> implements MapEntryReadService {

    @Resource
    private MapEntryStorageInfo mapEntryStorageInfo;

    @Override
    public Class<MapEntry> getStorageClass() {
        return MapEntry.class;
    }

    @Override
    protected boolean isUriQuery(MapEntryReadId identifier) {
        return StringUtils.isNotBlank(identifier.getUri());
    }

    @Override
    protected String getUri(MapEntryReadId identifier) {
        return identifier.getUri();
    }

    @Override
    protected AqlQuery getNameFilter(MapEntryReadId identifier) {
        String aql = "FILTER x.mapFrom.name == @name AND x.mapFrom.namespace == @namespace AND x.assertedBy.mapVersion.content == @mapVersion";

        String mapVersion = identifier.getMapVersion().getName();
        String entityName = identifier.getEntityName().getName();
        String entityNamespace = identifier.getEntityName().getNamespace();

        Map<String,Object> parameters = Maps.newHashMap();
        parameters.put("name", entityName);
        parameters.put("namespace", entityNamespace);
        parameters.put("mapVersion", mapVersion);

        return new AqlQuery(aql, parameters);
    }

    @Override
    public boolean exists(MapEntryReadId identifier, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected String getUriPath() {
        return MapEntryConstants.URI_PATH;
    }

    @Override
    public StorageInfo<MapEntry> getStorageInfo() {
        return this.mapEntryStorageInfo;
    }

}
