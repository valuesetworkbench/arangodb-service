package edu.mayo.cts2.framework.plugin.service.arangodb.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.core.types.ChangeType;
import edu.mayo.cts2.framework.model.service.core.types.StructuralProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Cts2ChangeService {

    public static final String CTS2_CHANGE_CHANNEL = "org.omg.spec.cts2.change";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UrlConstructor urlConstructor;

    @Autowired(required = false)
    private RedisTemplate redisTemplate;

    public void onChange(ChangeType changeType, StructuralProfile resourceType, String uri, String href, Date resourceTimestamp) {
        if(this.redisTemplate != null) {
            try {
                this.redisTemplate.convertAndSend
                        (CTS2_CHANGE_CHANNEL,
                                this.objectMapper.writeValueAsString(
                                        new Cts2Change(
                                            changeType,
                                                resourceType,
                                                uri,
                                                href,
                                                this.urlConstructor.getServerRootWithAppName(),
                                                resourceTimestamp)));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
