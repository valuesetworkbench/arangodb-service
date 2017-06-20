package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

public class ElasticsearchDao implements InitializingBean {

    @Value("${index:cts2}")
    private String index;

    @Value("${index.host:localhost}")
    private String indexHost = "localhost";

    @Value("${index.port:9300}")
    private Integer indexPort = 9300;

    @Value("${index.clustername:elasticsearch}")
    private String indexClustename = "elasticsearch";

    private ElasticsearchOperations elasticsearchOperations;

    private Client esClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.esClient = this.createClient();
        this.elasticsearchOperations = this.createElasticsearchOperations(this.esClient);

        if (!this.elasticsearchOperations.indexExists(this.index)) {
            this.elasticsearchOperations.createIndex(this.index);
        }
    }

    protected Client createClient() throws UnknownHostException {
        return new TransportClient.Builder().settings(Settings.settingsBuilder().put("cluster.name", this.indexClustename)).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(this.indexHost), this.indexPort));
    }

    protected ElasticsearchOperations createElasticsearchOperations(Client client) {
        return new ElasticsearchTemplate(client);
    }

    public List<String> queryForIds(SearchQuery query) {
        final List<String> ids = Lists.newArrayList();
        this.elasticsearchOperations.query(query, new ResultsExtractor<Boolean>() {
            @Override
            public Boolean extract(SearchResponse response) {
                for (SearchHit hit : response.getHits().getHits()) {
                    ids.add(hit.getId());
                }

                return true;
            }
        });

        return ids;
    }

    public void deleteDocument(String uri, String type) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(QueryBuilders.termQuery("uri", uri));
        deleteQuery.setType(type);
        deleteQuery.setIndex(this.index);

        this.elasticsearchOperations.delete(deleteQuery);
        this.elasticsearchOperations.refresh(this.index);
    }

    public <T> List<T> query(SearchQuery query, Class<T> clazz) {
        query.addIndices(this.index);

        return this.elasticsearchOperations.queryForPage(query, clazz).getContent();
    }

    public void indexDocument(String type, IndexDocument<?> document) {
        try {
            this.esClient.index(Requests.indexRequest(this.index).refresh(true).source(this.objectMapper.writeValueAsBytes(document)).type(type).id(document.getId())).actionGet();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void indexDocuments(String type, Collection<IndexDocument<?>> documents) {
        BulkRequest request = Requests.bulkRequest().refresh(true);

        try {
            for(IndexDocument<?> document : documents) {
                request = request.add(Requests.indexRequest(this.index).source(this.objectMapper.writeValueAsBytes(document)).type(type).id(document.getId()));
            }

            this.esClient.bulk(request).actionGet();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getIndex() {
        return index;
    }

    public ElasticsearchOperations getElasticsearchOperations() {
        return elasticsearchOperations;
    }

}