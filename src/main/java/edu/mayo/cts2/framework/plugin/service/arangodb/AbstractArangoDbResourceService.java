package edu.mayo.cts2.framework.plugin.service.arangodb;

public abstract class AbstractArangoDbResourceService<R> extends AbstractArangoDbService {

    protected abstract StorageInfo<R> getStorageInfo();

    protected String getUriPath() {
        return "about";
    }

    protected String getResourcePath() {
        return null;
    }

    public abstract Class<R> getStorageClass();

    public String getArangoDbKey(R resource) {
        return this.getStorageInfo().getArangoDbKey(resource);
    }

}
