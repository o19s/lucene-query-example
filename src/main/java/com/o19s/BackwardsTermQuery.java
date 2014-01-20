package com.o19s;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

// Query that assigns a lot of points for a term occuring backwards,
// but some points for a term occuring forwards
public class BackwardsTermQuery extends Query {
	
	TermQuery backwardsQuery;
	TermQuery forwardsQuery;

	public BackwardsTermQuery(String field, String term) {
		// A wrapped TermQuery for the reverse string
		Term backwardsTerm = new Term(field, new StringBuilder(term).reverse().toString());
		backwardsQuery = new TermQuery(backwardsTerm);
		// A wrapped TermQuery for the Forward
		Term forwardsTerm = new Term(field, term);
		forwardsQuery = new TermQuery(forwardsTerm);
	}
	
	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		return new BackwardsWeight(searcher);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		if (getClass() != other .getClass()) {
			return false;
		}
		BackwardsTermQuery otherQ = (BackwardsTermQuery)(other);		
		if (otherQ.getBoost() != getBoost()) {
			return false;
		}
		return otherQ.backwardsQuery.equals(backwardsQuery) && otherQ.forwardsQuery.equals(forwardsQuery);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + backwardsQuery.hashCode() + forwardsQuery.hashCode(); 
	}
	
	public class BackwardsWeight extends Weight {
		
		Weight backwardsWeight = null;
		Weight forwardsWeight = null;
		
		public BackwardsWeight(IndexSearcher searcher) throws IOException {
			super();
			backwardsWeight = backwardsQuery.createWeight(searcher);
			forwardsWeight = forwardsQuery.createWeight(searcher);
		}
		
		@Override
		public Explanation explain(AtomicReaderContext context, int doc)
				throws IOException {
			return null;
		}
	
		@Override
		public Query getQuery() {
			return BackwardsTermQuery.this ;
		}
	
		@Override
		public float getValueForNormalization() throws IOException {
			return backwardsWeight.getValueForNormalization() + 
					forwardsWeight.getValueForNormalization();
		}
	
		@Override
		public void normalize(float norm, float topLevelBoost) {
			backwardsWeight.normalize(norm, topLevelBoost);
			forwardsWeight.normalize(norm, topLevelBoost);
		}
	
		@Override
		public Scorer scorer(AtomicReaderContext context, boolean scoreDocsInOrder,
				boolean topScorer, Bits acceptDocs) throws IOException {
			// TODO Auto-generated method stub
			Scorer backwardsScorer = backwardsWeight.scorer(context, scoreDocsInOrder, topScorer, acceptDocs);
			Scorer forwardsScorer = forwardsWeight.scorer(context, scoreDocsInOrder, topScorer, acceptDocs);
			return new BackwardsScorer(this, context, backwardsScorer, forwardsScorer);
		}
	}
	
	public class BackwardsScorer extends Scorer {

		final float BACKWARDS_SCORE = 5.0f;
		final float FORWARDS_SCORE = 1.0f;
		float currScore = 0.0f;
		
		Scorer backwardsScorer = null;
		Scorer forwardsScorer = null;
		
		protected BackwardsScorer(Weight weight, AtomicReaderContext context,
								  Scorer _backwardsScorer, Scorer _forwardsScorer) throws IOException {
			super(weight);
			backwardsScorer = _backwardsScorer;
			forwardsScorer = _forwardsScorer;
		}

		@Override
		public float score() throws IOException {
			return currScore;
		}

		@Override
		public int freq() throws IOException {
			return 1;
		}

		@Override
		public int docID() {
			int backwordsDocId = backwardsScorer.docID();
			int forwardsDocId = forwardsScorer.docID();
			if (backwordsDocId <= forwardsDocId && backwordsDocId != NO_MORE_DOCS) {
				currScore = BACKWARDS_SCORE;
				return backwordsDocId;
			} else if (forwardsDocId != NO_MORE_DOCS) {
				currScore = FORWARDS_SCORE;
				return forwardsDocId;
			}
			return NO_MORE_DOCS;
		}	

		@Override
		public int nextDoc() throws IOException {
			int currDocId = docID();
			// increment one or both
			if (currDocId == backwardsScorer.docID()) {
				backwardsScorer.nextDoc();
			}
			if (currDocId == forwardsScorer.docID()) {
				forwardsScorer.nextDoc();
			}
			return docID();
		}

		@Override
		public int advance(int target) throws IOException {
			backwardsScorer.advance(target);
			forwardsScorer.advance(target);
			return docID();
		}

		@Override
		public long cost() {
			return 1;
		}
		
	}

	@Override
	public String toString(String field) {
		return "BackwardsQuery: " + backwardsQuery.toString();
	}

}
