package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion

import edu.mayo.cts2.framework.model.core.ChangeDescription
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.entity.EntityDescription
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.service.exception.ResourceIsNotOpen
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry
import edu.mayo.cts2.framework.plugin.service.arangodb.MaintenanceTestBase
import edu.mayo.cts2.framework.service.profile.QueryService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.Resource

import static org.junit.Assert.*

class ArangoDbValueSetDefinitionMaintenanceServiceTest extends MaintenanceTestBase {

    @Autowired
    ArangoDbValueSetDefinitionReadService service

    @Resource(type=ArangoDbValueSetDefinitionMaintenanceService)
    def maintenanceService

    @Resource(type=ArangoDbValueSetDefinitionQueryService)
    QueryService queryService

    @Override
    def createResources(num) {
        def ret = []
        (0..num-1).each {
            ret << new ValueSetDefinition(
                            about: "http://uri/$it",
                            definedValueSet: new ValueSetReference(content: "test"),
                            changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate())))
        }

        ret
    }

    @Test
    void testDelete() {
        def vsd = maintenanceService.createResource(new ValueSetDefinition(about: "http://test", definedValueSet: new ValueSetReference(content: "vs")))

        assertNotNull service.read(new ValueSetDefinitionReadId(vsd.localID, null), null)

        maintenanceService.deleteResource(new ValueSetDefinitionReadId(vsd.getLocalID(), new NameOrURI(name: "vs")), null);

        assertNull service.read(new ValueSetDefinitionReadId(vsd.localID, null), null)
    }

    @Test
    void testUpdateKeepSameLocalId() {
        LocalIdValueSetDefinition localIdDef = maintenanceService.createResource(
                new ValueSetDefinition(
                        state: FinalizableState.OPEN,
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        localIdDef.getResource().addEntry(new ValueSetDefinitionEntry(entityList:
                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "foo", name: "test")])))

        localIdDef.changeableElementGroup = new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2010, 1, 1, 1, 1).toDate()))

        maintenanceService.updateResource(localIdDef)

        assertEquals 1, arangoDao.query("for v in ValueSetDefinition filter v.localId == @localId return v", ['localId': localIdDef.localID], ValueSetDefinition).asEntityList().size()
    }

    @Test
    void testSpecificEntitiesAreStored() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "http://uri/test", name: "test", namespace: "ns")]))]))

        def entities = arangoDao.query('for e in EntityDescription return e', [:], EntityDescription)

        assertEquals 1, entities.asEntityList().size()
    }

    @Ignore("Ignore for now...")
    @Test(expected = ResourceIsNotOpen)
    void testErrorOnChangeFinal() {
        def resource = maintenanceService.createResource(createResources(1))
        resource.resource.setState(FinalizableState.FINAL)

        getMaintenanceService().updateResource(resource)

    }
}
