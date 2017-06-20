package edu.mayo.cts2.framework.plugin.service.arangodb;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Component
public class ElasticsearchInitializer implements InitializingBean {

    private static Logger log = LoggerFactory.getLogger(ArangoDao.class);

    @Resource
    private Set<StorageInfo> storageInfos;

    @Resource
    private ElasticsearchDao elasticsearchDao;

    public ElasticsearchInitializer() {}

    public ElasticsearchInitializer(ElasticsearchDao elasticsearchDao, Set<StorageInfo> storageInfos){
        super();
        this.elasticsearchDao = elasticsearchDao;
        this.storageInfos = new HashSet<StorageInfo>(storageInfos);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initialize();
    }

    public void initialize() throws Exception {
        for(StorageInfo<?> storageInfo : this.storageInfos) {
            for(ElasticSearchIndexSpecification elasticSearchIndexSpecification : storageInfo.getElasticSearchIndexSpecifications()) {
                String index = this.elasticsearchDao.getIndex();
                String type = elasticSearchIndexSpecification.getTypeName();

                if(! this.elasticsearchDao.getElasticsearchOperations().typeExists(index, type)) {
                    log.info("Creating missing elasticsearch mapping for Collection " + type);
                    this.elasticsearchDao.getElasticsearchOperations().putMapping(
                            index,
                            type,
                            IOUtils.toString(
                                    new ClassPathResource(elasticSearchIndexSpecification.getMappingFile()).getInputStream()));
                }
            }
        }
    }

}
