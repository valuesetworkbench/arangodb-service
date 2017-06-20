package edu.mayo.cts2.framework.plugin.service.arangodb.update;

import edu.mayo.cts2.framework.model.core.types.ChangeType;
import edu.mayo.cts2.framework.model.service.core.types.StructuralProfile;

import java.util.Date;

public class Cts2Change {

    private ChangeType changeType;

    private Date resourceTimestamp;

    private String uri;

    private StructuralProfile resourceType;

    private String href;

    private String baseService;

    public Cts2Change() {
        //
    }

    public Cts2Change(ChangeType changeType, StructuralProfile resourceType, String uri, String href, String baseService, Date resourceTimestamp) {
        this.changeType = changeType;
        this.resourceType = resourceType;
        this.uri = uri;
        this.href = href;
        this.baseService = baseService;
        this.resourceTimestamp = resourceTimestamp;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public StructuralProfile getResourceType() {
        return resourceType;
    }

    public void setResourceType(StructuralProfile resourceType) {
        this.resourceType = resourceType;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getBaseService() {
        return baseService;
    }

    public void setBaseService(String baseService) {
        this.baseService = baseService;
    }

    public Date getResourceTimestamp() {
        return resourceTimestamp;
    }

    public void setResourceTimestamp(Date resourceTimestamp) {
        this.resourceTimestamp = resourceTimestamp;
    }
}
