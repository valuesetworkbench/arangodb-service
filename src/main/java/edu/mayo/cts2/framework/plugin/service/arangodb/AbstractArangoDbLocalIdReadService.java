package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.extension.LocalIdResource;

import java.util.Map;

public abstract class AbstractArangoDbLocalIdReadService<W extends LocalIdResource<R>,R,I> extends AbstractArangoDbBaseReadService<W,R,I> {

    @Override
    protected AqlQuery getNameFilter(I identifier) {
        String aql = "FILTER x." + ArangoDbServiceConstants.LOCAL_ID_PROP + " == @key";

        Map<String,Object> params = Maps.newHashMap();
        params.put("key", this.getName(identifier));

        return new AqlQuery(aql, params);
    }

    @Override
    protected R unwrap(W resource) {
        return resource.getResource();
    }

    protected abstract W toLocalIdResource(String key, R resource);

    protected abstract String getName(I identifier);

}
