package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;

public final class AssociationUtils {

    public static String getHref(LocalIdAssociation localIdAssociation, UrlConstructor urlConstructor) {
        Association resource = localIdAssociation.getResource();

        String codeSystemName = resource.getAssertedBy().getCodeSystem().getContent();
        String codeSystemVersionName = resource.getAssertedBy().getVersion().getContent();

        return urlConstructor.createAssociationOfCodeSystemVersionUrl(codeSystemName, codeSystemVersionName, localIdAssociation.getLocalID());
    }

}
