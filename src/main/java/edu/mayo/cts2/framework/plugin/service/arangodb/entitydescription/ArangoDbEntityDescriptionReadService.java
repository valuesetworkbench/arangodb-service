package edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription;

import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.EntityReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlQuery;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class ArangoDbEntityDescriptionReadService extends AbstractArangoDbDefaultReadService<EntityDescription, EntityDescriptionReadId> implements EntityDescriptionReadService {

    @Resource
    private EntityDescriptionStorageInfo entityDescriptionStorageInfo;

    @Override
    public Class<EntityDescription> getStorageClass() {
        return EntityDescription.class;
    }

    @Override
    protected AqlQuery getNameFilter(EntityDescriptionReadId identifier) {
        String aql = "FILTER x.namedEntity.entityID.name == @name AND x.namedEntity.entityID.namespace == @namespace";

        String entityName = identifier.getEntityName().getName();
        String entityNamespace = identifier.getEntityName().getNamespace();

        Map<String,Object> parameters = Maps.newHashMap();
        parameters.put("name", entityName);
        parameters.put("namespace", entityNamespace);

        return new AqlQuery(aql, parameters);
    }

    @Override
    protected String getResourcePath() {
        return "namedEntity";
    }

    @Override
    protected boolean isUriQuery(EntityDescriptionReadId identifier) {
        return StringUtils.isNotBlank(identifier.getUri());
    }

    @Override
    protected String getUri(EntityDescriptionReadId identifier) {
        return identifier.getUri();
    }

    @Override
    public DirectoryResult<EntityListEntry> readEntityDescriptions(EntityNameOrURI entityId, SortCriteria sortCriteria, ResolvedReadContext readContext, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public EntityReference availableDescriptions(EntityNameOrURI entityId, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<EntityListEntry> readEntityDescriptions(EntityNameOrURI entityId, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<CodeSystemReference> getKnownCodeSystems() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<CodeSystemVersionReference> getKnownCodeSystemVersions() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<VersionTagReference> getSupportedVersionTags() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected String getUriPath() {
        return EntityDescriptionConstants.URI_PATH;
    }

    @Override
    public StorageInfo getStorageInfo() {
        return this.entityDescriptionStorageInfo;
    }

}
