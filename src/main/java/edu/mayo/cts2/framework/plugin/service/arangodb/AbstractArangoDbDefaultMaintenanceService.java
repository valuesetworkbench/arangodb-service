package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.model.core.IsChangeable;

import javax.annotation.Resource;

public abstract class AbstractArangoDbDefaultMaintenanceService<R extends IsChangeable,I> extends AbstractArangoDbMaintenanceService<R,R,I> {

    @Resource
    private ArangoDao arangoDao;

    protected ArangoDao getArangoDao() {
        return arangoDao;
    }

    @Override
    protected R toStorageResource(R entity) {
        return entity;
    }

    @Override
    protected R unwrap(R resource) {
        return resource;
    }

    public Class<R> getResourceClass() {
        return this.getStorageClass();
    }

}
