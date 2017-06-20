package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.GraphInfo;
import org.springframework.stereotype.Component;

@Component
public class AssocitionGraphInfo implements GraphInfo {

    @Override
    public String getName() {
        return ArangoDbServiceConstants.ASSOCIATION_GRAPH;
    }

    @Override
    public String getEdgeCollection() {
        return ArangoDbServiceConstants.ASSOCIATION_COLLECTION;
    }

    @Override
    public String getFromCollection() {
        return ArangoDbServiceConstants.ENTITY_DESCRIPTION_COLLECTION;
    }

    @Override
    public String getToCollection() {
        return ArangoDbServiceConstants.ENTITY_DESCRIPTION_COLLECTION;
    }

}
