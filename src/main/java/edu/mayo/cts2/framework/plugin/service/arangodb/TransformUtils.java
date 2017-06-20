package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.model.core.ResourceDescription;
import edu.mayo.cts2.framework.model.core.ResourceDescriptionDirectoryEntry;
import edu.mayo.cts2.framework.model.core.ResourceVersionDescription;
import edu.mayo.cts2.framework.model.core.ResourceVersionDescriptionDirectoryEntry;

public final class TransformUtils {

    public final static <E extends ResourceDescriptionDirectoryEntry, D extends ResourceDescription> E baseTransform(
            E summary, D resource) {

        summary.setAbout(resource.getAbout());
        summary.setFormalName(resource.getFormalName());
        summary.setResourceSynopsis(resource.getResourceSynopsis());

        return summary;
    }

    public final static<E extends ResourceVersionDescriptionDirectoryEntry, D extends ResourceVersionDescription> E baseTransformResourceVersion(
            E summary, D resource) {

        summary = baseTransform(summary, resource);
        summary.setDocumentURI(resource.getDocumentURI());
        summary.setOfficialReleaseDate(resource.getOfficialReleaseDate());
        summary.setOfficialResourceVersionId(resource.getOfficialResourceVersionId());

        return summary;
    }

}
