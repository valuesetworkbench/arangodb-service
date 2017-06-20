package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import edu.mayo.cts2.framework.model.core.SourceAndRoleReference;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;

public class IndexedValueSetDefinition implements IndexDocument<ValueSetDefinition> {

    private String formalName;

    private String uri;

    private String valueSet;

    private String valueSetAnalyzed;

    private String resourceSynopsis;

    private String owner;

    private String arangoDbId;

    private String officialResourceVersionId;

    public IndexedValueSetDefinition() {
        //
    }

    public IndexedValueSetDefinition(String handle, LocalIdValueSetDefinition document) {
        ValueSetDefinition valueSetDefinition = document.getResource();

        this.arangoDbId = handle;

        if(valueSetDefinition.getResourceSynopsis() != null &&
                valueSetDefinition.getResourceSynopsis().getValue() != null) {
            this.resourceSynopsis = valueSetDefinition.getResourceSynopsis().getValue().getContent();
        }

        if(valueSetDefinition.getDefinedValueSet() != null) {
            this.valueSet = valueSetDefinition.getDefinedValueSet().getContent();
            this.valueSetAnalyzed = valueSetDefinition.getDefinedValueSet().getContent();
        }

        this.uri = valueSetDefinition.getAbout();
        this.formalName = valueSetDefinition.getFormalName();
        this.officialResourceVersionId = valueSetDefinition.getOfficialResourceVersionId();

        this.owner = getOwner(valueSetDefinition);
    }

    private static String getOwner(ValueSetDefinition valueSetDefinition) {
        for(SourceAndRoleReference sourceAndRoleReference : valueSetDefinition.getSourceAndRole()) {
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

    public String getValueSet() {
        return valueSet;
    }

    public void setValueSet(String valueSet) {
        this.valueSet = valueSet;
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

    public String getValueSetAnalyzed() {
        return valueSetAnalyzed;
    }

    @Override
    public String getId() {
        return this.uri;
    }
}