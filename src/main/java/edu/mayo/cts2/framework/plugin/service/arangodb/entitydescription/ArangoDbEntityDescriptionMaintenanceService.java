package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDescriptionBase;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.association.HierarchyAssociation;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.association.AssociationMaintenanceService;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionMaintenanceService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class ArangoDbEntityDescriptionMaintenanceService extends AbstractArangoDbDefaultMaintenanceService<EntityDescription,EntityDescriptionReadId> implements EntityDescriptionMaintenanceService {

    @Resource
    private EntityDescriptionStorageInfo entityDescriptionStorageInfo;

    @Resource
    private AssociationMaintenanceService associationMaintenanceService;

    @Resource
    private ArangoDbEntityDescriptionReadService arangoDbEntityDescriptionReadService;

    @Override
    public void updateChangeableMetadata(EntityDescriptionReadId identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public EntityDescription createResource(EntityDescription resource) {
        EntityDescription entityDescription = super.createResource(resource);

        EntityDescriptionBase entity = ModelUtils.getEntity(resource);

        URIAndEntityName[] parents = ModelUtils.getEntity(resource).getParent();
        if(parents != null) {
            for(URIAndEntityName parent : parents) {
                Association association = new HierarchyAssociation();
                association.setSubject(parent);
                StatementTarget target = new StatementTarget();
                target.setEntity(this.toURIAndEntityName(entity.getEntityID(), entity.getAbout()));
                association.addTarget(target);
                association.setAssertedBy(entity.getDescribingCodeSystemVersion());

                this.associationMaintenanceService.createResource(association);
            }
        }

        return entityDescription;
    }

    private URIAndEntityName toURIAndEntityName(ScopedEntityName scopedEntityName, String uri) {
        URIAndEntityName uriAndEntityName = new URIAndEntityName();
        uriAndEntityName.setName(scopedEntityName.getName());
        uriAndEntityName.setNamespace(scopedEntityName.getNamespace());
        uriAndEntityName.setUri(uri);

        return uriAndEntityName;
    }

    @Override
    protected String getHref(EntityDescription resource) {
        return EntityDescriptionUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    protected IndexDocument<EntityDescription> getIndexDocument(String handle, EntityDescription document) {
        return new IndexedEntityDescription(handle, document);
    }

    @Override
    protected String getAbout(EntityDescription resource) {
        return ModelUtils.getEntity(resource).getAbout();
    }

    @Override
    protected String getAbout(EntityDescriptionReadId identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<EntityDescription, EntityDescriptionReadId> getDocumentReader() {
        return this.arangoDbEntityDescriptionReadService;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.entityDescriptionStorageInfo;
    }

    @Override
    protected String getUriPath() {
        return EntityDescriptionConstants.URI_PATH;
    }

    @Override
    public Class<EntityDescription> getStorageClass() {
        return EntityDescription.class;
    }

}
