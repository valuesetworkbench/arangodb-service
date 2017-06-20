package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public abstract class AbstractArangoDbNameOrUriReadService<R> extends AbstractArangoDbDefaultReadService<R,NameOrURI> {

    @Override
    protected boolean isUriQuery(NameOrURI identifier) {
        return StringUtils.isNotBlank(identifier.getUri());
    }

    @Override
    protected String getUri(NameOrURI identifier) {
        return identifier.getUri();
    }

    @Override
    protected AqlQuery getNameFilter(NameOrURI identifier) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("name", identifier.getName());

        return new AqlQuery("FILTER x." + this.getNamePath() + " == @name ", params);
    }

    protected abstract String getNamePath();

}
