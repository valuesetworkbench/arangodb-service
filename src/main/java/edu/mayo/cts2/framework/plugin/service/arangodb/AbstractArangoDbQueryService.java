package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.filter.directory.DirectoryBuilder;
import edu.mayo.cts2.framework.filter.match.StateAdjustingComponentReference;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.ComponentReference;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.QueryService;
import edu.mayo.cts2.framework.service.profile.ResourceQuery;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractArangoDbQueryService<D,R,L,S,Q extends ResourceQuery> extends AbstractArangoDbResourceService<D> implements QueryService<L,S,Q> {

    @Resource
    private ArangoDao arangoDao;

    @Override
    public DirectoryResult<S> getResourceSummaries(Q query, SortCriteria sortCriteria, Page page) {
        if(page == null) {
            page = new Page();
        }

        DirectoryBuilder<S> directoryBuilder = this.getDirectoryBuilder(query, this.getSummarizer(), page);

        directoryBuilder.addStart(page.getStart());
        directoryBuilder.addMaxToReturn(page.getMaxToReturn());

        return directoryBuilder.resolve();
    }

    protected List getDefaultSortFields() {
        // no-op
        return null;
    }

    protected final <Z> DirectoryBuilder<Z> getDirectoryBuilder(Q query, Transformer<D,Z> transformer, Page page) {
        AqlDirectoryBuilder<D,Z> directoryBuilder = this.addState(query, new AqlDirectoryBuilder<D,Z>(
                this.arangoDao,
                this.getCollection(),
                this.getResourcePath(),
                this.getUriPath(),
                transformer,
                this.getStorageClass(),
                this.getSupportedMatchAlgorithms(),
                new HashSet<StateAdjustingComponentReference<List<AqlDirectoryBuilder.AqlState>>>(),
                this.getDefaultSortFields()), page);

        return this.filter(query, directoryBuilder, page);
    }

    protected <T> AqlDirectoryBuilder<D,T> addState(Q query, AqlDirectoryBuilder<D,T> builder, Page page) {
        // no-op
        return builder;
    }

    protected final <T> AqlDirectoryBuilder<D,T> filter(Q query, AqlDirectoryBuilder<D,T> builder, Page page) {
        if(query != null && CollectionUtils.isNotEmpty(query.getFilterComponent())) {
            builder.addState(this.doFilter(query.getFilterComponent(), query, page));
        }

        return builder;
    }

    protected abstract AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filter, Q query, Page page);

    protected abstract Transformer<D,S> getSummarizer();

    protected abstract Transformer<D,L> getLister();

    @Override
    public DirectoryResult<L> getResourceList(Q query, SortCriteria sortCriteria, Page page) {
        if(page == null) {
            page = new Page();
        }

        DirectoryBuilder<L> directoryBuilder = this.getDirectoryBuilder(query, this.getLister(), page);

        directoryBuilder.addStart(page.getStart());
        directoryBuilder.addMaxToReturn(page.getMaxToReturn());

        return directoryBuilder.resolve();
    }

    @Override
    public int count(Q query) {
        DirectoryBuilder<L> directoryBuilder = this.getDirectoryBuilder(query, this.getLister(), null);

        if(query != null) {
            directoryBuilder.restrict(query.getFilterComponent());
        }

        return directoryBuilder.count();
    }

    @Override
    public Set<MatchAlgorithmReference> getSupportedMatchAlgorithms() {
        Set<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();

        returnSet.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
        returnSet.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference());
        returnSet.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference());

        return returnSet;
    }

    @Override
    public Set<ComponentReference> getSupportedSearchReferences() {
        Set<ComponentReference> returnSet = new HashSet<ComponentReference>();

        returnSet.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference());
        returnSet.add(StandardModelAttributeReference.ABOUT.getComponentReference());
        returnSet.add(StandardModelAttributeReference.RESOURCE_NAME.getComponentReference());
        returnSet.add(StandardModelAttributeReference.DESIGNATION.getComponentReference());
        returnSet.add(StandardModelAttributeReference.KEYWORD.getComponentReference());

        ComponentReference owner = new ComponentReference();
        owner.setAttributeReference("owner");
        returnSet.add(owner);

        return returnSet;
    }

    @Override
    public Set<? extends ComponentReference> getSupportedSortReferences() {
        return new HashSet<>();
    }

    @Override
    public Set<PredicateReference> getKnownProperties() {
        return new HashSet<>();
    }

    protected String getCollection() {
        return this.getStorageInfo().getCollection();
    }

}
