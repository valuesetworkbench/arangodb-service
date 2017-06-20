package edu.mayo.cts2.framework.plugin.service.arangodb.codesystem;

import com.arangodb.entity.marker.VertexEntity;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryListEntry;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntrySummary;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.TransformUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.profile.ResourceQuery;
import edu.mayo.cts2.framework.service.profile.codesystem.CodeSystemQueryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class ArangoDbCodeSystemQueryService extends AbstractArangoDbDefaultQueryService<CodeSystemCatalogEntry, CodeSystemCatalogEntryListEntry, CodeSystemCatalogEntrySummary, ResourceQuery> implements CodeSystemQueryService {

    @Resource
    private CodeSystemStorageInfo codeSystemStorageInfo;

    protected CodeSystemCatalogEntrySummary toDirectoryEntry(VertexEntity<CodeSystemCatalogEntry> node) {
        CodeSystemCatalogEntry resource = node.getEntity();

        CodeSystemCatalogEntrySummary directoryEntry = new CodeSystemCatalogEntrySummary();
        directoryEntry = TransformUtils.baseTransform(directoryEntry, resource);

        directoryEntry.setCodeSystemName(resource.getCodeSystemName());
        directoryEntry.setHref(CodeSystemUtils.getHref(resource, this.getUrlConstructor()));

        return directoryEntry;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filter, ResourceQuery query, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected Transformer<CodeSystemCatalogEntry, CodeSystemCatalogEntrySummary> getSummarizer() {
        return new Transformer<CodeSystemCatalogEntry, CodeSystemCatalogEntrySummary>() {
            @Override
            public CodeSystemCatalogEntrySummary toSummary(VertexEntity<CodeSystemCatalogEntry> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<CodeSystemCatalogEntry, CodeSystemCatalogEntryListEntry> getLister() {
        return new Transformer<CodeSystemCatalogEntry, CodeSystemCatalogEntryListEntry>() {
            @Override
            public CodeSystemCatalogEntryListEntry toSummary(VertexEntity<CodeSystemCatalogEntry> fullResource) {
                CodeSystemCatalogEntryListEntry entry = new CodeSystemCatalogEntryListEntry();
                entry.setEntry(fullResource.getEntity());

                return entry;
            }
        };
    }

    @Override
    public Class<CodeSystemCatalogEntry> getStorageClass() {
        return CodeSystemCatalogEntry.class;
    }

    @Override
    public StorageInfo<CodeSystemCatalogEntry> getStorageInfo() {
        return this.codeSystemStorageInfo;
    }

}
