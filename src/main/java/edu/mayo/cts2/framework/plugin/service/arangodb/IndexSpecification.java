package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.entity.IndexType;

import java.util.Arrays;

public class IndexSpecification {

    private boolean unique;

    private IndexType indexType;

    private String[] fields;

    public IndexSpecification(boolean unique, IndexType indexType, String[] fields) {
        this.unique = unique;
        this.indexType = indexType;
        this.fields = fields;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "IndexSpecification{" +
                "unique=" + unique +
                ", indexType=" + indexType +
                ", fields=" + Arrays.toString(fields) +
                '}';
    }
}
