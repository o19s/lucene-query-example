package com.o19s;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;

public class CountingScoreProvider extends CustomScoreProvider {

	public CountingScoreProvider(AtomicReaderContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	

}
