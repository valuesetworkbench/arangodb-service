package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.entity.marker.VertexEntity;

public interface Transformer<F,S> {

    S toSummary(VertexEntity<F> fullResource);

}
