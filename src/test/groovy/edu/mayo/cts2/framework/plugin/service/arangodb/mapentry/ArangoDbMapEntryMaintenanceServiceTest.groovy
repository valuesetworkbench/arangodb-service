package edu.mayo.cts2.framework.plugin.service.arangodb.mapentry
import edu.mayo.cts2.framework.model.core.MapReference
import edu.mayo.cts2.framework.model.core.MapVersionReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.mapversion.MapEntry
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ArangoDbMapEntryMaintenanceServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbMapEntryMaintenanceService service

    @Autowired
    ArangoDbMapEntryReadService readService

    @Test
    void testUpdate() {
        def mapEntry = new MapEntry(
                        assertedBy: new MapVersionReference(
                                map: new MapReference(content: "map"),
                                mapVersion:  new NameAndMeaningReference(content: "mapv")),
                        mapFrom: new URIAndEntityName(uri: "http://foo", name: "123", namespace: "ns"))


        service.createResource(mapEntry)
        service.updateResource(mapEntry)

        print "Hi"
    }



}
