package com.o19s;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackwardsTermQueryTest extends LuceneTestCase {

	Field newField(String name, String value, Store stored) {
		FieldType tagsFieldType = new FieldType();
		tagsFieldType.setStored(stored == Store.YES);
		tagsFieldType.setIndexed(true);
		return new Field(name, value, tagsFieldType);
	}

	IndexSearcher searcherUnderTest;
	RandomIndexWriter indexWriterUnderTest;
	IndexReader indexReaderUnderTest;
	Directory dirUnderTest;

	@Before
	public void setupIndex() throws IOException {
		dirUnderTest = newDirectory();

		indexWriterUnderTest = new RandomIndexWriter(random(), dirUnderTest);
		String[] docs = new String[] { "how now brown cow", "woc",
				"nworb", "won woh nworb" };
		for (int i = 0; i < docs.length; i++) {
			Document doc = new Document();
			doc.add(newStringField("id", "" + i, Field.Store.YES));
			doc.add(newField("field", docs[i], Field.Store.NO));
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
	public void testBackwardsMatchesGetScoreOf5() throws IOException {
		Query q = new BackwardsTermQuery("field", "brown");
		// we should get the "brown" docs backwards first (ie the nworb)
		TopDocs t = searcherUnderTest.search(q, 10);
		ScoreDoc[] docs = t.scoreDocs;
		assert(docs[0].score == 5.0f);
		assert(docs[1].score == 5.0f);
		assert(docs[2].score == 1.0f);
		assert(docs.length == 3);
		
	}

}
