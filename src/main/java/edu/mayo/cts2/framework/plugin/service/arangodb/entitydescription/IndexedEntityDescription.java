package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription;

import com.google.common.collect.Lists;
import edu.mayo.cts2.framework.model.entity.Designation;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;

import java.util.List;

public class IndexedEntityDescription implements IndexDocument<EntityDescription> {

    private String name;

    private String namespace;

    private String uri;

    private String codeSystemVersion;

    private List<String> designations;

    private String arangoDbId;

    public IndexedEntityDescription() {
        //
    }

    public IndexedEntityDescription(String handle, EntityDescription document) {
        NamedEntityDescription namedEntity = document.getNamedEntity();

        this.arangoDbId = handle;
        List<String> text = Lists.newArrayList();

        for(Designation designation : namedEntity.getDesignation()) {
            text.add(designation.getValue().getContent());
        }

        this.designations = text;

        this.uri = namedEntity.getAbout();
        this.name = namedEntity.getEntityID().getName();
        this.namespace = namedEntity.getEntityID().getNamespace();
        this.codeSystemVersion = namedEntity.getDescribingCodeSystemVersion().getVersion().getContent();
    }

    public List<String> getDesignations() {
        return designations;
    }

    public void setDesignations(List<String> designations) {
        this.designations = designations;
    }

    @Override
    public String getArangoDbId() {
        return arangoDbId;
    }

    public void setArangoDbId(String arangoDbId) {
        this.arangoDbId = arangoDbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public void setCodeSystemVersion(String codeSystemVersion) {
        this.codeSystemVersion = codeSystemVersion;
    }

    @Override
    public String getId() {
        return this.getUri() + this.getCodeSystemVersion();
    }
}