package com.o19s;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Lucene101Test extends LuceneTestCase {
	
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
	public void testBasics() throws IOException {
		Directory d = new RAMDirectory();
		
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_46);
		
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
		IndexWriter iw = new IndexWriter(d, iwc);
		
		Document doc = new Document();
		FieldType tagsFieldType = new FieldType();
		tagsFieldType.setStored(true);
		tagsFieldType.setIndexed(true);
		FieldType idFieldType = new FieldType();
		idFieldType.setStored(true);
		idFieldType.setIndexed(true);
		idFieldType.setTokenized(false);

		Field idField = new Field("id", "1", idFieldType);
		Field field = new Field("tag", "star-trek captain-picard tv-show space", tagsFieldType);
		doc.add(field);
		doc.add(idField);
		iw.addDocument(doc);

		field = new Field("tag", "star-wars darth-vader space", tagsFieldType);
		idField = new Field("id", "2", idFieldType);
		doc.add(field);		
		doc.add(idField);		
		iw.addDocument(doc);

		iw.commit();
		
		IndexReader ir = DirectoryReader.open(d);
		IndexSearcher is = new IndexSearcher(ir);
		
		Term termToFind = new Term("tag", "space");
		TermQuery tq = new TermQuery(termToFind);
		
		TopDocs td = is.search(tq, 10);
		assert(td.scoreDocs.length == 2);

		
	}
}
