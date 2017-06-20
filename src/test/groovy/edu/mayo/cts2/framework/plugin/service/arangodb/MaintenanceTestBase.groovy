package edu.mayo.cts2.framework.plugin.service.arangodb

import edu.mayo.cts2.framework.model.service.exception.EntityAlreadyExists
import edu.mayo.cts2.framework.service.profile.QueryService
import org.junit.Test

import static org.junit.Assert.assertEquals

abstract class MaintenanceTestBase extends DbClearingTest {

    abstract getMaintenanceService()

    abstract QueryService getQueryService()

    abstract def createResources(number)

    @Test
    void importDocuments() {
        maintenanceService.importResources(createResources(2))

        assertEquals 2, getQueryService().getResourceSummaries(null,null,null).entries.size()
    }

    @Test(expected = EntityAlreadyExists)
    void testErrorOnDuplicateCreate() {
        maintenanceService.createResource(createResources(1))
        maintenanceService.createResource(createResources(1))
    }

}
