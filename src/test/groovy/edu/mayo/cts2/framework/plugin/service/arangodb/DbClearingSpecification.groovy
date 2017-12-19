package edu.mayo.cts2.framework.plugin.service.arangodb

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration("/test-arangodb-service-context.xml")
@ActiveProfiles("test")
abstract class DbClearingSpecification extends Specification {

    //

}
