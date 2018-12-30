package com.o19s;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

// Note CustomScoreQuery is deprecated
//  https://issues.apache.org/jira/browse/LUCENE-8099
public class CountingQuery extends CustomScoreQuery {

	public CountingQuery(Query subQuery) {
		super(subQuery);
	}
	
	
	public class CountingQueryScoreProvider extends CustomScoreProvider {

		String _field;
		
		public CountingQueryScoreProvider(String field, LeafReaderContext context) {
			super(context);
			_field = field;
		}
	
		// Rescores by counting the number of terms in the field
		public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {
			IndexReader r = context.reader();
			Terms tv = r.getTermVector(doc, _field);
			TermsEnum termsEnum = tv.iterator();
		    int numTerms = 0;
			while((termsEnum.next()) != null) {
		    	numTerms++;
		    }
			return (float)(numTerms);
		}

	
	}
	
	protected CustomScoreProvider getCustomScoreProvider(
			LeafReaderContext context) throws IOException {
		return new CountingQueryScoreProvider("tag", context);
	}
	
	

}
