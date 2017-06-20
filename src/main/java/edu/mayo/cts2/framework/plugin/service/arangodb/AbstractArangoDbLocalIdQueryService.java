package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.model.extension.LocalIdResource;
import edu.mayo.cts2.framework.service.profile.ResourceQuery;

public abstract class AbstractArangoDbLocalIdQueryService<T extends LocalIdResource<R>,R,L,S,Q  extends ResourceQuery> extends AbstractArangoDbQueryService<T,R,L,S,Q>  {

}
