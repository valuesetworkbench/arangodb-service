package edu.mayo.cts2.framework.plugin.service.arangodb

import edu.mayo.cts2.framework.service.profile.MaintenanceService
import edu.mayo.cts2.framework.service.profile.ReadService
import org.junit.Test

import static org.junit.Assert.*

abstract class ReadTestBase extends DbClearingTest {

    abstract MaintenanceService getMaintenanceService()

    abstract ReadService getReadService()

    abstract def createResource()

    abstract def getReadIdByName()

    abstract def getReadIdByUri()

    @Test
    void readByNameNotNull() {
        maintenanceService.createResource(createResource())

        assertNotNull readService.read(getReadIdByName(), null)
    }

    @Test
    void readByUriNotNull() {
        maintenanceService.createResource(createResource())

        assertNotNull readService.read(getReadIdByUri(), null)
    }

    @Test
    void existsByNameNotNull() {
        maintenanceService.createResource(createResource())

        assertTrue readService.exists(getReadIdByName(), null)
    }

    @Test
    void existsByUriNotNull() {
        maintenanceService.createResource(createResource())

        assertTrue readService.exists(getReadIdByUri(), null)
    }
}
