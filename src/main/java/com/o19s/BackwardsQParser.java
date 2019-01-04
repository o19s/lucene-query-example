package com.o19s;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

import java.util.ArrayList;
import java.util.List;

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

        String qf = getBestParam(localParams, params, "qf", null);

        // get fields on whitespace
        String[] fields = qf.split("\\s+");

        if (qf == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                    "qf required by backwards query parser");
        }

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (String field: fields) {
            Query btq = new BackwardsTermQuery(field, qText);
            builder.add(btq, BooleanClause.Occur.SHOULD);
        }

        return builder.build();

    }
}
