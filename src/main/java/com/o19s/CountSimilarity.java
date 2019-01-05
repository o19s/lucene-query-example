package com.o19s;

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class CountSimilarity extends ClassicSimilarity {
    public float tf(float freq) {
        return freq;
    }

    public float idf(long docFreq, long docCount) {
        return 1.0f;
    }

    public float lengthNorm(int numTerms) {
        return 1.0f;
    }

    // term frequency for phrase queries with slop
    public float sloppyFreq(int distance) {
        return 1.0f;
    }

    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1.0f;
    }
}
