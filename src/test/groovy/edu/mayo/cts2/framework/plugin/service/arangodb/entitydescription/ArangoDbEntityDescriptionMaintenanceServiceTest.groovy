package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.*
import edu.mayo.cts2.framework.model.entity.Designation
import edu.mayo.cts2.framework.model.entity.EntityDescription
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.arangodb.MaintenanceTestBase
import edu.mayo.cts2.framework.service.profile.QueryService
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.Resource

import static org.junit.Assert.assertEquals

class ArangoDbEntityDescriptionMaintenanceServiceTest extends MaintenanceTestBase {

    @Autowired
    ArangoDbEntityDescriptionQueryService service

    @Resource(type=ArangoDbEntityDescriptionMaintenanceService)
    def maintenanceService

    @Resource(type=ArangoDbEntityDescriptionQueryService)
    QueryService queryService

    @Override
    def createResources(num) {
        def ret = []
        (0..num-1).each {
            ret << new EntityDescription(
                    namedEntity: new NamedEntityDescription(
                            about: "http://$it",
                            entityID: ModelUtils.createScopedEntityName("name-$it", "bar"),
                            describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs"))))
        }

        ret
    }

    @Test
    void testImportAndQueryWithFilter() {
        def entity = new EntityDescription(
                namedEntity: new NamedEntityDescription(
                        about: "http://child",
                        entityID: ModelUtils.createScopedEntityName("child", "bar"),
                        describingCodeSystemVersion: new CodeSystemVersionReference(version: new NameAndMeaningReference(content: "csv"), codeSystem: new CodeSystemReference(content: "cs")),
                        designation: [new Designation(value: new TsAnyType(content: "Heart"))]))

        maintenanceService.importResources([entity])

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

        def results = service.getResourceSummaries(query, null, null).entries

        assertEquals 1, results.size()
    }
}
