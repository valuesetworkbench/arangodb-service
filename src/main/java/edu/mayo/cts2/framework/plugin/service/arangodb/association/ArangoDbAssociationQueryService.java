package edu.mayo.cts2.framework.plugin.service.arangodb.association;

import com.arangodb.VertexCursor;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.core.util.EncodingUtils;
import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.association.types.GraphDirection;
import edu.mayo.cts2.framework.model.association.types.GraphFocus;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
import edu.mayo.cts2.framework.plugin.service.arangodb.AbstractArangoDbLocalIdQueryService;
import edu.mayo.cts2.framework.plugin.service.arangodb.AqlDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDao;
import edu.mayo.cts2.framework.plugin.service.arangodb.StorageInfo;
import edu.mayo.cts2.framework.plugin.service.arangodb.Transformer;
import edu.mayo.cts2.framework.plugin.service.arangodb.entitydescription.ArangoDbEntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.association.AssociationQuery;
import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ArangoDbAssociationQueryService extends AbstractArangoDbLocalIdQueryService<LocalIdAssociation, Association, Association, AssociationDirectoryEntry, AssociationQuery> implements AssociationQueryService {

    @Resource
    private UrlConstructor urlConstructor;

    @Resource
    private ArangoDao arangoDao;

    @Resource
    private ArangoDbEntityDescriptionReadService documentReader;

    @Resource
    private AssociationStorageInfo associationStorageInfo;

    protected AssociationDirectoryEntry toDirectoryEntry(VertexEntity<LocalIdAssociation> node) {
        Association fullEntity = node.getEntity().getResource();

        AssociationDirectoryEntry directoryEntry = new AssociationDirectoryEntry();

        //TODO:

        return directoryEntry;
    }

    @Override
    protected AqlDirectoryBuilder.LuceneQuery doFilter(Set<ResolvedFilter> filter, AssociationQuery query, Page page) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Transformer<LocalIdAssociation, AssociationDirectoryEntry> getSummarizer() {
        return new Transformer<LocalIdAssociation, AssociationDirectoryEntry>() {
            @Override
            public AssociationDirectoryEntry toSummary(VertexEntity<LocalIdAssociation> fullResource) {
                return toDirectoryEntry(fullResource);
            }
        };
    }

    @Override
    protected Transformer<LocalIdAssociation, Association> getLister() {
        return new Transformer<LocalIdAssociation, Association>() {
            @Override
            public Association toSummary(VertexEntity<LocalIdAssociation> fullResource) {
                //TOOD:

                return null;
            }
        };
    }

    public static class URIAndEntityNameAndCsv {
        private URIAndEntityName entity;
        private CodeSystemVersionReference csv;

        public CodeSystemVersionReference getCsv() {
            return csv;
        }

        public void setCsv(CodeSystemVersionReference csv) {
            this.csv = csv;
        }

        public URIAndEntityName getEntity() {
            return entity;
        }

        public void setEntity(URIAndEntityName entity) {
            this.entity = entity;
        }
    }

    public static class GraphResult {
        private URIAndEntityNameAndCsv from;
        private URIAndEntityNameAndCsv to;

        public URIAndEntityNameAndCsv getFrom() {
            return from;
        }

        public void setFrom(URIAndEntityNameAndCsv from) {
            this.from = from;
        }

        public URIAndEntityNameAndCsv getTo() {
            return to;
        }

        public void setTo(URIAndEntityNameAndCsv to) {
            this.to = to;
        }
    }

    @Override
    public DirectoryResult<GraphNode> getAssociationGraph(GraphFocus focusType, EntityDescriptionReadId focusEntity, GraphDirection direction, long depth) {
        String aql = "FOR e IN GRAPH_EDGES(\"Association\", @entityId, {maxDepth: @depth, includeData: true, direction: @direction, edgeExamples: [{isHierarchy: true}]})\n" +
                    "    for to in EntityDescription\n" +
                    "        filter to._id == e._to\n" +
                    "    for from in EntityDescription\n" +
                    "        filter from._id == e._from\n" +
                    "RETURN {rel: e.rel, from: {" +
                    "                        csv: from.namedEntity.describingCodeSystemVersion, \n" +
                    "                        entity: {uri: from.namedEntity.about, \n" +
                    "                        designation: from.namedEntity.designationList[0].value.content, \n" +
                    "                        name: from.namedEntity.entityID.name, \n" +
                    "                        namespace: from.namedEntity.entityID.namespace}}, \n" +
                    "                    to: {" +
                    "                        csv: to.namedEntity.describingCodeSystemVersion, \n" +
                    "                        entity: {uri: to.namedEntity.about, \n" +
                    "                        designation: to.namedEntity.designationList[0].value.content, \n" +
                    "                        name: to.namedEntity.entityID.name, \n" +
                    "                        namespace: to.namedEntity.entityID.namespace}}}";

        String entityId = this.documentReader.getDocumentHandle(focusEntity, null);
        Map<String,Object> params = Maps.newHashMap();
        params.put("entityId", entityId);
        params.put("depth", depth);
        params.put("direction", direction.equals(GraphDirection.REVERSE) ? "inbound" : "outbound");

        VertexCursor<GraphResult> results = this.arangoDao.query(aql, params, GraphResult.class);

        List<GraphNode> graphNodeList = Lists.newArrayList();

        for(DocumentEntity<GraphResult> document : results) {
            GraphResult result = document.getEntity();

            URIAndEntityName from = this.setHref(result.getFrom());
            URIAndEntityName to = this.setHref(result.getTo());

            GraphNode graphNode = new GraphNode();
            graphNode.setNodeEntity(direction.equals(GraphDirection.FORWARD) ? from : to);
            graphNode.setSubject(from);

            StatementTarget target = new StatementTarget();
            target.setEntity(to);

            graphNode.setTarget(target);

            graphNodeList.add(graphNode);
        }

        return new DirectoryResult<>(Lists.newArrayList(graphNodeList), true);
    }

    private URIAndEntityName setHref(URIAndEntityNameAndCsv uriAndEntityNameAndCsv) {
        URIAndEntityName uriAndEntityName = uriAndEntityNameAndCsv.getEntity();
        CodeSystemVersionReference codeSystemVersionReference = uriAndEntityNameAndCsv.getCsv();

        uriAndEntityName.setHref(this.getUrlConstructor().createEntityUrl(
                codeSystemVersionReference.getCodeSystem().getContent(),
                codeSystemVersionReference.getVersion().getContent(),
                EncodingUtils.encodeScopedEntityName(uriAndEntityName)));

        return uriAndEntityName;
    }

    @Override
    public Class<LocalIdAssociation> getStorageClass() {
        return LocalIdAssociation.class;
    }

    @Override
    public StorageInfo<LocalIdAssociation> getStorageInfo() {
        return this.associationStorageInfo;
    }

}
