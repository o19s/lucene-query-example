package com.o19s;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

public class CountingQuery extends CustomScoreQuery {

	public CountingQuery(Query subQuery) {
		super(subQuery);
	}
	
	
	public class CountingQueryScoreProvider extends CustomScoreProvider {

		String _field;
		
		public CountingQueryScoreProvider(String field, AtomicReaderContext context) {
			super(context);
			_field = field;
		}
	
		// Rescores by counting the number of terms in the field
		public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {
			IndexReader r = context.reader();
			Terms tv = r.getTermVector(doc, _field);
			TermsEnum termsEnum = null;
			termsEnum = tv.iterator(termsEnum);
		    int numTerms = 0;
			while((termsEnum.next()) != null) {
		    	numTerms++;
		    }
			return (float)(numTerms);
		}

	
	}
	
	protected CustomScoreProvider getCustomScoreProvider(
			AtomicReaderContext context) throws IOException {
		return new CountingQueryScoreProvider("tag", context);
	}
	
	

}
