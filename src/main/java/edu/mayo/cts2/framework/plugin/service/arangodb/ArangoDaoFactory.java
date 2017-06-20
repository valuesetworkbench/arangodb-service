package edu.mayo.cts2.framework.plugin.service.arangodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.Map;

public class ArangoDaoFactory implements FactoryBean<ArangoDao> {

    private static final Logger log = LoggerFactory.getLogger(ArangoDaoFactory.class);

    private boolean hasBeenConfigured = false;

    private String database;
    private String databaseHost;
    private int databasePort;

    @Override
    public ArangoDao getObject() throws Exception {
        while(! this.hasBeenConfigured){
            this.log.warn("Waiting for the Configuration Service to start...");
            Thread.sleep(4000);
        }

        System.out.println("Starting Arango Dao...");
        ArangoDao arangoDao = new ArangoDao(this.database, this.databaseHost, this.databasePort);
        System.out.println("Done Starting Arango Dao...");

        return arangoDao;
    }

    public void updateCallback(Map<String,?> properties){
        this.database = this.getPropertyValue(properties, "database", "cts2");
        this.databaseHost = this.getPropertyValue(properties, "database.host", "localhost");
        this.databasePort = Integer.parseInt(this.getPropertyValue(properties, "database.port", "8529"));

        this.hasBeenConfigured = true;
    }

    private String getPropertyValue(Map<String,?> properties, String key, String defaultValue) {
        if(properties.containsKey(key)) {
            return properties.get(key).toString();
        } else {
            return defaultValue;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return ArangoDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
