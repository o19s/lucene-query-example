package com.o19s;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackwardsTermCustomScoreQueryTest extends LuceneTestCase {
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
	public void customTest() {
		Term termToFind = new Term("tag", "woc");
		Query subQ = new TermQuery(termToFind);
		BackwardsTermCustomQuery q = new BackwardsTermCustomQuery(subQ);
		TopDocs results = searcherUnderTest.search(subQ, 10);
		
		
		
	}
}
