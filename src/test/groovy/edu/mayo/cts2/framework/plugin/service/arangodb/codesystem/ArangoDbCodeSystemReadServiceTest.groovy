package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.arangodb.ReadTestBase
import edu.mayo.cts2.framework.service.profile.MaintenanceService
import edu.mayo.cts2.framework.service.profile.ReadService

import javax.annotation.Resource

class ArangoDbCodeSystemReadServiceTest extends ReadTestBase {

    @Resource(type=ArangoDbCodeSystemReadService)
    ReadService readService

    @Resource(type=ArangoDbCodeSystemMaintenanceService)
    MaintenanceService maintenanceService

    @Override
    def createResource() {
        new CodeSystemCatalogEntry(about: "http://test", codeSystemName: "foo")
    }

    @Override
    def getReadIdByName() {
        new NameOrURI(name: "foo")
    }

    @Override
    def getReadIdByUri() {
        new NameOrURI(uri: "http://test")
    }

}
