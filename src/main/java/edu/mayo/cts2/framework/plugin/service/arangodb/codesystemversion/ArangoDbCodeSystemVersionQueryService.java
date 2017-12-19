package edu.mayo.cts2.framework.plugin.service.arangodb.codesystemversion;

import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Sets;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntryListEntry;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntrySummary;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.TransformUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQuery;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQueryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class ArangoDbCodeSystemVersionQueryService extends AbstractArangoDbDefaultQueryService<CodeSystemVersionCatalogEntry, CodeSystemVersionCatalogEntryListEntry, CodeSystemVersionCatalogEntrySummary, CodeSystemVersionQuery> implements CodeSystemVersionQueryService {

    @Resource
    private CodeSystemVersionStorageInfo codeSystemVersionStorageInfo;

    private Set<String> indexSearchFields = Sets.newHashSet(
            "resourceSynopsis",
            "formalName",
            "valueSetAnalyzed",
            "officialResourceVersionId"
    );

    protected CodeSystemVersionCatalogEntrySummary toDirectoryEntry(VertexEntity<CodeSystemVersionCatalogEntry> node) {
        CodeSystemVersionCatalogEntry resource = node.getEntity();

        CodeSystemVersionCatalogEntrySummary directoryEntry = new CodeSystemVersionCatalogEntrySummary();
        directoryEntry = TransformUtils.baseTransformResourceVersion(directoryEntry, resource);

        directoryEntry.setCodeSystemVersionName(resource.getCodeSystemVersionName());
        directoryEntry.setVersionOf(resource.getVersionOf());
        directoryEntry.setHref(CodeSystemVersionUtils.getHref(resource, this.getUrlConstructor()));

        return directoryEntry;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filter, CodeSystemVersionQuery query, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected Transformer<CodeSystemVersionCatalogEntry, CodeSystemVersionCatalogEntrySummary> getSummarizer() {
        return new Transformer<CodeSystemVersionCatalogEntry, CodeSystemVersionCatalogEntrySummary>() {
            @Override
            public CodeSystemVersionCatalogEntrySummary toSummary(VertexEntity<CodeSystemVersionCatalogEntry> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<CodeSystemVersionCatalogEntry, CodeSystemVersionCatalogEntryListEntry> getLister() {
        return new Transformer<CodeSystemVersionCatalogEntry, CodeSystemVersionCatalogEntryListEntry>() {
            @Override
            public CodeSystemVersionCatalogEntryListEntry toSummary(VertexEntity<CodeSystemVersionCatalogEntry> fullResource) {
                CodeSystemVersionCatalogEntryListEntry entry = new CodeSystemVersionCatalogEntryListEntry();
                entry.setEntry(fullResource.getEntity());

                return entry;
            }
        };
    }

    @Override
    public Class<CodeSystemVersionCatalogEntry> getStorageClass() {
        return CodeSystemVersionCatalogEntry.class;
    }

    @Override
    public StorageInfo<CodeSystemVersionCatalogEntry> getStorageInfo() {
        return this.codeSystemVersionStorageInfo;
    }

}
