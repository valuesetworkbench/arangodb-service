package edu.mayo.cts2.framework.plugin.service.arangodb;

import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.ComponentReference;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.service.profile.HistoryService;
import edu.mayo.cts2.framework.service.profile.ReadService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractArangoDbBaseReadService<S,R,I> extends AbstractArangoDbResourceService<S> implements ArangoDbDocumentReader<S,I>, ReadService<S,I>, HistoryService<R,I> {

    @Resource
    private ArangoDao arangoDao;

    @Override
    public final S read(I identifier, ResolvedReadContext readContext) {
        DocumentEntity<S> document = this.readDocument(identifier, readContext);

        if(document == null) {
            return null;
        } else {
            return this.decorateEntity(document.getEntity());
        }
    }

    protected S decorateEntity(S entity) {
        return entity;
    }

    @Override
    public boolean exists(I identifier, ResolvedReadContext readContext) {
        return this.read(identifier, readContext) != null;
    }

    protected DocumentEntity<S> doReadFromStorage(AqlQuery aqlQuery) {
        return this.arangoDao.readDocument(
                aqlQuery.getAql(),
                aqlQuery.getParameters(),
                this.getStorageClass());
    }

    protected List<DocumentEntity<S>> doReadListFromStorage(AqlQuery aqlQuery) {
        DocumentCursor<S> cursor = null;
        try {
            cursor = this.arangoDao.getDriver().executeDocumentQuery(
                    aqlQuery.getAql(),
                    aqlQuery.getParameters(),
                    null,
                    this.getStorageClass());
        } catch (ArangoException e) {
            throw new RuntimeException(e);
        }

        return cursor.asList();
    }

    @Override
    public DocumentEntity<S> readDocument(I identifier, ResolvedReadContext resolvedReadContext) {
        AqlQuery aqlQuery = this.getAql(identifier, resolvedReadContext);

        return this.doReadFromStorage(aqlQuery);
    }

    //TODO: Optimize this -- don't need to get the whole document.
    @Override
    public String getDocumentHandle(I identifier, ResolvedReadContext resolvedReadContext) {
        return this.readDocument(identifier, resolvedReadContext).getDocumentHandle();
    }

    public List<DocumentEntity<S>> readDocumentWithHistory(I identifier) {
        AqlQuery aqlQuery = this.getAqlForHistory(identifier);

        return this.doReadListFromStorage(aqlQuery);
    }

    protected abstract AqlQuery getNameFilter(I identifier);

    private AqlQuery getUriFilter(I identifier) {
        String uriPath = this.getUriPath();

        String aql = "FILTER x." + uriPath + " == @uri";

        Map<String,Object> params = Maps.newHashMap();
        params.put("uri", this.getUri(identifier));

        return new AqlQuery(aql, params);
    }

    protected abstract boolean isUriQuery(I identifier);

    protected abstract String getUri(I identifier);

    protected AqlQuery getAqlForHistory(I identifier) {
        String collection = this.getStorageInfo().getCollection() + ArangoDbServiceConstants.HISTORY_COLLECTION_SUFFIX;

        AqlQuery filter;

        if(this.isUriQuery(identifier)) {
            filter = this.getUriFilter(identifier);
        } else {
            filter = this.getNameFilter(identifier);
        }

        String aql = "FOR x IN " + collection + " " + filter.getAql() + " SORT x.changeableElementGroup.changeDescription.changeDate RETURN x";

        Map<String,Object> params = Maps.newHashMap();
        params.putAll(filter.getParameters());

        return new AqlQuery(aql, params);
    }

    protected String getResourcePath() {
        return null;
    }

    protected AqlQuery getAql(I identifier, ResolvedReadContext resolvedReadContext) {
        String collection;

        String historyAql = "";
        Map<String,Object> historyParams = Maps.newHashMap();
        if(resolvedReadContext != null && resolvedReadContext.getReferenceTime() != null) {
            Date effectiveDate = resolvedReadContext.getReferenceTime();

            AqlQuery effectiveDateFilter = AqlUtils.getEffectiveDateFilterAql(
                    effectiveDate,
                    "x",
                    this.getResourcePath(),
                    this.getUriPath(),
                    this.getStorageInfo().getCollection() + ArangoDbServiceConstants.HISTORY_COLLECTION_SUFFIX);

            historyAql = effectiveDateFilter.getAql();
            historyParams = effectiveDateFilter.getParameters();

            collection = this.getStorageInfo().getCollection() + ArangoDbServiceConstants.HISTORY_COLLECTION_SUFFIX;
        } else {
            collection = this.getStorageInfo().getCollection();
        }

        AqlQuery filter;
        if(this.isUriQuery(identifier)) {
            filter = this.getUriFilter(identifier);
        } else {
            filter = this.getNameFilter(identifier);
        }

        String aql = "FOR x IN " + collection + " " + filter.getAql() + " " + historyAql + " RETURN x";

        Map<String,Object> params = Maps.newHashMap();
        params.putAll(filter.getParameters());
        params.putAll(historyParams);

        return new AqlQuery(aql, params);
    }

    @Override
    public Date getEarliestChange() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Date getLatestChange() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public DirectoryResult<R> getChangeHistory(I identifier, Date fromDate, Date toDate) {
        List<DocumentEntity<S>> history = this.doReadListFromStorage(this.getAqlForHistory(identifier));

        List<R> list = new ArrayList<R>();
        for(DocumentEntity<S> documentEntity : history) {
            list.add(this.unwrap(documentEntity.getEntity()));
        }

       return new DirectoryResult<R>(list, true);
    }

    protected abstract R unwrap(S resource);

    @Override
    public R getEarliestChangeFor(I identifier) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public R getLastChangeFor(I identifier) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public DirectoryResult<R> getChangeHistoryFor(I identifier) {
        return this.getChangeHistory(identifier, null, null);
    }

    @Override
    public Set<PredicateReference> getKnownProperties() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<? extends ComponentReference> getSupportedSortReferences() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<? extends ComponentReference> getSupportedSearchReferences() {
        throw new UnsupportedOperationException("not implemented");
    }

}
