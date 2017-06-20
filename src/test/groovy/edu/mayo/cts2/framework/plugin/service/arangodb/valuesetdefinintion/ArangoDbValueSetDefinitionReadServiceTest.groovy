package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion

import edu.mayo.cts2.framework.core.json.JsonConverter
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

class ArangoDbValueSetDefinitionReadServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbValueSetDefinitionReadService service

    @Autowired
    ArangoDbValueSetDefinitionMaintenanceService maintenanceService

    @Test
    void testCreateAndReadByLocalId() {
        def vsd = maintenanceService.createResource(new ValueSetDefinition(about: "http://test", definedValueSet: new ValueSetReference(content: "test")))

        print new JsonConverter().toJson(new ValueSetDefinition(about: "http://test", definedValueSet: new ValueSetReference(content: "test")))

        assertNotNull service.read(new ValueSetDefinitionReadId(vsd.localID, null), null)
    }

    @Test
    void testCreateAndReadByUri() {
        maintenanceService.createResource(new ValueSetDefinition(about: "http://test", definedValueSet: new ValueSetReference(content: "test")))

        assertNotNull service.read(new ValueSetDefinitionReadId("http://test"), null)
    }

    @Test
    void testCreateAndReadByTag() {
        maintenanceService.createResource(new ValueSetDefinition(versionTag: [new VersionTagReference(content: "DEV")], about: "http://test1", definedValueSet: new ValueSetReference(content: "test")))
        maintenanceService.createResource(new ValueSetDefinition(versionTag: [new VersionTagReference(content: "PRODUCTION")], about: "http://test2", definedValueSet: new ValueSetReference(content: "test")))

        assertNotNull service.readByTag(new NameOrURI(name: "test"), new VersionTagReference(content: "PRODUCTION"), null)
    }

    @Test
    void testCreateAndReadByWrongTag() {
        maintenanceService.createResource(new ValueSetDefinition(versionTag: [new VersionTagReference(content: "DEV")], about: "http://test1", definedValueSet: new ValueSetReference(content: "test")))
        maintenanceService.createResource(new ValueSetDefinition(versionTag: [new VersionTagReference(content: "PRODUCTION")], about: "http://test2", definedValueSet: new ValueSetReference(content: "test")))

        assertNull service.readByTag(new NameOrURI(name: "test"), new VersionTagReference(content: "__INVALID__"), null)
    }
}
