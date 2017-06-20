package edu.mayo.cts2.framework.plugin.service.arangodb
import org.apache.commons.io.FileUtils
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.Node

import static org.elasticsearch.node.NodeBuilder.nodeBuilder

public class EmbeddedElasticsearchServer {

    private static final String DEFAULT_DATA_DIRECTORY = "target/elasticsearch-data";

    private final Node node;
    private final String dataDirectory;

    public EmbeddedElasticsearchServer() {
        this(DEFAULT_DATA_DIRECTORY);
    }

    public EmbeddedElasticsearchServer(String dataDirectory) {
        this.dataDirectory = dataDirectory;

        def elasticsearchSettings = Settings.settingsBuilder()
                .put("http.enabled", "false")
                .put("path.home", dataDirectory);

        node = nodeBuilder()
                .clusterName("embedded")
                .local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }

    public Client getClient() {
        return node.client();
    }

    public void shutdown() {
        node.close();
        deleteDataDirectory();
    }

    private void deleteDataDirectory() {
        try {
            FileUtils.deleteDirectory(new File(dataDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
        }
    }
}
