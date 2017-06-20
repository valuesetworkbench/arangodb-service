package edu.mayo.cts2.framework.plugin.service.arangodb;

public abstract class AbstractArangoDbDefaultReadService<R,I> extends AbstractArangoDbBaseReadService<R,R,I> {

    @Override
    protected R unwrap(R resource) {
        return resource;
    }

}
