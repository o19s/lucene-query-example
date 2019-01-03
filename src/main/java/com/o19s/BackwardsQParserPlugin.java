package com.o19s;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class BackwardsQParserPlugin extends QParserPlugin {
    public QParser createParser(String s,
                                SolrParams localParams,
                                SolrParams globalPraams,
                                SolrQueryRequest solrQueryRequest) {
        return new BackwardsQParser(s, localParams, globalPraams, solrQueryRequest);
    }
}
