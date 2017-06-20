package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;

public final class CodeSystemUtils {

    public static String getHref(CodeSystemCatalogEntry codeSystemCatalogEntry, UrlConstructor urlConstructor) {
        return urlConstructor.createCodeSystemUrl(codeSystemCatalogEntry.getCodeSystemName());
    }

}
