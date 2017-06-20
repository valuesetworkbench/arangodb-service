package edu.mayo.cts2.framework.plugin.service.arangodb.valueset

import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.plugin.service.arangodb.ReadTestBase
import edu.mayo.cts2.framework.service.profile.MaintenanceService
import edu.mayo.cts2.framework.service.profile.ReadService

import javax.annotation.Resource

class ArangoDbValueSetReadServiceTest extends ReadTestBase {

    @Resource(type=ArangoDbValueSetReadService)
    ReadService readService

    @Resource(type=ArangoDbValueSetMaintenanceService)
    MaintenanceService maintenanceService

    @Override
    def createResource() {
        new ValueSetCatalogEntry(about: "http://test", valueSetName: "foo")
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
