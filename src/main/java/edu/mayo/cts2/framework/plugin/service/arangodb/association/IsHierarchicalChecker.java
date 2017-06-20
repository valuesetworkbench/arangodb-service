package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import edu.mayo.cts2.framework.model.core.PredicateReference;
import org.apache.commons.lang.StringUtils;

public class IsHierarchicalChecker {

    private static final String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";

    protected boolean isHierarchical(PredicateReference predicateReference) {
        return predicateReference != null && StringUtils.equals(predicateReference.getUri(), RDFS_SUBCLASS_OF);
    }

}
