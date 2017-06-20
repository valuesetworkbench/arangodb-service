package edu.mayo.cts2.framework.plugin.service.arangodb;

public class ElasticSearchIndexSpecification {

    private String typeName;

    private String mappingFile;

    public ElasticSearchIndexSpecification(String typeName, String mappingFile) {
        this.typeName = typeName;
        this.mappingFile = mappingFile;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getMappingFile() {
        return mappingFile;
    }
}
