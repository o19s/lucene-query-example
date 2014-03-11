package com.o19s;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

public class BackwardsTermCustomQuery extends CustomScoreQuery {

	public BackwardsTermCustomQuery(Query subQuery) {
		super(subQuery);
		// TODO Auto-generated constructor stub
	}
	
	protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
	    return new BackwardsScoreProvider(context);
	}

}
