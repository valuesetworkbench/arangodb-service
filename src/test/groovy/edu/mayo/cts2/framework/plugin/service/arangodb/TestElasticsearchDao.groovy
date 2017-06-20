package edu.mayo.cts2.framework.plugin.service.arangodb

import org.elasticsearch.client.Client
import org.springframework.beans.factory.DisposableBean

class TestElasticsearchDao extends ElasticsearchDao implements DisposableBean {

    def embeddedElasticsearchServer = new EmbeddedElasticsearchServer();

    @Override
    protected Client createClient() throws UnknownHostException {
        embeddedElasticsearchServer.client
    }

    @Override
    void destroy() throws Exception {
        embeddedElasticsearchServer.shutdown()
    }

}
