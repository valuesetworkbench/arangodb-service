package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry
import edu.mayo.cts2.framework.plugin.service.arangodb.QueryTestBase
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService
import edu.mayo.cts2.framework.service.profile.QueryService

import javax.annotation.Resource

class ArangoDbCodeSystemQueryServiceTest extends QueryTestBase {

    @Resource(type=ArangoDbCodeSystemQueryService)
    QueryService queryService

    @Resource(type=ArangoDbCodeSystemMaintenanceService)
    BaseMaintenanceService maintenanceService

    @Override
    def createResource() {
        new CodeSystemCatalogEntry(about: "http://test", codeSystemName: "foo")
    }

}
