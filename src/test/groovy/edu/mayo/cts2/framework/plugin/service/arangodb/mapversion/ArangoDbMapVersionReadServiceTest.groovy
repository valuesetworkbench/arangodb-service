package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.ChangeDescription
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup
import edu.mayo.cts2.framework.model.core.MapReference
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.mapversion.MapVersion
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.arangodb.ReadTestBase
import edu.mayo.cts2.framework.service.profile.MaintenanceService
import edu.mayo.cts2.framework.service.profile.ReadService
import org.joda.time.DateTime
import org.junit.Test

import javax.annotation.Resource

import static org.junit.Assert.assertNotNull

class ArangoDbMapVersionReadServiceTest extends ReadTestBase {

    @Resource(type=ArangoDbMapVersionReadService)
    ReadService readService

    @Resource(type=ArangoDbMapVersionMaintenanceService)
    MaintenanceService maintenanceService

    @Override
    def createResource() {
        new MapVersion(about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"))
    }

    @Override
    def getReadIdByName() {
        new NameOrURI(name: "foo")
    }

    @Override
    def getReadIdByUri() {
        new NameOrURI(uri: "http://test")
    }

    @Test
    void testCreateAndReadByUri() {
        maintenanceService.createResource(new MapVersion(about: "http://test", versionOf: new MapReference(content: "test")))

        assertNotNull readService.read(new NameOrURI(uri: "http://test"), null)
    }

    @Test
    void testCreateAndReadByName() {
        maintenanceService.createResource(new MapVersion(about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test")))

        assertNotNull readService.read(new NameOrURI(name: "foo"), null)
    }

    @Test
    void testCreateAndReadVersionByName() {
        def old = new MapVersion(state: FinalizableState.OPEN, about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"), changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))
        def middle = new MapVersion(state: FinalizableState.OPEN, about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"),changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2005, 1, 1, 1, 1).toDate())))
        def latest = new MapVersion(state: FinalizableState.OPEN, about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"), changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2010, 1, 1, 1, 1).toDate())))

        maintenanceService.createResource(old)
        maintenanceService.updateResource(middle)
        maintenanceService.updateResource(latest)

        def resource = readService.read(new NameOrURI(name: "foo"), new ResolvedReadContext(referenceTime: new DateTime(2040, 1, 1, 1, 1).toDate()))

        assertNotNull resource
    }

}
