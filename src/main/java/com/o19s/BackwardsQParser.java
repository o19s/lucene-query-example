package com.o19s;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

public class BackwardsQParser extends QParser {

    // Return a parameter if available in localParams,
    // Otherwise fallback to globalParams
    private String getBestParam(SolrParams localParams, SolrParams globalParams, String parameter, String defVal) {
        SolrParams preferredParams = localParams;
        if (preferredParams == null) {
            preferredParams = params;
        }
        return preferredParams.get(parameter, defVal);
    }

    // Return the user's actual query. Either the global qStr or this queries 'v' parameter
    // which is a hack to allow local params to have its own query string
    private String getQueryText(String qStr,  SolrParams localParams, SolrParams globalParams) {
        if (qStr == null) {
            return localParams.get(QueryParsing.V);
        }
        return qStr;
    }

    public BackwardsQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    public Query parse() throws SyntaxError {
        String qText = getQueryText(qstr, localParams, params);

        String field = getBestParam(localParams, params, "qf", null);

        if (field == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                    "qf required by backwards query parser");
        }

        return new BackwardsTermQuery(field, qText);
    }
}
