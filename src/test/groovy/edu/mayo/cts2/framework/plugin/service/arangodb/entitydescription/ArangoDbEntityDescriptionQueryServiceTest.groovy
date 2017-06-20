package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription

import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.entity.Designation
import edu.mayo.cts2.framework.model.entity.EntityDescription
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ArangoDbEntityDescriptionQueryServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbEntityDescriptionQueryService service

    @Autowired
    ArangoDbEntityDescriptionMaintenanceService maintenanceService

    @Test
    void testGetResourceSummaries() {
        maintenanceService.createResource(new EntityDescription(
                namedEntity: new NamedEntityDescription(
                        about: "http://foo",
                        entityID: ModelUtils.createScopedEntityName("foo", "bar"),
                        describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")))))

        assertNotNull service.getResourceSummaries(null, null, null)
        Assert.assertEquals 1, service.getResourceSummaries(null, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesChildren() {
        def parent = new EntityDescription(
            namedEntity: new NamedEntityDescription(
                    about: "http://parent",
                    entityID: ModelUtils.createScopedEntityName("parent", "bar"),
                    describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs"))))

        def parentId = parent.getNamedEntity().getEntityID()

        def child = new EntityDescription(
            namedEntity: new NamedEntityDescription(
                    about: "http://child",
                    entityID: ModelUtils.createScopedEntityName("child", "bar"),
                    describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")),
                    parent: [new URIAndEntityName(
                            name: parentId.getName(),
                            namespace: parentId.getNamespace(),
                            uri: parent.getNamedEntity().about)]));

        maintenanceService.createResource(parent)
        maintenanceService.createResource(child)

        def query = [
            getEntitiesFromAssociationsQuery: {
            },

            getRestrictions : {
                new EntityDescriptionQueryServiceRestrictions(
                        codeSystemVersions: [new NameOrURI(name: "csv")] as Set,
                        hierarchyRestriction:
                                new EntityDescriptionQueryServiceRestrictions.HierarchyRestriction(
                                        entity: ModelUtils.entityNameOrUriFromName(parentId),
                                        hierarchyType:EntityDescriptionQueryServiceRestrictions.HierarchyRestriction.HierarchyType.CHILDREN))
            },

            getQuery: {
            },

            getFilterComponent: {
            },

            getReadContext: {
            }
        ] as EntityDescriptionQuery

        def children = service.getResourceSummaries(query, null, null).entries

        Assert.assertEquals 1, children.size()
        assertEquals "child", children[0].name.name
    }

    @Test
    void testElasticSearchQuery() {

        def e = new EntityDescription(
                namedEntity: new NamedEntityDescription(
                        about: "http://child",
                        entityID: ModelUtils.createScopedEntityName("child", "bar"),
                        describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")),
                        designation: [new Designation(value: new TsAnyType(content: "Heart"))]))

        maintenanceService.createResource(e)

        def query = [
                getEntitiesFromAssociationsQuery: {
                },

                getRestrictions : {

                },

                getQuery: {
                },

                getFilterComponent: {
                    [new ResolvedFilter(matchValue: "Heart", matchAlgorithmReference: new MatchAlgorithmReference(content: "contains"), componentReference: new ComponentReference(attributeReference: "resourceSynopsis"))] as Set
                },

                getReadContext: {
                }
        ] as EntityDescriptionQuery

        def results = service.getResourceSummaries(query, null, new Page(maxtoreturn: 3)).entries

        assertEquals 1, results.size()
    }

    @Test
    void testElasticSearchQueryWithCodeSystemVersionRestriction() {
        def e1 = new EntityDescription(
                namedEntity: new NamedEntityDescription(
                        about: "http://child1",
                        entityID: ModelUtils.createScopedEntityName("child1", "bar"),
                        describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")),
                        designation: [new Designation(value: new TsAnyType(content: "Heart"))]))

        maintenanceService.createResource(e1)

        def e2 = new EntityDescription(
                namedEntity: new NamedEntityDescription(
                        about: "http://child2",
                        entityID: ModelUtils.createScopedEntityName("child2", "bar"),
                        describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv___SOMETHING_ELSE"), codeSystem: new CodeSystemReference(content: "cs")),
                        designation: [new Designation(value: new TsAnyType(content: "Heart"))]))

        maintenanceService.createResource(e2)

        def query = [
                getEntitiesFromAssociationsQuery: {
                },

                getRestrictions : {
                    new EntityDescriptionQueryServiceRestrictions(codeSystemVersions: [new NameOrURI(name: "csv")] as Set)
                },

                getQuery: {
                },

                getFilterComponent: {
                    [new ResolvedFilter(matchValue: "Heart", matchAlgorithmReference: new MatchAlgorithmReference(content: "contains"), componentReference: new ComponentReference(attributeReference: "resourceSynopsis"))] as Set
                },

                getReadContext: {
                }
        ] as EntityDescriptionQuery

        def results = service.getResourceSummaries(query, null, new Page()).entries

        assertEquals 1, results.size()
    }

    @Test
    void testElasticSearchQueryWithWrongCodeSystemVersionRestriction() {
        def e = new EntityDescription(
                namedEntity: new NamedEntityDescription(
                        about: "http://child",
                        entityID: ModelUtils.createScopedEntityName("child", "bar"),
                        describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")),
                        designation: [new Designation(value: new TsAnyType(content: "Heart"))]))

        maintenanceService.createResource(e)

        def query = [
                getEntitiesFromAssociationsQuery: {
                },

                getRestrictions : {
                    new EntityDescriptionQueryServiceRestrictions(codeSystemVersions: [new NameOrURI(name: "WRONG!!!")] as Set)
                },

                getQuery: {
                },

                getFilterComponent: {
                    [new ResolvedFilter(matchValue: "Heart", matchAlgorithmReference: new MatchAlgorithmReference(content: "contains"), componentReference: new ComponentReference(attributeReference: "resourceSynopsis"))] as Set
                },

                getReadContext: {
                }
        ] as EntityDescriptionQuery

        def results = service.getResourceSummaries(query, null, new Page()).entries

        assertEquals 0, results.size()
    }
}
