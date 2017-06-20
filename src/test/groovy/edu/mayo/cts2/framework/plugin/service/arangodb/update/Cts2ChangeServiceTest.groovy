package edu.mayo.cts2.framework.plugin.service.arangodb.update
import com.fasterxml.jackson.databind.ObjectMapper
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.types.ChangeType
import edu.mayo.cts2.framework.model.service.core.types.StructuralProfile
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.arangodb.DbClearingTest
import edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion.ArangoDbValueSetDefinitionMaintenanceService
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

@Ignore("Need Redis runinng... ignore for now.")
class Cts2ChangeServiceTest extends DbClearingTest {

    @Autowired
    ArangoDbValueSetDefinitionMaintenanceService maintenanceService

    @Autowired
    RedisTemplate redisTemplate

    @Test
    void testGetResourceSummaries() {
        def lock = new CountDownLatch(1);

        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer()

        listenerContainer.setConnectionFactory(redisTemplate.getConnectionFactory())

        def cts2Change

        listenerContainer.addMessageListener([
            onMessage: { Message message, byte[] pattern ->
                cts2Change = new ObjectMapper().readValue(message.body, Cts2Change)
                lock.countDown()
            }] as MessageListener, new ChannelTopic(Cts2ChangeService.CTS2_CHANGE_CHANNEL))

        listenerContainer.afterPropertiesSet()
        listenerContainer.start()

        maintenanceService.createResource(new ValueSetDefinition(about: "http://test", definedValueSet: new ValueSetReference(content: "testvs")))

        // give it some time
        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals ChangeType.CREATE, cts2Change.changeType
        assertEquals "http://test", cts2Change.uri
        assertNotNull cts2Change.href
        assertEquals cts2Change.resourceType, StructuralProfile.SP_VALUE_SET_DEFINITION
    }

}
