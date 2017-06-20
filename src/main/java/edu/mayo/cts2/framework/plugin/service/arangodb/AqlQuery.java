package edu.mayo.cts2.framework.plugin.service.arangodb;

import java.util.Map;

public class AqlQuery {

    private String aql;

    private Map<String,Object> parameters;

    public AqlQuery(String aql, Map<String, Object> parameters) {
        this.aql = aql;
        this.parameters = parameters;
    }

    public String getAql() {
        return aql;
    }

    public void setAql(String aql) {
        this.aql = aql;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

}
