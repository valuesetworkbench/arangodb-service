package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.IndexEntity;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class ArangoInitializer implements InitializingBean {

    private static Logger log = LoggerFactory.getLogger(ArangoDao.class);

    @Resource
    private Set<StorageInfo> storageInfos;

    @Resource
    private Set<GraphInfo> graphInfos;

    @Resource
    private ArangoDao arangoDao;

    public ArangoInitializer() {}

    public ArangoInitializer(ArangoDao arangoDao, Set<StorageInfo> storageInfos, Set<GraphInfo> graphInfos){
        super();
        this.arangoDao = arangoDao;
        this.storageInfos = new HashSet<StorageInfo>(storageInfos);
        this.graphInfos = new HashSet<GraphInfo>(graphInfos);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initialize();
    }

    public void initialize() throws Exception {
        String defaultDatabase = this.arangoDao.getDriver().getDefaultDatabase();
        if(! this.arangoDao.getDriver().getDatabases().getResult().contains(defaultDatabase)) {
            this.arangoDao.getDriver().createDatabase(defaultDatabase);
        }

        Set<String> collections = this.arangoDao.getDriver().getCollections(true).getNames().keySet();

        for(StorageInfo<?>  storageInfo : this.storageInfos) {
            for(String collection : Sets.newHashSet(storageInfo.getCollection(), storageInfo.getCollection() + ArangoDbServiceConstants.HISTORY_COLLECTION_SUFFIX)) {
                if (!collections.contains(collection)) {
                    CollectionType type;
                    switch (storageInfo.getStorageType()) {
                        case DOCUMENT:
                            type = CollectionType.DOCUMENT;
                            break;
                        case EDGE:
                            type = CollectionType.EDGE;
                            break;
                        default:
                            throw new IllegalStateException("Can't handle: " + storageInfo.getStorageType() + " collection types.");
                    }

                    this.arangoDao.getDriver().createCollection(collection, new CollectionOptions().setType(type));
                }

                for (IndexSpecification indexSpecification : storageInfo.getIndexSpecifications()) {
                    boolean found = false;
                    for (IndexEntity index : this.arangoDao.getDriver().getIndexes(collection).getIndexes()) {
                        if (new HashSet<String>(Arrays.asList(indexSpecification.getFields())).
                                equals(new HashSet<String>(index.getFields()))) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        log.info("Creating missing index for Collection " + collection + ", " + indexSpecification.toString());
                        this.arangoDao.getDriver().createIndex(collection, indexSpecification.getIndexType(), indexSpecification.isUnique(), indexSpecification.getFields());
                    }
                }
            }
        }

        Set<String> graphs = new HashSet<String>(this.arangoDao.getDriver().getGraphList());

        if(this.graphInfos != null) {
            for (GraphInfo graphInfo : this.graphInfos) {
                if (!graphs.contains(graphInfo.getName())) {
                    EdgeDefinitionEntity edgeDefinitionEntity = new EdgeDefinitionEntity();
                    edgeDefinitionEntity.setCollection(graphInfo.getEdgeCollection());
                    edgeDefinitionEntity.setFrom(Arrays.asList(graphInfo.getFromCollection()));
                    edgeDefinitionEntity.setTo(Arrays.asList(graphInfo.getToCollection()));

                    this.arangoDao.getDriver().createGraph(graphInfo.getName(), Arrays.asList(edgeDefinitionEntity), new ArrayList<String>(), true);
                }
            }
        }

    }

}
