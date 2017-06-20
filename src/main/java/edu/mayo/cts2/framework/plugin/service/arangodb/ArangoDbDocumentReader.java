package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.entity.DocumentEntity;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;

public interface ArangoDbDocumentReader<R,I> {

    DocumentEntity<R> readDocument(I identifier, ResolvedReadContext resolvedReadContext);

    String getDocumentHandle(I identifier, ResolvedReadContext resolvedReadContext);

}
