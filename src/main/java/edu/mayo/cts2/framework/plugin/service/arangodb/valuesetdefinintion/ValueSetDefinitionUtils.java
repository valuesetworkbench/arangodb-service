package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;

public final class ValueSetDefinitionUtils {

    public static String getHref(LocalIdValueSetDefinition localIdvalueSetDefinition, UrlConstructor urlConstructor) {
        return urlConstructor.createValueSetDefinitionUrl(
                localIdvalueSetDefinition.getResource().getDefinedValueSet().getContent(),
                localIdvalueSetDefinition.getLocalID());
    }

}
