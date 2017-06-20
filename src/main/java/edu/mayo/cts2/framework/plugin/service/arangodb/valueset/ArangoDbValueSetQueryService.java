package edu.mayo.cts2.framework.plugin.service.arangodb.valueset;

import com.arangodb.entity.marker.VertexEntity;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntryListEntry;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbDefaultQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.TransformUtils;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class ArangoDbValueSetQueryService extends AbstractArangoDbDefaultQueryService<ValueSetCatalogEntry, ValueSetCatalogEntryListEntry, ValueSetCatalogEntrySummary, ValueSetQuery> implements ValueSetQueryService {

    @Resource
    private ValueSetStorageInfo valueSetStorageInfo;

    protected ValueSetCatalogEntrySummary toDirectoryEntry(VertexEntity<ValueSetCatalogEntry> node) {
        ValueSetCatalogEntry resource = node.getEntity();

        ValueSetCatalogEntrySummary directoryEntry = new ValueSetCatalogEntrySummary();
        directoryEntry = TransformUtils.baseTransform(directoryEntry, resource);

        directoryEntry.setValueSetName(resource.getValueSetName());
        directoryEntry.setHref(this.getUrlConstructor().createValueSetUrl(resource.getValueSetName()));

        return directoryEntry;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filters, ValueSetQuery query, Page page) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected Transformer<ValueSetCatalogEntry, ValueSetCatalogEntrySummary> getSummarizer() {
        return new Transformer<ValueSetCatalogEntry, ValueSetCatalogEntrySummary>() {
            @Override
            public ValueSetCatalogEntrySummary toSummary(VertexEntity<ValueSetCatalogEntry> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<ValueSetCatalogEntry, ValueSetCatalogEntryListEntry> getLister() {
        return new Transformer<ValueSetCatalogEntry, ValueSetCatalogEntryListEntry>() {
            @Override
            public ValueSetCatalogEntryListEntry toSummary(VertexEntity<ValueSetCatalogEntry> fullResource) {
                ValueSetCatalogEntryListEntry entry = new ValueSetCatalogEntryListEntry();
                entry.setEntry(fullResource.getEntity());

                return entry;
            }
        };
    }

    @Override
    public Class<ValueSetCatalogEntry> getStorageClass() {
        return ValueSetCatalogEntry.class;
    }

    @Override
    public StorageInfo<ValueSetCatalogEntry> getStorageInfo() {
        return this.valueSetStorageInfo;
    }

}
