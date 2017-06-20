package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion

import edu.mayo.cts2.framework.model.core.ChangeDescription
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup
import edu.mayo.cts2.framework.model.core.MapReference
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.mapversion.MapVersion
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ArangoDbMapVersionHistoryServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbMapVersionReadService service

    @Autowired
    ArangoDbMapVersionMaintenanceService maintenanceService

    @Test
    void testGetHistory() {
        def old = new MapVersion(state: FinalizableState.OPEN, formalName: "old", about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"), changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))
        def middle = new MapVersion(state: FinalizableState.OPEN, formalName: "middle", about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"), changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2005, 1, 1, 1, 1).toDate())))
        def latest = new MapVersion(state: FinalizableState.OPEN, formalName: "latest", about: "http://test", mapVersionName: "foo", versionOf: new MapReference(content: "test"), changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2010, 1, 1, 1, 1).toDate())))

        maintenanceService.createResource(old)
        maintenanceService.updateResource(middle)
        maintenanceService.updateResource(latest)

        def resource = service.getChangeHistoryFor(new NameOrURI(name: "foo"))

        assertEquals 2, resource.entries.size()

        assertEquals "old", resource.entries.get(0).formalName
        assertEquals "middle", resource.entries.get(1).formalName
    }

    @Test
    @Ignore("The CTS2 Framework should populate this as a precondition.")
    void testGetHistoryHasChangeDate() {
        def old = new MapVersion(state: FinalizableState.OPEN, formalName: "old", about: "http://test", mapVersionName: "foo", changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription()))
        def middle = new MapVersion(formalName: "middle", about: "http://test", mapVersionName: "foo", changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription()))
        def latest = new MapVersion(formalName: "latest", about: "http://test", mapVersionName: "foo", changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription()))

        maintenanceService.createResource(old)
        maintenanceService.updateResource(middle)
        maintenanceService.updateResource(latest)

        def resource = service.getChangeHistoryFor(new NameOrURI(name: "foo"))

        assertEquals 3, resource.entries.size()

        resource.entries.each {
            assertNotNull it.changeableElementGroup.changeDescription.changeDate
        }
    }

}
