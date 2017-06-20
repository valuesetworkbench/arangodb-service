package edu.mayo.cts2.framework.plugin.service.arangodb.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.exceptions.JedisConnectionException;

//@Component
//@Profile("!test")
public class RedisTemplateFactory implements FactoryBean<RedisTemplate> {
    
    private static final Logger log = LoggerFactory.getLogger(RedisTemplateFactory.class);

    @Value("${redisHost:localhost}")
    private String redisHost;

    @Override
    public RedisTemplate getObject() throws Exception {
        RedisTemplate redisTemplate = null;
        try {
            redisTemplate = new RedisTemplate();
            redisTemplate.setValueSerializer(new StringRedisSerializer());

            JedisConnectionFactory factory = new JedisConnectionFactory();
            factory.setHostName(this.redisHost);
            factory.afterPropertiesSet();

            redisTemplate.setConnectionFactory(factory);
            redisTemplate.afterPropertiesSet();
        } catch (JedisConnectionException e) {
            log.warn("Redis connection cannot be made. Update notifications will be disabled: " + e.getMessage());
            return null;
        }

        return redisTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return RedisTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
