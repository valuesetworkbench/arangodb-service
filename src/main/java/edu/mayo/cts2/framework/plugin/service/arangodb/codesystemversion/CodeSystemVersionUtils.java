package edu.mayo.cts2.framework.plugin.service.arangodb.codesystemversion;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;

public final class CodeSystemVersionUtils {

    public static String getHref(CodeSystemVersionCatalogEntry codeSystemVersionCatalogEntry, UrlConstructor urlConstructor) {
        return urlConstructor.createCodeSystemVersionUrl(
                codeSystemVersionCatalogEntry.getVersionOf().getContent(),
                codeSystemVersionCatalogEntry.getCodeSystemVersionName());
    }

}
