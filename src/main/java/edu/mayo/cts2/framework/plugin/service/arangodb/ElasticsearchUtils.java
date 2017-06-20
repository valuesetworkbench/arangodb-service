package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Set;

public final class ElasticsearchUtils {

    public static final BoolQueryBuilder buildQueryFromFilter(BoolQueryBuilder queryBuilder, ResolvedFilter filter, Set<String> fields) {
        if ("owner".equals(filter.getComponentReference().getAttributeReference())) {
            BoolQueryBuilder overall = QueryBuilders.boolQuery();
            overall.must(queryBuilder);

            String[] owners = StringUtils.split(filter.getMatchValue(), '|');

            BoolQueryBuilder ownersBuilder = QueryBuilders.boolQuery().minimumNumberShouldMatch(1);

            for(String owner : owners) {
                ownersBuilder.should(QueryBuilders.termQuery("owner", owner));
            }

            overall.must(ownersBuilder);

            return overall;
        } else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().
                    minimumNumberShouldMatch(1);

            for(String field : fields) {
                boolQueryBuilder = boolQueryBuilder.should(
                            QueryBuilders.matchQuery(field, filter.getMatchValue()).operator(MatchQueryBuilder.Operator.AND));
            }

            return queryBuilder.must(boolQueryBuilder);
        }
    }

}
