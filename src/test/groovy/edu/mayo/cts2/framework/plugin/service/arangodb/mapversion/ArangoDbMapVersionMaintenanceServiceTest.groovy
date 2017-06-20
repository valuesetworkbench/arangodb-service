package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.mapversion.MapEntry
import edu.mayo.cts2.framework.model.mapversion.MapVersion
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.service.exception.ResourceIsNotOpen
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.plugin.service.arangodb.mapentry.ArangoDbMapEntryMaintenanceService
import edu.mayo.cts2.framework.plugin.service.arangodb.mapentry.ArangoDbMapEntryQueryService
import edu.mayo.cts2.framework.service.command.restriction.MapEntryQueryServiceRestrictions
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryQuery
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test

import javax.annotation.Resource

import static org.junit.Assert.assertEquals

class ArangoDbMapVersionMaintenanceServiceTest extends DbClearingTest {

    @Resource(type=ArangoDbMapVersionQueryService)
    ArangoDbMapVersionQueryService queryService

    @Resource(type=ArangoDbMapVersionMaintenanceService)
    ArangoDbMapVersionMaintenanceService maintenanceService

    @Resource(type=ArangoDbMapEntryMaintenanceService)
    ArangoDbMapEntryMaintenanceService mapEntryMaintenanceService

    @Resource(type=ArangoDbMapEntryQueryService)
    ArangoDbMapEntryQueryService mapEntryQueryService

    @Test
    void testClone() {
        def mapVersion = new MapVersion(
                about: "http://test1",
                mapVersionName: "test1",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))

        def toClonemapVersion = new MapVersion(
                about: "http://test2",
                mapVersionName: "test2",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))

        maintenanceService.createResource(mapVersion)

        def mapEntry = new MapEntry(
                assertedBy: new MapVersionReference(
                        map: new MapReference(content: "testOne"),
                        mapVersion:  new NameAndMeaningReference(content: "test1")),
                mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns"))

        mapEntryMaintenanceService.createResource(mapEntry)

        maintenanceService.cloneResource(new NameOrURI(name: "test1"), toClonemapVersion)

        ["test1", "test2"].each {
            def restriction = new MapEntryQueryServiceRestrictions(mapVersion: new NameOrURI(name: it))

            def query = [
                    getRestrictions   : {
                        restriction
                    },

                    getQuery          : {
                        null
                    },

                    getFilterComponent: {
                        [] as Set
                    },

                    getReadContext    : {
                        null
                    }
            ] as MapEntryQuery

            assertEquals 1, mapEntryQueryService.getResourceSummaries(query, null, null).entries.size()
        }
    }

    @Test
    void testDeleteDependencies() {
        def mapVersion = new MapVersion(
                about: "http://test1",
                mapVersionName: "test1",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))

        maintenanceService.createResource(mapVersion)

        def mapEntry = new MapEntry(
                assertedBy: new MapVersionReference(
                        map: new MapReference(content: "testOne"),
                        mapVersion:  new NameAndMeaningReference(content: "test1")),
                mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns"))

        mapEntryMaintenanceService.createResource(mapEntry)

        maintenanceService.deleteResource(new NameOrURI(name: "test1"), "test")

        def restriction = new MapEntryQueryServiceRestrictions(mapVersion: new NameOrURI(name: "test1"))

        def query = [
                getRestrictions   : {
                    restriction
                },

                getQuery          : {
                    null
                },

                getFilterComponent: {
                    [] as Set
                },

                getReadContext    : {
                    null
                }
        ] as MapEntryQuery

        assertEquals 0, mapEntryQueryService.getResourceSummaries(query, null, null).entries.size()
    }

    @Test
    void testDeleteMultipleDependencies() {
        def mapVersion = new MapVersion(
                about: "http://test1",
                mapVersionName: "test1",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))

        maintenanceService.createResource(mapVersion)

        [1..100].each {
            def mapEntry = new MapEntry(
                    assertedBy: new MapVersionReference(
                            map: new MapReference(content: "testOne"),
                            mapVersion: new NameAndMeaningReference(content: "test1")),
                    mapFrom: new URIAndEntityName(uri: "http://foo/$it", name: "$it", namespace: "ns"))

            mapEntryMaintenanceService.createResource(mapEntry)
        }

        maintenanceService.deleteResource(new NameOrURI(name: "test1"), "test")

        def restriction = new MapEntryQueryServiceRestrictions(mapVersion: new NameOrURI(name: "test1"))

        def query = [
                getRestrictions   : {
                    restriction
                },

                getQuery          : {
                    null
                },

                getFilterComponent: {
                    [] as Set
                },

                getReadContext    : {
                    null
                }
        ] as MapEntryQuery

        assertEquals 0, mapEntryQueryService.getResourceSummaries(query, null, null).entries.size()
    }

    @Ignore("Ignore for now...")
    @Test(expected = ResourceIsNotOpen)
    void testErrorOnChangeFinal() {
        def resource = new MapVersion(
                about: "http://test1",
                mapVersionName: "test1",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))

        maintenanceService.createResource(resource)

        resource.setState(FinalizableState.FINAL)

        getMaintenanceService().updateResource(resource)

    }

}
