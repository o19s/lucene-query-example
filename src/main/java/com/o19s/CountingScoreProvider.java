package com.o19s;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;

public class CountingScoreProvider extends CustomScoreProvider {

	public CountingScoreProvider(LeafReaderContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	

}
