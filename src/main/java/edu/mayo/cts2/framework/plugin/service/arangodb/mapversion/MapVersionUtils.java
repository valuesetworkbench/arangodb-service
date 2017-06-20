package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;

public final class MapVersionUtils {

    public static String getHref(MapVersion mapVersion, UrlConstructor urlConstructor) {
        return urlConstructor.createMapVersionUrl(mapVersion.getVersionOf().getContent(), mapVersion.getMapVersionName());
    }

}
