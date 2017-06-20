package edu.mayo.cts2.framework.plugin.service.arangodb.codesystemversion

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry
import edu.mayo.cts2.framework.model.core.CodeSystemReference
import edu.mayo.cts2.framework.plugin.service.arangodb.QueryTestBase
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService
import edu.mayo.cts2.framework.service.profile.QueryService

import javax.annotation.Resource

class ArangoDbCodeSystemVersionQueryServiceTest extends QueryTestBase {

    @Resource(type=ArangoDbCodeSystemVersionQueryService)
    QueryService queryService

    @Resource(type=ArangoDbCodeSystemVersionMaintenanceService)
    BaseMaintenanceService maintenanceService

    @Override
    def createResource() {
        new CodeSystemVersionCatalogEntry(about: "http://test", codeSystemVersionName: "foo", versionOf: new CodeSystemReference(content: "test"))
    }

}
