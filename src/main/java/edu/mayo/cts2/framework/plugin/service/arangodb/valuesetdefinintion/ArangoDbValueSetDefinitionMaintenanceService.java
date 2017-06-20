package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import com.arangodb.entity.DocumentEntity;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.core.types.FinalizableState;
import edu.mayo.cts2.framework.model.entity.Designation;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import edu.mayo.cts2.framework.model.entity.types.DesignationRole;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbLocalIdMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbDocumentReader;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionMaintenanceService;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.IndexDocument;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionMaintenanceService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Set;

@Component
public class ArangoDbValueSetDefinitionMaintenanceService extends AbstractArangoDbLocalIdMaintenanceService<LocalIdValueSetDefinition, ValueSetDefinition, ValueSetDefinitionReadId> implements ValueSetDefinitionMaintenanceService {

    @Resource
    private ValueSetDefinitionStorageInfo valueSetDefinitionStorageInfo;

    @Resource
    private ArangoDbValueSetDefinitionReadService arangoDbValueSetDefinitionReadService;

    @Resource
    private ArangoDbEntityDescriptionMaintenanceService arangoDbEntityDescriptionMaintenanceService;

    @Override
    public void updateChangeableMetadata(ValueSetDefinitionReadId identifier, UpdateChangeableMetadataRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected LocalIdValueSetDefinition toLocalIdResource(String key, ValueSetDefinition resource) {
        return new LocalIdValueSetDefinition(key, resource);
    }

    @Override
    protected IndexDocument<ValueSetDefinition> getIndexDocument(String handle, LocalIdValueSetDefinition document) {
        return new IndexedValueSetDefinition(handle, document);
    }

    @Override
    protected String getResourceAbout(ValueSetDefinition resource) {
        return resource.getAbout();
    }

    @Override
    protected String getAbout(ValueSetDefinitionReadId identifier) {
        return identifier.getUri();
    }

    @Override
    public ArangoDbDocumentReader<LocalIdValueSetDefinition, ValueSetDefinitionReadId> getDocumentReader() {
        return this.arangoDbValueSetDefinitionReadService;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.valueSetDefinitionStorageInfo;
    }

    @Override
    protected String getHref(LocalIdValueSetDefinition resource) {
        return ValueSetDefinitionUtils.getHref(resource, this.getUrlConstructor());
    }

    @Override
    protected void beforeUpdate(DocumentEntity<ValueSetDefinition> oldResource, LocalIdValueSetDefinition newResource) {
        if(oldResource.getEntity().getState() == FinalizableState.FINAL) {
            //TODO: Relax this for now
            //throw new ResourceIsNotOpen();
        }
    }

    @Override
    protected void afterUpdate(DocumentEntity<LocalIdValueSetDefinition> document) {
        super.afterUpdate(document);
        this.storeLocalSpecificEntities(document.getEntity());
    }

    @Override
    protected void afterCreate(DocumentEntity<LocalIdValueSetDefinition> document) {
        super.afterCreate(document);
        this.storeLocalSpecificEntities(document.getEntity());
    }

    protected void storeLocalSpecificEntities(LocalIdValueSetDefinition localIdValueSetDefinition) {
        ValueSetDefinition valueSetDefinition = localIdValueSetDefinition.getResource();
        Set<URIAndEntityName> specificEntities = Sets.newHashSet();

        for(ValueSetDefinitionEntry entry : valueSetDefinition.getEntry()) {
            if(entry.getEntityList() != null) {
                specificEntities.addAll(Arrays.asList(entry.getEntityList().getReferencedEntity()));
            }
        }

        Set<EntityDescription> entities = Sets.newHashSet();

        for(URIAndEntityName entity : specificEntities) {
            // only add 'local' codes -- where 'local' means a code with a URI base from the ValueSetDefinition.
            if(entity.getUri().startsWith(valueSetDefinition.getAbout())) {
                NamedEntityDescription namedEntityDescription = new NamedEntityDescription();
                namedEntityDescription.setAbout(entity.getUri());
                namedEntityDescription.setEntityID(ModelUtils.createScopedEntityName(entity.getName(), entity.getNamespace()));

                CodeSystemVersionReference codeSystemVersionReference = new CodeSystemVersionReference();

                NameAndMeaningReference nameAndMeaningReference = new NameAndMeaningReference();
                nameAndMeaningReference.setUri(valueSetDefinition.getAbout());
                nameAndMeaningReference.setContent(localIdValueSetDefinition.getLocalID());

                codeSystemVersionReference.setVersion(nameAndMeaningReference);

                CodeSystemReference codeSystemReference = new CodeSystemReference();
                codeSystemReference.setContent(valueSetDefinition.getDefinedValueSet().getContent());
                codeSystemReference.setUri(valueSetDefinition.getDefinedValueSet().getUri());

                codeSystemVersionReference.setCodeSystem(codeSystemReference);

                namedEntityDescription.setDescribingCodeSystemVersion(codeSystemVersionReference);

                Designation designation = new Designation();
                designation.setValue(ModelUtils.toTsAnyType(entity.getDesignation()));
                designation.setDesignationRole(DesignationRole.PREFERRED);

                namedEntityDescription.addDesignation(designation);

                EntityDescription entityDescription = new EntityDescription();
                entityDescription.setNamedEntity(namedEntityDescription);

                entities.add(entityDescription);
            }
        }

        if(CollectionUtils.isNotEmpty(entities)) {
            this.arangoDbEntityDescriptionMaintenanceService.importResources(entities);
        }
    }

    @Override
    public Class<LocalIdValueSetDefinition> getStorageClass() {
        return LocalIdValueSetDefinition.class;
    }

    @Override
    public Class<ValueSetDefinition> getResourceClass() {
        return ValueSetDefinition.class;
    }

}
