package edu.mayo.cts2.framework.plugin.service.arangodb

import org.elasticsearch.index.query.MatchAllQueryBuilder
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.query.DeleteQuery
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-arangodb-service-context.xml")
@ActiveProfiles("test")
abstract class DbClearingTest {

    @Autowired
    ArangoDao arangoDao

    @Autowired
    ElasticsearchDao elasticsearchDao

    @Autowired
    List<StorageInfo> storageInfos = []

    @Before
    public void before() {
        clear()
    }

    @After
    public void after() {
        clear()
    }

    void clear(){
        def collections = this.arangoDao.getDriver().getCollections(true).getNames().keySet()

        collections.each {
            this.arangoDao.getDriver().truncateCollection(it);
        }

        elasticsearchDao.elasticsearchOperations.refresh(elasticsearchDao.getIndex())

        storageInfos.each { si ->
            si.elasticSearchIndexSpecifications.each { spec ->
                elasticsearchDao.elasticsearchOperations.delete(new DeleteQuery(index: elasticsearchDao.getIndex(), type: spec.typeName, query: new MatchAllQueryBuilder()))
            }
        }
        elasticsearchDao.elasticsearchOperations.refresh(elasticsearchDao.getIndex())
    }

}
