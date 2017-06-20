package edu.mayo.cts2.framework.plugin.service.arangodb

import edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion.IndexedValueSetDefinition
import org.elasticsearch.index.query.QueryBuilders
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery

import static org.junit.Assert.assertEquals

class ElasticsearchDaoTest extends DbClearingTest {

    @Autowired
    ElasticsearchDao elasticsearchDao

    @Test
    void testDelete() {
        def indexedValueSetDefinition = new IndexedValueSetDefinition(uri: "test")
        assertEquals 0, elasticsearchDao.elasticsearchOperations.queryForIds(new NativeSearchQuery(QueryBuilders.matchAllQuery())).size()

        elasticsearchDao.indexDocument("ValueSetDefinition", indexedValueSetDefinition)

        assertEquals 1, elasticsearchDao.elasticsearchOperations.queryForIds(new NativeSearchQuery(QueryBuilders.matchAllQuery())).size()

        elasticsearchDao.deleteDocument("test", "ValueSetDefinition")

        assertEquals 0, elasticsearchDao.elasticsearchOperations.queryForIds(new NativeSearchQuery(QueryBuilders.matchAllQuery())).size()
    }
}
