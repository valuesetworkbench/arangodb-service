package edu.mayo.cts2.framework.plugin.service.arangodb;

public interface GraphInfo {

    String getEdgeCollection();

    String getFromCollection();

    String getToCollection();

    String getName();
}
