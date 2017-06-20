package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion;

import edu.mayo.cts2.framework.model.core.SourceAndRoleReference;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;

public class IndexedMapVersion implements IndexDocument<MapVersion> {

    private String formalName;

    private String uri;

    private String map;

    private String mapAnalyzed;

    private String resourceSynopsis;

    private String owner;

    private String arangoDbId;

    private String mapVersionName;

    private String officialResourceVersionId;

    public IndexedMapVersion() {
        //
    }

    public IndexedMapVersion(String handle, MapVersion document) {
        this.arangoDbId = handle;

        if(document.getResourceSynopsis() != null &&
                document.getResourceSynopsis().getValue() != null) {
            this.resourceSynopsis = document.getResourceSynopsis().getValue().getContent();
        }

        if(document.getVersionOf() != null) {
            this.map = document.getVersionOf().getContent();
            this.mapAnalyzed = document.getVersionOf().getContent();
        }

        this.uri = document.getAbout();
        this.formalName = document.getFormalName();
        this.owner = getOwner(document);
        this.officialResourceVersionId = document.getOfficialResourceVersionId();
        this.mapVersionName = document.getMapVersionName();
    }

    private static String getOwner(MapVersion mapVersion) {
        for(SourceAndRoleReference sourceAndRoleReference : mapVersion.getSourceAndRole()) {
            sourceAndRoleReference.getRole().getContent().equals("owner");
            return sourceAndRoleReference.getSource().getContent();
        }

        return null;
    }

    public String getFormalName() {
        return formalName;
    }

    public void setFormalName(String formalName) {
        this.formalName = formalName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getResourceSynopsis() {
        return resourceSynopsis;
    }

    public void setResourceSynopsis(String resourceSynopsis) {
        this.resourceSynopsis = resourceSynopsis;
    }

    public String getArangoDbId() {
        return arangoDbId;
    }

    public void setArangoDbId(String arangoDbId) {
        this.arangoDbId = arangoDbId;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOfficialResourceVersionId() {
        return officialResourceVersionId;
    }

    public void setOfficialResourceVersionId(String officialResourceVersionId) {
        this.officialResourceVersionId = officialResourceVersionId;
    }

    public String getMapVersionName() {
        return mapVersionName;
    }

    public void setMapVersionName(String mapVersionName) {
        this.mapVersionName = mapVersionName;
    }

    public String getMapAnalyzed() {
        return mapAnalyzed;
    }

    @Override
    public String getId() {
        return this.getUri();
    }

}