package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup;
import edu.mayo.cts2.framework.model.core.IsChangeable;
import edu.mayo.cts2.framework.model.core.types.ChangeType;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.plugin.service.arangodb.update.Cts2ChangeService;
import edu.mayo.cts2.framework.service.profile.BaseMaintenanceService;
import edu.mayo.cts2.framework.service.profile.StructuralConformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractArangoDbMaintenanceService<T extends IsChangeable, R extends IsChangeable,I> extends AbstractArangoDbResourceService<T> implements BaseMaintenanceService<T,R,I> {

    private static final Logger log = LoggerFactory.getLogger(AbstractArangoDbMaintenanceService.class);

    @Resource
    private ArangoDao arangoDao;

    @Resource
    private Cts2ChangeService cts2ChangeService;

    @Resource
    private ElasticsearchDao elasticsearchDao;

    public abstract ArangoDbDocumentReader<T,I> getDocumentReader();

    protected abstract T toStorageResource(R entity);

    @Override
    public final T cloneResource(I resourceToClone, R newResource) {
        T resource = this.createResource(newResource);

        this.cloneDependencies(resourceToClone, newResource);

        return resource;
    }

    protected void cloneDependencies(I resourceToClone, R newResource) {
        //
    }

    protected void deleteDependencies(I resource) {
        //
    }

    private static class KeyedResource {
        private String key;
        private JsonElement resource;

        public KeyedResource(String key, JsonElement resource) {
            this.key = key;
            this.resource = resource;
        }
    }

    public void moveDocumentsToHistory(Collection<T> resources) {
        Set<String> keys = Sets.newHashSet();

        for(T resource : resources) {
            keys.add(this.getStorageInfo().getArangoDbKey(resource));
        }

        String aql = "FOR u IN " + this.getCollection() + " FILTER u._key IN @keys REMOVE u IN " + this.getCollection() + " INSERT UNSET(u, '_key') IN  " + this.getCollection() + ArangoDbServiceConstants.HISTORY_COLLECTION_SUFFIX;

        Map<String,Object> params = Maps.newHashMap();
        params.put("keys", keys);

        try {
            this.arangoDao.getDriver().executeAqlQuery(aql, params, null, null);
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyedResource getKeyedResource(T resource) {
        JsonElement jsonElement = EntityFactory.toJsonElement(resource, false);
        String key = this.getArangoDbKey(resource);
        jsonElement.getAsJsonObject().addProperty(ArangoDbServiceConstants.ARANGO_KEY_PROP, key);

        return new KeyedResource(key, jsonElement);
    }

    public void importResources(Collection<R> resources) {
        Map<T,String> resourceKeyMap = Maps.newHashMap();

        List<JsonElement> json = Lists.newArrayList();
        for(R resource : resources) {
            T wrapped = this.toStorageResource(resource);
            KeyedResource keyedResource = this.getKeyedResource(wrapped);
            String key = keyedResource.key;
            json.add(keyedResource.resource);

            resourceKeyMap.put(wrapped, key);
        }
        try {
            this.arangoDao.getDriver().importDocuments(this.getCollection(), false, json);
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }

        List<IndexDocument<?>> indexDocuments = Lists.newArrayList();

        for(Map.Entry<T, String> entry : resourceKeyMap.entrySet()) {
            IndexDocument<R> indexDocument = this.getIndexDocument(this.getCollection() + "/" + entry.getValue(), entry.getKey());

            if(indexDocument != null) {
                indexDocuments.add(indexDocument);
            }
        }

        if(indexDocuments.size() > 0) {
            this.elasticsearchDao.indexDocuments(this.getStorageInfo().getCollection(), indexDocuments);
        }
    }

    @Override
    public T createResource(R resource) {
        DocumentEntity<T> stored;

        T storageResource = this.toStorageResource(resource);

        switch (this.getStorageInfo().getStorageType()) {

            case DOCUMENT:
                stored = this.arangoDao.writeDocument(this.getCollection(), this.getStorageInfo().getArangoDbKey(storageResource), storageResource);
                break;
            case EDGE:
                stored = this.arangoDao.writeEdge(this.getCollection(), storageResource, this.getFrom(resource), this.getTo(resource));
                break;
            default:
                throw new IllegalStateException();
        }

        this.afterCreate(stored);

        return stored.getEntity();
    }

    protected String getFrom(R resource) {
        throw new UnsupportedOperationException();
    }

    protected String getTo(R resource) {
        throw new UnsupportedOperationException();
    }

    protected void beforeUpdate(DocumentEntity<R> oldResource, T newResource) {
        //
    }

    protected void afterUpdate(DocumentEntity<T> document) {
        IndexDocument<R> indexDocument = this.getIndexDocument(document);

        if(indexDocument != null) {
            this.elasticsearchDao.indexDocument(this.getStorageInfo().getCollection(), indexDocument);
        }

        this.fireChangeEvent(document, ChangeType.UPDATE);
    }

    protected void afterCreate(DocumentEntity<T> document) {
        IndexDocument<R> indexDocument = this.getIndexDocument(document);

        if(indexDocument != null) {
            this.elasticsearchDao.indexDocument(this.getStorageInfo().getCollection(), indexDocument);
        }

        this.fireChangeEvent(document, ChangeType.CREATE);
    }

    private void fireChangeEvent(DocumentEntity<T> document, ChangeType changeType) {
        StructuralConformance conformance = AnnotationUtils.findAnnotation(this.getClass(), StructuralConformance.class);

        ChangeableElementGroup changeableElementGroup = document.getEntity().getChangeableElementGroup();

        if(changeableElementGroup == null ||
                changeableElementGroup.getChangeDescription() == null ||
                changeableElementGroup.getChangeDescription().getChangeDate() == null) {
            log.warn("Cannot determine a Resource ChangeDate. Change notifications will not be sent!");
        } else {
            this.cts2ChangeService.onChange(
                    changeType,
                    conformance.value(),
                    this.getAbout(document.getEntity()),
                    this.getHref(document.getEntity()),
                    document.getEntity().getChangeableElementGroup().getChangeDescription().getChangeDate());
        }
    }

    protected IndexDocument<R> getIndexDocument(DocumentEntity<T> document) {
        return this.getIndexDocument(document.getDocumentHandle(), document.getEntity());
    }

    protected abstract IndexDocument<R> getIndexDocument(String handle, T document);

    public void updateResource(T resource) {
        DocumentEntity<R> before = this.arangoDao.readDocumentById(
                this.getStorageInfo().getArangoDbKey(resource),
                this.getCollection(),
                this.getResourceClass());

        this.beforeUpdate(before, resource);

        this.moveDocumentsToHistory(Lists.newArrayList(resource));

        DocumentEntity<T> stored;

        switch (this.getStorageInfo().getStorageType()) {
            case DOCUMENT: stored = this.arangoDao.writeDocument(this.getCollection(), this.getStorageInfo().getArangoDbKey(resource), resource); break;
            case EDGE: stored = this.arangoDao.writeEdge(this.getCollection(), resource, this.getFrom(this.unwrap(resource)), this.getTo(this.unwrap(resource))); break;
            default: throw new IllegalStateException();
        }

        this.afterUpdate(stored);
    }

    protected abstract R unwrap(T resource);

    @Override
    public void deleteResource(I identifier, String changeSetUri) {
        DocumentEntity<T> document = this.getDocumentReader().readDocument(identifier, null);

        String key = document.getDocumentKey();

        try {
            this.arangoDao.getDriver().deleteDocument(this.getStorageInfo().getCollection(), key);
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }

        this.elasticsearchDao.deleteDocument(this.getAbout(document.getEntity()), this.getCollection());

        this.deleteDependencies(identifier);
    }

    protected abstract String getAbout(T resource);

    protected abstract String getHref(T resource);

    protected abstract String getAbout(I identifier);

    protected String getCollection() {
        return this.getStorageInfo().getCollection();
    }

    public abstract Class<R> getResourceClass();

}
