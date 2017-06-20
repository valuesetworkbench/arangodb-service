package edu.mayo.cts2.framework.plugin.service.arangodb.association

import edu.mayo.cts2.framework.model.association.types.GraphDirection
import edu.mayo.cts2.framework.model.core.CodeSystemReference
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.model.core.ScopedEntityName
import edu.mayo.cts2.framework.model.core.TsAnyType
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.entity.Designation
import edu.mayo.cts2.framework.model.entity.EntityDescription
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionMaintenanceService
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ArangoDbAssociationQueryServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbAssociationQueryService queryService

    @Autowired
    ArangoDbEntityDescriptionMaintenanceService entityMaintService

    @Test
    void testResolveGraph() {
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

        def result = queryService.getAssociationGraph(null, new EntityDescriptionReadId("http://uri/child", null), GraphDirection.REVERSE, -1);

        print result;
    }
}
