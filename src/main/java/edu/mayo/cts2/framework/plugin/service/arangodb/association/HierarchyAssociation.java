package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import edu.mayo.cts2.framework.model.association.Association;

public class HierarchyAssociation extends Association {

    private final boolean isHierarchy = true;

    public final boolean isHierarchy() {
        return isHierarchy;
    }

}
