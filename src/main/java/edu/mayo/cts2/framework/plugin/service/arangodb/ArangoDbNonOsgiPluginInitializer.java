package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.util.spring.AbstractSpringNonOsgiPluginInitializer;

public class ArangoDbNonOsgiPluginInitializer extends AbstractSpringNonOsgiPluginInitializer {

    @Override
    protected String[] getContextConfigLocations() {
        return new String[]{
                "/META-INF/spring/arangodb-service-context.xml",
                "/META-INF/spring/osgi-context.xml"};
    }

}