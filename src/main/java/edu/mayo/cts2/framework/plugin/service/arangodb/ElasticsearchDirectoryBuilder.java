package edu.mayo.cts2.framework.plugin.service.arangodb;

import edu.mayo.cts2.framework.filter.directory.AbstractStateBuildingDirectoryBuilder;
import edu.mayo.cts2.framework.filter.match.StateAdjustingComponentReference;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import org.apache.lucene.search.BooleanQuery;

import java.util.Set;

public class ElasticsearchDirectoryBuilder<T> extends AbstractStateBuildingDirectoryBuilder<BooleanQuery.Builder, T> {

    public ElasticsearchDirectoryBuilder(BooleanQuery.Builder initialState, Callback<BooleanQuery.Builder, T> callback, Set<MatchAlgorithmReference> matchAlgorithmReferences, Set<StateAdjustingComponentReference<BooleanQuery.Builder>> stateAdjustingPropertyReferences) {
        super(initialState, callback, matchAlgorithmReferences, stateAdjustingPropertyReferences);
    }

}
