package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.core.util.EncodingUtils;
import edu.mayo.cts2.framework.model.mapversion.MapEntry;

public final class MapEntryUtils {

    public static String getHref(MapEntry mapEntry, UrlConstructor urlConstructor) {
        return urlConstructor.createMapEntryUrl(
                mapEntry.getAssertedBy().getMap().getContent(),
                mapEntry.getAssertedBy().getMapVersion().getContent(),
                EncodingUtils.encodeScopedEntityName(mapEntry.getMapFrom()));
    }

}
