package com.o19s;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;
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
	public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
		return new BackwardsWeight(searcher);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
        if (other == null) {
            return false;
        }
		if (getClass() != other.getClass()) {
			return false;
		}
		BackwardsTermQuery otherQ = (BackwardsTermQuery)(other);
		return otherQ.backwardsQuery.equals(backwardsQuery) && otherQ.forwardsQuery.equals(forwardsQuery);
	}
	
	@Override
	public int hashCode() {
		return classHash() + backwardsQuery.hashCode() + forwardsQuery.hashCode();
	}
	
	public class BackwardsWeight extends Weight {
		
		Weight backwardsWeight = null;
		Weight forwardsWeight = null;
		
		public BackwardsWeight(IndexSearcher searcher) throws IOException {
			super(BackwardsTermQuery.this);
			backwardsWeight = backwardsQuery.createWeight(searcher, true, 1.0f);
			forwardsWeight = forwardsQuery.createWeight(searcher, true, 1.0f);
		}
		
		@Override
		public Explanation explain(LeafReaderContext context, int doc)
				throws IOException {

			BackwardsScorer scorer = _scorer(context);

			scorer.iterator().advance(doc);
			return scorer.explain();
		}

        public boolean isCacheable(LeafReaderContext leaf) {
		    return false;
        }

        public void extractTerms(Set<Term> terms) {

        }

        private BackwardsScorer _scorer(LeafReaderContext context) throws IOException {
            DocIdSetIterator backIter = DocIdSetIterator.empty();
            DocIdSetIterator fwdIter = DocIdSetIterator.empty();
            Scorer backwardsScorer = backwardsWeight.scorer(context);
            Scorer forwardsScorer = forwardsWeight.scorer(context);

            if (backwardsScorer != null) {
                backIter = backwardsScorer.iterator();
            }
            if (forwardsScorer != null) {
                fwdIter = forwardsScorer.iterator();
            }
            return new BackwardsScorer(this, context, backIter, fwdIter);

        }
	
		@Override
		public Scorer scorer(LeafReaderContext context) throws IOException {
		    return this._scorer(context);
		}
	}
	
	public class BackwardsScorer extends Scorer {

		final float BACKWARDS_SCORE = 5.0f;
		final float FORWARDS_SCORE = 1.0f;
		float currScore = 0.0f;
		
		DocIdSetIterator backwardsIterator = null;
        DocIdSetIterator forwardIterator = null;
        DocIdSetIterator iter = null;

		protected BackwardsScorer(Weight weight, LeafReaderContext context,
								  DocIdSetIterator backwardsIter, DocIdSetIterator forwardsIter) throws IOException {
			super(weight);
            backwardsIterator = backwardsIter;
            forwardIterator = forwardsIter;
		}

		public Explanation explain() {
            if (docID() == backwardsIterator.docID()) {
                return Explanation.match(BACKWARDS_SCORE, "Backwards term match " + this.getWeight().getQuery());
            } else if (docID() == forwardIterator.docID()) {
                return Explanation.match(FORWARDS_SCORE, "Forward term match " + this.getWeight().getQuery());
            }
            return null;
        }

		@Override
		public float score() throws IOException {
			if (docID() == backwardsIterator.docID()) {
			    return BACKWARDS_SCORE;
            } else if (docID() == forwardIterator.docID()) {
                return FORWARDS_SCORE;
            }
            return 0.0f;
		}

        public DocIdSetIterator iterator() {
		    iter = new Iterator(backwardsIterator, forwardIterator);
		    return iter;
        }

		@Override
		public int docID() {
			return iter.docID();
		}	


	}

	public class Iterator extends DocIdSetIterator {

	    private DocIdSetIterator backwardsIterator;
        private DocIdSetIterator forwardIterator;

	    Iterator(DocIdSetIterator fwdIter, DocIdSetIterator bwdIter) {
	        backwardsIterator = bwdIter;
	        forwardIterator = fwdIter;
        }

        @Override
        public int docID() {
            int backwordsDocId = backwardsIterator.docID();
            int forwardsDocId = forwardIterator.docID();
            if (backwordsDocId <= forwardsDocId && backwordsDocId != NO_MORE_DOCS) {
                return backwordsDocId;
            } else if (forwardsDocId != NO_MORE_DOCS) {
                return forwardsDocId;
            }
            return NO_MORE_DOCS;
        }

        @Override
        public int nextDoc() throws IOException {
            int currDocId = docID();
            // increment one or both
            if (currDocId == backwardsIterator.docID()) {
                backwardsIterator.nextDoc();
            }
            if (currDocId == forwardIterator.docID()) {
                forwardIterator.nextDoc();
            }
            return docID();
        }


        @Override
        public int advance(int target) throws IOException {
            backwardsIterator.advance(target);
            forwardIterator.advance(target);
            return docID();                }

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
