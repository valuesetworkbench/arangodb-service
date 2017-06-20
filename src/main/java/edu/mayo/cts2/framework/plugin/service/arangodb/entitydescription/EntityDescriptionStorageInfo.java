package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription;

import com.arangodb.entity.IndexType;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDescriptionBase;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.ElasticSearchIndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.IndexSpecification;
import edu.mayo.cts2.framework.plugin.service.arangodb.json.TypeAdapterProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EntityDescriptionStorageInfo extends AbstractStorageInfo<EntityDescription> {

    @Override
    public String getCollection() {
        return ArangoDbServiceConstants.ENTITY_DESCRIPTION_COLLECTION;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.DOCUMENT;
    }

    @Override
    public Set<IndexSpecification> getIndexSpecifications() {
        IndexSpecification about = new IndexSpecification(false, IndexType.HASH, new String[]{"namedEntity.about"});
        IndexSpecification name = new IndexSpecification(false, IndexType.HASH, new String[]{"namedEntity.entityID.name"});
        //IndexSpecification namespace = new IndexSpecification(false, IndexType.SKIPLIST, new String[]{"namedEntity.entityID.namespace"});
        //IndexSpecification codeSystemName = new IndexSpecification(false, IndexType.SKIPLIST, new String[]{"namedEntity.describingCodeSystemVersion.codeSystem.content"});
        IndexSpecification codeSystemVersionName = new IndexSpecification(false, IndexType.HASH, new String[]{"namedEntity.describingCodeSystemVersion.version.content"});

        return Sets.newHashSet(about, name/*, namespace, codeSystemName*/, codeSystemVersionName);
    }

    @Override
    public Set<ElasticSearchIndexSpecification> getElasticSearchIndexSpecifications() {
        return Sets.newHashSet(new ElasticSearchIndexSpecification(ArangoDbServiceConstants.ENTITY_DESCRIPTION_COLLECTION, "mappings/EntityDescription.json"));
    }

    @Override
    public Set<? extends TypeAdapterProvider> getTypeAdapters() {
        return Sets.newHashSet();
    }

    @Override
    protected String doGetArangoDbKey(EntityDescription resource) {
        EntityDescriptionBase base = ModelUtils.getEntity(resource);

        return base.getDescribingCodeSystemVersion().getVersion().getContent() + base.getAbout();
    }

    public String getArangoDbKey(CodeSystemVersionReference codeSystemVersion, String uri) {
        return this.hash(codeSystemVersion.getVersion().getContent() + uri);
    }

}
