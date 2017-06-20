package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.mapversion.MapEntry;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;

public final class ArangoDbServiceConstants {

    public static final String VALUE_SET_DEFINITION_COLLECTION = ValueSetDefinition.class.getSimpleName();

    public static final String ENTITY_DESCRIPTION_COLLECTION = EntityDescription.class.getSimpleName();

    public static final String MAP_VERSION_COLLECTION = MapVersion.class.getSimpleName();

    public static final String CODE_SYSTEM_VERSION_COLLECTION = CodeSystemVersionCatalogEntry.class.getSimpleName();

    public static final String CODE_SYSTEM_COLLECTION = CodeSystemCatalogEntry.class.getSimpleName();

    public static final String VALUE_SET_COLLECTION = ValueSetCatalogEntry.class.getSimpleName();

    public static final String MAP_ENTRY_COLLECTION = MapEntry.class.getSimpleName();

    public static final String ASSOCIATION_COLLECTION = Association.class.getSimpleName();

    public static final String ASSOCIATION_GRAPH = Association.class.getSimpleName();

    public static final String HISTORY_COLLECTION_SUFFIX = "_History";

    public static final String LOCAL_ID_PROP = "localId";

    public static final String ARANGO_KEY_PROP = "_key";

    public static final String IS_HIERARCY_PROP = "isHierarchy";

    public static final VersionTagReference PRODUCTION_TAG = buildVersionTagReference("PRODUCTION", null);

    private static VersionTagReference buildVersionTagReference(String content, String uri) {
        VersionTagReference versionTagReference = new VersionTagReference();
        versionTagReference.setContent(content);
        versionTagReference.setUri(uri);

        return versionTagReference;
    }
}
