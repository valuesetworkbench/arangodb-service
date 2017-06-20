package edu.mayo.cts2.framework.plugin.service.arangodb;

public abstract class AbstractStorageInfo<R> implements StorageInfo<R> {

    @Override
    public final String getArangoDbKey(R resource) {
        return this.hash(this.doGetArangoDbKey(resource));
    }


    protected String hash(String key) {
        return Integer.toString(key.hashCode());
    }

    protected abstract String doGetArangoDbKey(R resource);

}
