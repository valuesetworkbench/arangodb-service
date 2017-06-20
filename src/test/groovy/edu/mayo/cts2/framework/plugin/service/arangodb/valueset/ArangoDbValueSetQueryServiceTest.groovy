package edu.mayo.cts2.framework.plugin.service.arangodb.valueset
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.plugin.service.arangodb.QueryTestBase
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService
import edu.mayo.cts2.framework.service.profile.QueryService

import javax.annotation.Resource

class ArangoDbValueSetQueryServiceTest extends QueryTestBase {

    @Resource(type=ArangoDbValueSetQueryService)
    QueryService queryService

    @Resource(type=ArangoDbValueSetMaintenanceService)
    BaseMaintenanceService maintenanceService

    @Override
    def createResource() {
        new ValueSetCatalogEntry(about: "http://test", valueSetName: "foo")
    }

}
