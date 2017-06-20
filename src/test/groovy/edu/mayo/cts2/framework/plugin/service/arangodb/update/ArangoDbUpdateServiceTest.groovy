package edu.mayo.cts2.framework.plugin.service.arangodb.update
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.updates.ChangeSet
import edu.mayo.cts2.framework.model.updates.ChangeableResource
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.plugin.service.arangodb.codesystem.ArangoDbCodeSystemReadService
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertNotNull

class ArangoDbUpdateServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbUpdateService service

    @Autowired
    ArangoDbCodeSystemReadService codeSystemReadService

    @Test
    void testImport() {
        def codeSystem = new CodeSystemCatalogEntry(about: "http://test", codeSystemName: "foo")

        def changeSet = new ChangeSet(member: [new ChangeableResource(codeSystem: codeSystem)] )

        service.importChangeSet(changeSet)

        assertNotNull codeSystemReadService.read(new NameOrURI(name: "foo"), null);
    }
}
