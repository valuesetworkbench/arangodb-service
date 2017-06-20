package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.mapversion.MapEntry
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.service.profile.mapentry.name.MapEntryReadId
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

class ArangoDbMapEntryReadServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbMapEntryReadService service

    @Autowired
    ArangoDbMapEntryMaintenanceService maintenanceService

    @Test
    void testCreateAndReadByName() {
        maintenanceService.createResource(
                new MapEntry(
                        assertedBy: new MapVersionReference(
                                map: new MapReference(content: "map"),
                                mapVersion:  new NameAndMeaningReference(content: "mapv")),
                        mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns")))

        assertNotNull service.read(
                new MapEntryReadId(
                        new EntityNameOrURI(entityName: new ScopedEntityName(name: "123", namespace: "ns")),
                        new NameOrURI(name: "mapv")), null)
    }

    @Test
    void testCreateAndReadByNameWrongName() {
        maintenanceService.createResource(
                new MapEntry(
                        assertedBy: new MapVersionReference(
                                map: new MapReference(content: "map"),
                                mapVersion:  new NameAndMeaningReference(content: "mapv")),
                        mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns")))

        assertNull service.read(
                new MapEntryReadId(
                        new EntityNameOrURI(entityName: new ScopedEntityName(name: "xxxxxxxxx", namespace: "ns")),
                        new NameOrURI(name: "mapv")), null)
    }
}
