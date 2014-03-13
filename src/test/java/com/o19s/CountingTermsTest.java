package com.o19s;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CountingTermsTest extends LuceneTestCase {
	
	Field newFieldAllOn(String name, String value) {
		FieldType tagsFieldType = new FieldType();
		tagsFieldType.setStored(true);
		tagsFieldType.setIndexed(true);
		tagsFieldType.setOmitNorms(true);
		tagsFieldType.setStoreTermVectors(true);
		tagsFieldType.setStoreTermVectorPositions(true);
		tagsFieldType.setStoreTermVectorPayloads(true);
		return new Field(name, value, tagsFieldType);
	}
	
	TermQuery newTermQuery(String field, String search) {
		Term t = new Term(field, search);
		return new TermQuery(t);
	}
	
	IndexSearcher searcherUnderTest;
	RandomIndexWriter indexWriterUnderTest;
	IndexReader indexReaderUnderTest;
	Directory dirUnderTest;

	@Before
	public void setupIndex() throws IOException {
		dirUnderTest = newDirectory();

		indexWriterUnderTest = new RandomIndexWriter(random(), dirUnderTest);
		String[] docs = new String[] { "star-trek star-wars space tv-shows", "star-trek",
				"tv-shows", "star-trek tv-shows" };
		for (int i = 0; i < docs.length; i++) {
			Document doc = new Document();
			String idStr = Integer.toString(i);
			doc.add(newFieldAllOn("id", idStr));
			doc.add(newFieldAllOn("tag", docs[i]));
			indexWriterUnderTest.addDocument(doc);
		}
		indexWriterUnderTest.commit();

		indexReaderUnderTest = indexWriterUnderTest.getReader();
		searcherUnderTest = newSearcher(indexReaderUnderTest);
	}

	@After
	public void closeStuff() throws IOException {
		indexReaderUnderTest.close();
		indexWriterUnderTest.close();
		dirUnderTest.close();

	}
	
	@Test
	public void testCountingScoring() throws IOException {
		TermQuery tq = newTermQuery("tag", "star-trek");
		CountingQuery ct = new CountingQuery(tq);
	
		TopDocs td = searcherUnderTest.search(ct, 10);
		ScoreDoc[] sdocs = td.scoreDocs;
		assert(sdocs[0].score == 4.0);
		assert(sdocs[1].score == 2.0);
		assert(sdocs[2].score == 1.0);
		
	}

	
}
