package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDescriptionBase;
import edu.mayo.cts2.framework.model.util.ModelUtils;

public final class EntityDescriptionUtils {

    public static String getHref(EntityDescription entityDescription, UrlConstructor urlConstructor) {
        EntityDescriptionBase entityDescriptionBase = ModelUtils.getEntity(entityDescription);

        String codeSystemName =
                entityDescriptionBase.getDescribingCodeSystemVersion().getCodeSystem().getContent();

        String codeSystemVersionName =
                entityDescriptionBase.getDescribingCodeSystemVersion().getVersion().getContent();

        return urlConstructor.createEntityUrl(codeSystemName, codeSystemVersionName, entityDescriptionBase.getEntityID());
    }

}
