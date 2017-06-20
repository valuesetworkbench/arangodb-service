package edu.mayo.cts2.framework.plugin.service.arangodb

import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService
import edu.mayo.cts2.framework.service.profile.QueryService
import org.junit.Test

import static org.junit.Assert.assertEquals

abstract class QueryTestBase extends DbClearingTest {

    abstract BaseMaintenanceService getMaintenanceService()

    abstract QueryService getQueryService()

    abstract def createResource()

    @Test
    void queryAll() {
        maintenanceService.createResource(createResource())

        assertEquals 1, queryService.getResourceSummaries(null, null, new Page()).entries.size()
    }

    @Test
    void queryWithZero() {
        maintenanceService.createResource(createResource())

        assertEquals 0, queryService.getResourceSummaries(null, null, new Page(maxtoreturn: 0)).entries.size()
    }

    @Test
    void countAll() {
        maintenanceService.createResource(createResource())

        assertEquals 1, queryService.count(null)
    }
}
