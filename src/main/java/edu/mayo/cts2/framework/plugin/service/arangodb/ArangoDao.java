package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.ArangoHost;
import com.arangodb.DocumentCursor;
import com.arangodb.VertexCursor;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Sets;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;
import edu.mayo.cts2.framework.model.service.exception.EntityAlreadyExists;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArangoDao implements InitializingBean {

    private static Logger log = LoggerFactory.getLogger(ArangoDao.class);

    private ArangoDriver driver;

    @Value("${database:cts2}")
    private String database;

    @Value("${database.host:localhost}")
    private String databaseHost = "localhost";

    @Value("${database.port:8529}")
    private Integer databasePort = 8529;

    @Autowired(required = false)
    private Set<TypeAdapterProvider> typeAdapterProviders = Sets.newHashSet();

    public ArangoDao() {}

    public ArangoDao(String database, String host, int port, Set<StorageInfo> storageInfo, Set<GraphInfo> graphInfo) {
        super();
        this.database = database;
        this.databaseHost = host;
        this.databasePort = port;

        this.typeAdapterProviders = Sets.newHashSet();
        for(StorageInfo info : storageInfo) {
            typeAdapterProviders.addAll(info.getTypeAdapters());
        }

        this.afterPropertiesSet();

        try {
            new ArangoInitializer(this, storageInfo, graphInfo).initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing ArangoDao.", e);
        }
    }

    public ArangoDao(String database, String host, int port) {
        this(database, host, port, new HashSet<StorageInfo>(), new HashSet<GraphInfo>());
    }

    @Override
    public void afterPropertiesSet() {
        this.driver = this.createArangoDriver();

        GsonBuilder builder = EntityFactory.getGsonBuilder().setFieldNamingStrategy(new FieldNamingStrategy(){

            @Override
            public String translateName(Field field) {
                String fieldName = field.getName();

                char[] array = fieldName.toCharArray();

                if(array[0] == '_'){
                    array = ArrayUtils.remove(array, 0);
                }

                return new String(array);
            }

        });

        for(TypeAdapterProvider<?> typeAdapterProvider : this.typeAdapterProviders) {
            builder = builder.registerTypeAdapter(typeAdapterProvider.getType(), typeAdapterProvider.getTypeAdapter());
        }

        builder = builder.addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getName().equals("_choiceValue");
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }

        });

        EntityFactory.configure(builder);
    }

    protected ArangoDriver createArangoDriver() {
        ArangoConfigure configure = new ArangoConfigure();
        configure.setArangoHost(new ArangoHost(this.databaseHost, this.databasePort));
        configure.setDefaultDatabase(this.database);
        configure.init();

        ArangoDriver driver = new ArangoDriver(configure);

        boolean ready = false;

        while(! ready) {
            try {
                driver.getTime();
                ready = true;
            } catch (Throwable e) {
                try {
                    System.out.println("Waiting for ArangoDB connection...");
                    e.printStackTrace();
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {}
            }
        }

        return driver;
    }

    public <T> DocumentEntity<T> writeDocument(String collection, String key, T object) {
        try {
            return (DocumentEntity<T>) this.driver.createDocument(collection, key, object, false, true);
        } catch (ArangoException e) {
            if(e.getErrorNumber() == 1210) {
                throw new EntityAlreadyExists();
            }
            throw new RuntimeException(e);
        }
    }

    public <T> EdgeEntity<T> writeEdge(String collection, T object, String from, String to) {
        try {
            EdgeEntity entity = this.driver.createEdge(collection, object, from, to, false, true);
            entity.setEntity(object);

            return entity;
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> VertexCursor<T> query(String query, Map<String,Object> vars, Class<T> clazz) {
        try {
            log.debug(query);

            return this.driver.executeVertexQuery(query, vars, null, clazz);
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> VertexEntity<T> readVertex(String id, String collection, Class<T> clazz) {
        try {
            return this.driver.graphGetVertex("g", collection, id, clazz);
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> DocumentEntity<T> readDocumentById(String id, String collection, Class<T> clazz) {
        try {
            return this.driver.getDocument(collection, id, clazz);
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> DocumentEntity<T> readDocument(String aql, Map<String,Object> variables, Class<T> clazz) {
        DocumentCursor<T> cursor = null;
        try {
            cursor = this.driver.executeDocumentQuery(aql, variables, null, clazz);

            return cursor.getUniqueResult();
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        } finally {
            if(cursor != null) {
                try {
                    cursor.close();
                } catch (ArangoException e) {
                    log.warn(e.getErrorMessage(), e);
                }
            }
        }
    }

    public ArangoDriver getDriver() {
        return driver;
    }

}
