package edu.mayo.cts2.framework.plugin.service.arangodb.update;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.core.SourceReference;
import edu.mayo.cts2.framework.model.updates.ChangeSet;
import edu.mayo.cts2.framework.model.updates.ChangeableResource;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbMaintenanceService;
import edu.mayo.cts2.framework.service.profile.update.ChangeSetService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbUpdateService implements ChangeSetService, InitializingBean {

    @Resource
    private Set<AbstractArangoDbMaintenanceService<?,?,?>> maintenanceServices;

    private Map<Class<?>,AbstractArangoDbMaintenanceService<?,?,?>> serviceMap = Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        for(AbstractArangoDbMaintenanceService<?,?,?> service : this.maintenanceServices) {
            this.serviceMap.put(service.getResourceClass(), service);
        }
    }

    @Override
    public ChangeSet readChangeSet(String changeSetUri) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ChangeSet createChangeSet() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void updateChangeSetMetadata(String changeSetUri, SourceReference creator, OpaqueData changeInstructions, Date officialEffectiveDate) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void rollbackChangeSet(String changeSetUri) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void commitChangeSet(String changeSetUri) {
        //TODO
        // pass through...
    }

    @Override
    public String importChangeSet(ChangeSet changeSet) {
        Map<Class,List> batches = Maps.newHashMap();
        for(Class clazz : this.serviceMap.keySet()) {
            batches.put(clazz, Lists.newArrayList());
        }

        for(ChangeableResource entry : changeSet.getMember()) {
            Object value = entry.getChoiceValue();
            if(! batches.containsKey(value.getClass())) {
                throw new RuntimeException("Cannot find bach service.");
            }
            batches.get(value.getClass()).add(value);
        }

        for(Map.Entry<Class, List> batch : batches.entrySet()) {
            if(CollectionUtils.isNotEmpty(batch.getValue())) {
                this.serviceMap.get(batch.getKey()).importResources(batch.getValue());
            }
        }

        return "";
    }

}
