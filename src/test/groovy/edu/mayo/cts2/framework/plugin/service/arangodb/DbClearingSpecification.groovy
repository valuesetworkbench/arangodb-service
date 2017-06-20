package edu.mayo.cts2.framework.plugin.service.arangodb

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration("/test-arangodb-service-context.xml")
@ActiveProfiles("test")
abstract class DbClearingSpecification extends Specification {

    @Autowired
    ArangoDao arangoDao

    @Override
    void setup(){
        def collections = this.arangoDao.getDriver().getCollections(true).getNames().keySet()

        collections.each {
            this.arangoDao.getDriver().truncateCollection(it);
        }
    }

    @Override
    void cleanup(){
        def collections = this.arangoDao.getDriver().getCollections(true).getNames().keySet()

        collections.each {
            this.arangoDao.getDriver().truncateCollection(it);
        }
    }

}
