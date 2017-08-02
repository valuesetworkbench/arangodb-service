package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.core.types.AssociationDirection
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.core.types.SetOperator
import edu.mayo.cts2.framework.model.entity.Designation
import edu.mayo.cts2.framework.model.entity.EntityDescription
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valuesetdefinition.*
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.plugin.service.arangodb.association.ArangoDbAssociationMaintenanceService
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionMaintenanceService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import org.joda.time.DateTime
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertEquals

class ArangoDbValueSetDefinitionResolutionServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbValueSetDefinitionResolutionService service

    @Autowired
    ArangoDbValueSetDefinitionMaintenanceService maintService

    @Autowired
    ArangoDbEntityDescriptionMaintenanceService entityMaintService

    @Autowired
    ArangoDbAssociationMaintenanceService associationMaintService

    @Test
    void testResolveCompleteCodeSystem() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "testcsv"), codeSystem: new CodeSystemReference(content: "cs")))));


        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(completeCodeSystem:
                                new CompleteCodeSystemReference(codeSystemVersion:
                                        new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "testcsv"))))]))

        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), null, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
    }

    @Test
    void testResolveCompleteCodeSystemWrongCsv() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "testcsv"), codeSystem: new CodeSystemReference(content: "cs")))));


        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(completeCodeSystem:
                                new CompleteCodeSystemReference(codeSystemVersion:
                                        new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "__INVALID__"))))]))

        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), null, null, null, null, new Page())

        assertEquals 0, resolution.entries.size()
    }

    @Test
    void testResolveSpecificEntities() {
        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                        new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "foo", name: "test", namespace: "ns")]))]))

        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), null, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
    }

    @Test
    void testResolveSpecificEntitiesAsCompleteSet() {

        def entries = (1..100).inject([]) { result, i -> result + new URIAndEntityName(uri: "foo$i", name: "test$i", namespace: "ns") }

        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                                new SpecificEntityList(referencedEntity: entries))]))

        def resolution = service.resolveDefinitionAsCompleteSet(new ValueSetDefinitionReadId("http://uri"), null, null, null, null)

        assertEquals 100, resolution.entry.size()
    }

    @Test
    void testResolveSpecificEntitiesAsEntityDirectory() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "foo",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "test", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));


        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "foo", name: "test", namespace: "ns")]))]))

        def resolution = service.resolveDefinitionAsEntityDirectory(new ValueSetDefinitionReadId("http://uri"), null, null, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
    }

    @Test
    void testResolveSpecificEntitiesAsEntityDirectoryWithFilter() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "foo",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "test", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));


        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "foo", name: "test", namespace: "ns")]))]))

        def resolution = service.resolveDefinitionAsEntityDirectory(
                new ValueSetDefinitionReadId("http://uri"),
                null,
                null,
                [
                        getFilterComponent: {
                            [new ResolvedFilter(matchValue: "parent", matchAlgorithmReference: new MatchAlgorithmReference(content: "contains"), componentReference: new ComponentReference(attributeReference: "resourceSynopsis"))] as Set
                        }
                ] as ResolvedValueSetResolutionEntityQuery,
                null,
                null,
                new Page())

        assertEquals 1, resolution.entries.size()
    }

    @Test
    void testResolveSpecificEntitiesAsEntityDirectoryWithWrongFilter() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "foo",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "test", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));


        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "foo", name: "test", namespace: "ns")]))]))

        def resolution = service.resolveDefinitionAsEntityDirectory(
                new ValueSetDefinitionReadId("http://uri"),
                null,
                null,
                [
                        getFilterComponent: {
                            [new ResolvedFilter(matchValue: "__INVALID__", matchAlgorithmReference: new MatchAlgorithmReference(content: "contains"), componentReference: new ComponentReference(attributeReference: "resourceSynopsis"))] as Set
                        }
                ] as ResolvedValueSetResolutionEntityQuery,
                null,
                null,
                new Page())

        assertEquals 0, resolution.entries.size()
    }

    @Test
    void testResolveSpecificEntitiesOnUpdatedValueSet() {
        LocalIdValueSetDefinition localIdDef = maintService.createResource(
                new ValueSetDefinition(
                        state: FinalizableState.OPEN,
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        localIdDef.getResource().addEntry(new ValueSetDefinitionEntry(entityList:
                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "foo", name: "test", namespace: "test")])))

        localIdDef.changeableElementGroup = new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2010, 1, 1, 1, 1).toDate()))

        maintService.updateResource(localIdDef)

        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), null, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
    }

    @Test
    void testResolveAssociatedEntities() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")],
                                about: "http://uri/child",
                                designation: [new Designation(value: new TsAnyType(content: "child"))],
                                entityID: new ScopedEntityName(name: "child", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(associatedEntities:
                                new AssociatedEntitiesReference(referencedEntity: new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")))]))


        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), [new NameOrURI(name: "csv")] as Set, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
    }

    @Test
    void testResolveAssociatedEntitiesAndSpecificEntities() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://something/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://something/parent", name: "parent", namespace: "ns")],
                                about: "http://something/child",
                                designation: [new Designation(value: new TsAnyType(content: "child"))],
                                entityID: new ScopedEntityName(name: "child", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://something/specific",
                                designation: [new Designation(value: new TsAnyType(content: "specific"))],
                                entityID: new ScopedEntityName(name: "specific", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));


        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(entityList:
                                new SpecificEntityList(referencedEntity: [new URIAndEntityName(uri: "http://something/specific", name: "specific", namespace: "ns")])),
                                new ValueSetDefinitionEntry(associatedEntities:
                                    new AssociatedEntitiesReference(referencedEntity: new URIAndEntityName(uri: "http://something/parent", name: "parent", namespace: "ns")))]))


        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), [new NameOrURI(name: "csv")] as Set, null, null, null, new Page())

        assertEquals 2, resolution.entries.size()
    }

    @Test
    void testResolveAssociatedEntitiesIntersect() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")],
                                about: "http://uri/child1",
                                designation: [new Designation(value: new TsAnyType(content: "child1"))],
                                entityID: new ScopedEntityName(name: "child1", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/child1", name: "parent", namespace: "ns")],
                                about: "http://uri/child2",
                                designation: [new Designation(value: new TsAnyType(content: "child2"))],
                                entityID: new ScopedEntityName(name: "child2", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(
                                    operator: SetOperator.INTERSECT,
                                    associatedEntities:
                                            new AssociatedEntitiesReference(direction: AssociationDirection.TARGET_TO_SOURCE, referencedEntity: new URIAndEntityName(uri: "http://uri/child1", name: "child1", namespace: "ns"))),
                                new ValueSetDefinitionEntry(
                                        operator: SetOperator.INTERSECT,
                                        associatedEntities:
                                            new AssociatedEntitiesReference(direction: AssociationDirection.TARGET_TO_SOURCE, referencedEntity: new URIAndEntityName(uri: "http://uri/child2", name: "child2", namespace: "ns")))]))


        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), [new NameOrURI(name: "csv")] as Set, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
        assertEquals "http://uri/parent", resolution.entries.get(0).getUri()
    }

    @Test
    void testResolveAssociatedEntitiesUnion() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")],
                                about: "http://uri/child1",
                                designation: [new Designation(value: new TsAnyType(content: "child1"))],
                                entityID: new ScopedEntityName(name: "child1", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/child1", name: "child1", namespace: "ns")],
                                about: "http://uri/child2",
                                designation: [new Designation(value: new TsAnyType(content: "child2"))],
                                entityID: new ScopedEntityName(name: "child2", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(
                                operator: SetOperator.UNION,
                                associatedEntities:
                                        new AssociatedEntitiesReference(direction: AssociationDirection.SOURCE_TO_TARGET, referencedEntity: new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns"))),
                               new ValueSetDefinitionEntry(
                                       operator: SetOperator.UNION,
                                       associatedEntities:
                                               new AssociatedEntitiesReference(direction: AssociationDirection.SOURCE_TO_TARGET, referencedEntity: new URIAndEntityName(uri: "http://uri/child1", name: "child1", namespace: "ns")))]))


        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), [new NameOrURI(name: "csv")] as Set, null, null, null, new Page())

        assertEquals 2, resolution.entries.size()
    }

    @Test
    void testResolveAssociatedEntitiesUnionAndIntersection() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")],
                                about: "http://uri/child1",
                                designation: [new Designation(value: new TsAnyType(content: "child1"))],
                                entityID: new ScopedEntityName(name: "child1", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/child1", name: "child1", namespace: "ns")],
                                about: "http://uri/child2",
                                designation: [new Designation(value: new TsAnyType(content: "child2"))],
                                entityID: new ScopedEntityName(name: "child2", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/child2", name: "child2", namespace: "ns")],
                                about: "http://uri/child3",
                                designation: [new Designation(value: new TsAnyType(content: "child3"))],
                                entityID: new ScopedEntityName(name: "child3", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        maintService.createResource(
                new ValueSetDefinition(
                        about: "http://uri",
                        definedValueSet: new ValueSetReference(content: "test"),
                        entry:[new ValueSetDefinitionEntry(
                                operator: SetOperator.UNION,
                                associatedEntities:
                                        new AssociatedEntitiesReference(direction: AssociationDirection.SOURCE_TO_TARGET, referencedEntity: new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns"))),
                               new ValueSetDefinitionEntry(
                                       operator: SetOperator.UNION,
                                       associatedEntities:
                                               new AssociatedEntitiesReference(direction: AssociationDirection.SOURCE_TO_TARGET, referencedEntity: new URIAndEntityName(uri: "http://uri/child1", name: "child1", namespace: "ns"))),
                               new ValueSetDefinitionEntry(
                                       operator: SetOperator.INTERSECT,
                                       associatedEntities:
                                               new AssociatedEntitiesReference(direction: AssociationDirection.SOURCE_TO_TARGET, referencedEntity: new URIAndEntityName(uri: "http://uri/child2", name: "child2", namespace: "ns")))]))


        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), [new NameOrURI(name: "csv")] as Set, null, null, null, new Page())

        assertEquals 1, resolution.entries.size()
        assertEquals "http://uri/child3", resolution.entries.get(0).getUri()
    }

    @Test
    void testResolveAssociatedEntitiesMultipeLevels() {
        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                about: "http://uri/parent",
                                designation: [new Designation(value: new TsAnyType(content: "parent"))],
                                entityID: new ScopedEntityName(name: "parent", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")],
                                about: "http://uri/child1",
                                designation: [new Designation(value: new TsAnyType(content: "child1"))],
                                entityID: new ScopedEntityName(name: "child1", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        entityMaintService.createResource(
                new EntityDescription(
                        namedEntity: new NamedEntityDescription(
                                parent: [new URIAndEntityName(uri: "http://uri/child1", name: "child1", namespace: "ns")],
                                about: "http://uri/child2",
                                designation: [new Designation(value: new TsAnyType(content: "child2"))],
                                entityID: new ScopedEntityName(name: "child2", namespace: "ns"),
                                describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))));

        maintService.createResource(
                new ValueSetDefinition(
                        definedValueSet: new ValueSetReference(content: "test"),
                        about: "http://uri",
                        entry:[new ValueSetDefinitionEntry(associatedEntities:
                                new AssociatedEntitiesReference(referencedEntity: new URIAndEntityName(uri: "http://uri/parent", name: "parent", namespace: "ns")))]))


        def resolution = service.resolveDefinition(new ValueSetDefinitionReadId("http://uri"), [new NameOrURI(name: "csv")] as Set, null, null, null, new Page())

        assertEquals 2, resolution.entries.size()
    }

}
