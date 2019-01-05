package com.o19s;

import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.schema.SimilarityFactory;

public class CountSimilarityFactory extends SimilarityFactory {
    public Similarity getSimilarity() {
        return new CountSimilarity();
    }
}
