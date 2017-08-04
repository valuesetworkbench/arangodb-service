package edu.mayo.cts2.framework.plugin.service.arangodb.valuesetdefinintion;

import com.arangodb.entity.DocumentEntity;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbLocalIdReadService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlQuery;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionHistoryService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ArangoDbValueSetDefinitionReadService extends AbstractArangoDbLocalIdReadService<LocalIdValueSetDefinition, ValueSetDefinition, ValueSetDefinitionReadId>
        implements ValueSetDefinitionReadService, ValueSetDefinitionHistoryService {

    @Resource
    private ValueSetDefinitionStorageInfo valueSetDefinitionStorageInfo;

    @Override
    protected boolean isUriQuery(ValueSetDefinitionReadId identifier) {
        return StringUtils.isNotBlank(identifier.getUri());
    }

    @Override
    protected String getUri(ValueSetDefinitionReadId identifier) {
        return identifier.getUri();
    }

    @Override
    protected String getName(ValueSetDefinitionReadId identifier) {
        return identifier.getName();
    }

    @Override
    public List<VersionTagReference> getSupportedTags() {
        return Arrays.asList(ArangoDbServiceConstants.PRODUCTION_TAG);
    }

    @Override
    public LocalIdValueSetDefinition readByTag(NameOrURI parentIdentifier, VersionTagReference tag, ResolvedReadContext readContext) {
        DocumentEntity<LocalIdValueSetDefinition> document = this.doReadFromStorage(this.getByTagAql(parentIdentifier, tag));

        if(document == null) {
            return null;
        } else {
            return this.decorateEntity(document.getEntity());
        }
    }

    @Override
    public boolean existsByTag(NameOrURI parentIdentifier, VersionTagReference tag, ResolvedReadContext readContext) {
        throw new UnsupportedOperationException("not implemented");
    }

    private AqlQuery getByTagAql(NameOrURI valueSet, VersionTagReference tagReference) {
        String aql = "FOR x IN " + this.getStorageInfo().getCollection()
                + " FILTER x " + (this.getResourcePath() == null ? "" : "." + this.getResourcePath()) + ".definedValueSet.content == @valueSet"
                + " FILTER @tag IN x" + (this.getResourcePath() == null ? "" : "." + this.getResourcePath()) + ".versionTagList[*].content RETURN x";

        Map<String,Object> params = Maps.newHashMap();
        params.put("valueSet", valueSet.getName());
        params.put("tag", tagReference.getContent());

        return new AqlQuery(aql, params);
    }

    @Override
    protected LocalIdValueSetDefinition toLocalIdResource(String key, ValueSetDefinition resource) {
        return new LocalIdValueSetDefinition(key, resource);
    }

    @Override
    public Class<LocalIdValueSetDefinition> getStorageClass() {
        return LocalIdValueSetDefinition.class;
    }

    @Override
    public StorageInfo<LocalIdValueSetDefinition> getStorageInfo() {
        return this.valueSetDefinitionStorageInfo;
    }

}
