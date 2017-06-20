package edu.mayo.cts2.framework.plugin.service.arangodb.mapversion
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.mapversion.MapVersion
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.arangodb.QueryTestBase
import edu.mayo.cts2.framework.plugin.service.arangodb.mapentry.ArangoDbMapEntryMaintenanceService
import edu.mayo.cts2.framework.plugin.service.arangodb.mapentry.ArangoDbMapEntryQueryService
import edu.mayo.cts2.framework.service.command.restriction.MapVersionQueryServiceRestrictions
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService
import edu.mayo.cts2.framework.service.profile.QueryService
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionQuery
import org.joda.time.DateTime
import org.junit.Test

import javax.annotation.Resource

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ArangoDbMapVersionQueryServiceTest extends QueryTestBase {

    @Resource(type=ArangoDbMapVersionQueryService)
    QueryService queryService

    @Resource(type=ArangoDbMapVersionMaintenanceService)
    BaseMaintenanceService maintenanceService

    @Resource
    ArangoDbMapEntryMaintenanceService mapEntryMaintenanceService

    @Resource
    ArangoDbMapEntryQueryService mapEntryQueryService

    @Override
    def createResource() {
        new MapVersion(
                about: "http://test",
                mapVersionName: "test",
                resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                versionOf: new MapReference(content: "testvs"))
    }

    @Test
    void testGetResourceSummariesLucene() {
        maintenanceService.createResource(
                new MapVersion(
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        versionOf: new MapReference(content: "testvs")))

        assertNotNull queryService.getResourceSummaries([

            getRestrictions: {
                //
            },

            getQuery: {
                //
            },

            getFilterComponent: {
                [
                        new ResolvedFilter(
                                matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                matchValue: "test")
                ] as Set
            },

            getReadContext: {
                //
            }
        ] as MapVersionQuery , null, null)
    }

    @Test
    void testGetResourceSummariesWithOwner() {
        maintenanceService.createResource(
                new MapVersion(
                        sourceAndRole: [
                                new SourceAndRoleReference(
                                        source: new SourceReference(content: "me"),
                                        role: new RoleReference(content: "owner")
                                )
                        ],
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        versionOf: new MapReference(content: "testvs")))

        assertEquals 1, queryService.getResourceSummaries([

                getRestrictions: {
                    //
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    [
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference,
                                    componentReference: new ComponentReference(attributeReference: "owner"),
                                    matchValue: "me")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery , null, null).entries.size()
    }


    @Test
    void testGetResourceSummariesWithOwnerAndDescription() {
        maintenanceService.createResource(
                new MapVersion(
                        sourceAndRole: [
                                new SourceAndRoleReference(
                                        source: new SourceReference(content: "me"),
                                        role: new RoleReference(content: "owner")
                                )
                        ],
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        versionOf: new MapReference(content: "testvs")))

        assertEquals 1, queryService.getResourceSummaries([

                getRestrictions: {
                    //
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    [
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference,
                                    componentReference: new ComponentReference(attributeReference: "owner"),
                                    matchValue: "me"),
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "test")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesWithOwnerAndWrongDescription() {
        maintenanceService.createResource(
                new MapVersion(
                        sourceAndRole: [
                                new SourceAndRoleReference(
                                        source: new SourceReference(content: "me"),
                                        role: new RoleReference(content: "owner")
                                )
                        ],
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        versionOf: new MapReference(content: "testvs")))

        assertEquals 0, queryService.getResourceSummaries([

                getRestrictions: {
                    //
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    [
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference,
                                    componentReference: new ComponentReference(attributeReference: "owner"),
                                    matchValue: "me"),
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "__INVALID__")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesWithWrongOwner() {
        maintenanceService.createResource(
                new MapVersion(
                        sourceAndRole: [
                                new SourceAndRoleReference(
                                        source: new SourceReference(content: "me"),
                                        role: new RoleReference(content: "owner")
                                )
                        ],
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        versionOf: new MapReference(content: "testvs")))

        assertEquals 0, queryService.getResourceSummaries([

                getRestrictions: {
                    //
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    [
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference,
                                    componentReference: new ComponentReference(attributeReference: "owner"),
                                    matchValue: "NOT ME")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleMaps() {
        maintenanceService.createResource(new MapVersion(
                about: "http://test1",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new MapVersion(
                about: "http://test2",
                versionOf: new MapReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 2, queryService.getResourceSummaries(null, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleMapsWithRestriction() {
        maintenanceService.createResource(new MapVersion(
                about: "http://test1",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new MapVersion(
                about: "http://test2",
                versionOf: new MapReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 1, queryService.getResourceSummaries([

                getRestrictions: {
                    new MapVersionQueryServiceRestrictions(map: ModelUtils.nameOrUriFromName("testOne"))
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    //
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleMapsWithRestrictionWithFilter() {
        maintenanceService.createResource(new MapVersion(
                about: "http://test1",
                formalName: "banana",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new MapVersion(
                about: "http://test2",
                formalName: "apple",
                versionOf: new MapReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 1, queryService.getResourceSummaries([

                getRestrictions: {
                    new MapVersionQueryServiceRestrictions(map: ModelUtils.nameOrUriFromName("testOne"))
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    [
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "banana")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleMapsWithRestrictionWithBadFilter() {
        maintenanceService.createResource(new MapVersion(
                about: "http://test1",
                formalName: "banana",
                versionOf: new MapReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new MapVersion(
                about: "http://test2",
                formalName: "apple",
                versionOf: new MapReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 0, queryService.getResourceSummaries([

                getRestrictions: {
                    new MapVersionQueryServiceRestrictions(map: ModelUtils.nameOrUriFromName("testOne"))
                },

                getQuery: {
                    //
                },

                getFilterComponent: {
                    [
                            new ResolvedFilter(
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "apple")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as MapVersionQuery, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleVersions() {
        maintenanceService.createResource(new MapVersion(
                state: FinalizableState.OPEN,
                about: "http://test",
                versionOf: new MapReference(content: "testvs"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.updateResource(new MapVersion(
                about: "http://test",
                versionOf: new MapReference(content: "testvs"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 1, queryService.getResourceSummaries(null, null, null).entries.size()
    }

}
