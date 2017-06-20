package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.core.SourceReference;
import edu.mayo.cts2.framework.model.service.core.DocumentedNamespaceReference;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.service.profile.BaseService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractArangoDbService implements BaseService {

    @Resource
    private UrlConstructor urlConstructor;

    @Override
    public String getServiceName() {
        return "test";
    }

    @Override
    public OpaqueData getServiceDescription() {
        return ModelUtils.createOpaqueData("test");
    }

    @Override
    public String getServiceVersion() {
        return "1.0";
    }

    @Override
    public SourceReference getServiceProvider() {
        SourceReference reference = new SourceReference();
        reference.setContent("test");

        return reference;
    }

    @Override
    public List<DocumentedNamespaceReference> getKnownNamespaceList() {
        return Arrays.asList();
    }

    public UrlConstructor getUrlConstructor() {
        return urlConstructor;
    }

    public void setUrlConstructor(UrlConstructor urlConstructor) {
        this.urlConstructor = urlConstructor;
    }
}
