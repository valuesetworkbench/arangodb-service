package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbLocalIdMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.EntityDescriptionStorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.association.AssociationMaintenanceService;
import edu.mayo.cts2.framework.service.profile.association.name.AssociationReadId;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class ArangoDbAssociationMaintenanceService extends AbstractArangoDbLocalIdMaintenanceService<LocalIdAssociation, Association,AssociationReadId> implements AssociationMaintenanceService {

    @Resource
    private AssociationStorageInfo associationStorageInfo;

    @Resource
    private EntityDescriptionStorageInfo entityDescriptionStorageInfo;

    @Resource
    private ArangoDbEntityDescriptionMaintenanceService entityMaintenanceService;

    @Override
    public void updateChangeableMetadata(AssociationReadId identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ArangoDbDocumentReader<LocalIdAssociation, AssociationReadId> getDocumentReader() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected IndexDocument<Association> getIndexDocument(String handle, LocalIdAssociation document) {
        return null;
    }

    @Override
    public LocalIdAssociation createResource(Association resource) {
        if(StringUtils.isBlank(resource.getAssociationID())) {
            resource.setAssociationID(UUID.randomUUID().toString());
        }

        return super.createResource(resource);
    }

    @Override
    protected String getResourceAbout(Association resource) {
        return resource.getAssociationID();
    }

    @Override
    protected String getAbout(AssociationReadId identifier) {
        return identifier.getUri();
    }

    @Override
    protected String getFrom(Association resource) {
        URIAndEntityName subject = resource.getSubject();

        return this.entityDescriptionStorageInfo.getCollection() + "/" + this.entityDescriptionStorageInfo.getArangoDbKey(resource.getAssertedBy(), subject.getUri());
    }

    @Override
    protected String getTo(Association resource) {
        URIAndEntityName target = resource.getTarget(0).getEntity();

        return this.entityDescriptionStorageInfo.getCollection() + "/" + this.entityDescriptionStorageInfo.getArangoDbKey(resource.getAssertedBy(), target.getUri());
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.associationStorageInfo;
    }

    @Override
    protected LocalIdAssociation toLocalIdResource(String key, Association resource) {
        return new LocalIdAssociation(key, resource);
    }

    @Override
    protected String getHref(LocalIdAssociation resource) {
        return AssociationUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    public Class<LocalIdAssociation> getStorageClass() {
        return LocalIdAssociation.class;
    }

    @Override
    public Class<Association> getResourceClass() {
        return Association.class;
    }

}
