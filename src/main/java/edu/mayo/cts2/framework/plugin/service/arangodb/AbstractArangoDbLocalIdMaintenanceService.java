package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.model.core.IsChangeable;
import edu.mayo.cts2.framework.model.extension.ChangeableLocalIdResource;

import javax.annotation.Resource;

public abstract class AbstractArangoDbLocalIdMaintenanceService<T extends ChangeableLocalIdResource<R>,R extends IsChangeable,I> extends AbstractArangoDbMaintenanceService<T,R,I> {

    @Resource
    private ArangoDao arangoDao;

    protected ArangoDao getArangoDao() {
        return arangoDao;
    }

    protected abstract T toLocalIdResource(String key, R resource);

    protected abstract String getResourceAbout(R resource);

    @Override
    protected final String getAbout(T resource) {
        return this.getResourceAbout(resource.getResource());
    }

    @Override
    protected T toStorageResource(R entity) {
        return this.toLocalIdResource(Integer.toString(this.getResourceAbout(entity).hashCode()), entity);
    }

    @Override
    protected R unwrap(T resource) {
        return resource.getResource();
    }

}
