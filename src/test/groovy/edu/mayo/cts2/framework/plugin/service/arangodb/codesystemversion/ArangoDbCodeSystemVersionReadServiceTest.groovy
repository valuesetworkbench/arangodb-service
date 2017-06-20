package edu.mayo.cts2.framework.plugin.service.arangodb.codesystemversion
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry
import edu.mayo.cts2.framework.model.core.CodeSystemReference
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.arangodb.ReadTestBase
import edu.mayo.cts2.framework.service.profile.MaintenanceService
import edu.mayo.cts2.framework.service.profile.ReadService

import javax.annotation.Resource

class ArangoDbCodeSystemVersionReadServiceTest extends ReadTestBase {

    @Resource(type=ArangoDbCodeSystemVersionReadService)
    ReadService readService

    @Resource(type=ArangoDbCodeSystemVersionMaintenanceService)
    MaintenanceService maintenanceService

    @Override
    def createResource() {
        new CodeSystemVersionCatalogEntry(about: "http://test", codeSystemVersionName: "foo", versionOf: new CodeSystemReference(content: "test"))
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
