package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion

import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.arangodb.QueryTestBase
import edu.mayo.cts2.framework.service.command.restriction.ValueSetDefinitionQueryServiceRestrictions
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService
import edu.mayo.cts2.framework.service.profile.QueryService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQuery
import org.joda.time.DateTime
import org.junit.Test

import javax.annotation.Resource

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ArangoDbValueSetDefinitionQueryServiceTest extends QueryTestBase {

    @Resource(type=ArangoDbValueSetDefinitionQueryService)
    QueryService queryService

    @Resource(type=ArangoDbValueSetDefinitionMaintenanceService)
    BaseMaintenanceService maintenanceService

    @Override
    def createResource() {
        new ValueSetDefinition(
                state: FinalizableState.OPEN,
                about: "http://test",
                resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                definedValueSet: new ValueSetReference(content: "testvs"))
    }

    @Test
    void testGetResourceSummaries() {
        maintenanceService.createResource(new ValueSetDefinition(about: "http://test", definedValueSet: new ValueSetReference(content: "testvs")))

        assertNotNull queryService.getResourceSummaries(null, null, null)
    }

    @Test
    void testGetResourceSummariesLucene() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "testvs")))

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
        ] as ValueSetDefinitionQuery , null, null)
    }

    @Test
    void testGetResourceSummariesLuceneAndNotOr() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test1",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "find")))

        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test2",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "find something")))

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
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "find something")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as ValueSetDefinitionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesLuceneByAny_Wrong() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "testvs")))

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
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "zzzzzzz")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as ValueSetDefinitionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesLuceneByAny_ValueSetName() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "testvs")))

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
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "test")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as ValueSetDefinitionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesLuceneByAny_ValueSetNameUnderscores() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "My_THING")))

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
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "thing")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as ValueSetDefinitionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesLuceneByAny_VersionId() {
        maintenanceService.createResource(
                new ValueSetDefinition(
                        about: "http://test",
                        officialResourceVersionId: "FindMe",
                        resourceSynopsis: new EntryDescription(value: new TsAnyType(content: "test")),
                        definedValueSet: new ValueSetReference(content: "testvs")))

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
                                    matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference,
                                    componentReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.componentReference,
                                    matchValue: "FindMe")
                    ] as Set
                },

                getReadContext: {
                    //
                }
        ] as ValueSetDefinitionQuery , null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleValueSets() {
        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test1",
                definedValueSet: new ValueSetReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test2",
                definedValueSet: new ValueSetReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 2, queryService.getResourceSummaries(null, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleValueSetsOrdering() {
        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test1",
                definedValueSet: new ValueSetReference(content: "atestOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        100.times {
            maintenanceService.createResource(new ValueSetDefinition(
                    about: "http://${it}testSomething",
                    definedValueSet: new ValueSetReference(content: "SomethingInTheMiddle"),
                    changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))
        }

        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test2",
                definedValueSet: new ValueSetReference(content: "atestTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        def entries = queryService.getResourceSummaries(null, null, null).entries

        assertEquals "http://test1", entries.get(0).about
        assertEquals "http://test2", entries.get(1).about
    }

    @Test
    void testGetResourceSummariesMultipleValueSetsWithRestriction() {
        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test1",
                definedValueSet: new ValueSetReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test2",
                definedValueSet: new ValueSetReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 1, queryService.getResourceSummaries([

                getRestrictions: {
                    new ValueSetDefinitionQueryServiceRestrictions(valueSet: ModelUtils.nameOrUriFromName("testOne"))
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
        ] as ValueSetDefinitionQuery, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleValueSetsWithRestrictionWithFilter() {
        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test1",
                formalName: "banana",
                definedValueSet: new ValueSetReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test2",
                formalName: "apple",
                definedValueSet: new ValueSetReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 1, queryService.getResourceSummaries([

                getRestrictions: {
                    new ValueSetDefinitionQueryServiceRestrictions(valueSet: ModelUtils.nameOrUriFromName("testOne"))
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
        ] as ValueSetDefinitionQuery, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleValueSetsWithRestrictionWithBadFilter() {
        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test1",
                formalName: "banana",
                definedValueSet: new ValueSetReference(content: "testOne"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        maintenanceService.createResource(new ValueSetDefinition(
                about: "http://test2",
                formalName: "apple",
                definedValueSet: new ValueSetReference(content: "testTwo"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))))

        assertEquals 0, queryService.getResourceSummaries([

                getRestrictions: {
                    new ValueSetDefinitionQueryServiceRestrictions(valueSet: ModelUtils.nameOrUriFromName("testOne"))
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
        ] as ValueSetDefinitionQuery, null, null).entries.size()
    }

    @Test
    void testGetResourceSummariesMultipleVersions() {
        def vs = maintenanceService.createResource(new ValueSetDefinition(
                state: FinalizableState.OPEN,
                about: "http://test",
                definedValueSet: new ValueSetReference(content: "testvs"),
                changeableElementGroup: new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2000, 1, 1, 1, 1).toDate()))))

        vs.changeableElementGroup = new ChangeableElementGroup(changeDescription: new ChangeDescription(changeDate: new DateTime(2001, 1, 1, 1, 1).toDate()))

        maintenanceService.updateResource(vs)

        assertEquals 1, queryService.getResourceSummaries(null, null, null).entries.size()
    }

}
