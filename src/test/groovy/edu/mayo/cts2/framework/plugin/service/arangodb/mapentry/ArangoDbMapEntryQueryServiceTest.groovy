package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry

import edu.mayo.cts2.framework.model.core.MapReference
import edu.mayo.cts2.framework.model.core.MapVersionReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.mapversion.MapEntry
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.service.command.restriction.MapEntryQueryServiceRestrictions
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryQuery
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertNotNull

class ArangoDbMapEntryQueryServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbMapEntryQueryService service

    @Autowired
    ArangoDbMapEntryMaintenanceService maintenanceService

    @Test
    void testGetResourceSummaries() {
        maintenanceService.createResource(
                new MapEntry(
                        assertedBy: new MapVersionReference(
                                map: new MapReference(content: "map"),
                                mapVersion:  new NameAndMeaningReference(content: "mapv")),
                        mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns")))

        def query = [
                getRestrictions: {
                    new MapEntryQueryServiceRestrictions(mapVersion: ModelUtils.nameOrUriFromName("mapv"))
                },

                getQuery: {
                    null
                },

                getFilterComponent: {
                    [] as Set
                },

                getReadContext: {
                    null
                }
        ] as MapEntryQuery

        assertNotNull service.getResourceSummaries(query, null, null)
        Assert.assertEquals 1, service.getResourceSummaries(query, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesWrongMapVersion() {
        maintenanceService.createResource(
                new MapEntry(
                        assertedBy: new MapVersionReference(
                                map: new MapReference(content: "map"),
                                mapVersion:  new NameAndMeaningReference(content: "mapv")),
                        mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns")))

        def query = [
            getRestrictions: {
                new MapEntryQueryServiceRestrictions(mapVersion: ModelUtils.nameOrUriFromName("xxxxxx"))
            },

            getQuery: {
                null
            },

            getFilterComponent: {
                [] as Set
            },

            getReadContext: {
                null
            }
        ] as MapEntryQuery

        assertNotNull service.getResourceSummaries(query, null, null)
        Assert.assertEquals 0, service.getResourceSummaries(query, null, null).entries.size()
    }

}
