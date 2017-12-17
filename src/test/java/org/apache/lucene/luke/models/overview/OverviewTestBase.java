package org.apache.lucene.luke.models.overview;

import org.apache.lucene.analysis.MockAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Path;

public abstract class OverviewTestBase extends LuceneTestCase {
  protected IndexReader reader;
  protected Directory dir;
  protected Path indexDir;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    createIndex();
    dir = newFSDirectory(indexDir);
    reader = DirectoryReader.open(dir);

  }

  protected void createIndex() throws IOException {
    indexDir = createTempDir();

    Directory dir = newFSDirectory(indexDir);
    RandomIndexWriter writer = new RandomIndexWriter(random(), dir, new MockAnalyzer(random()));

    Document doc1 = new Document();
    doc1.add(newStringField("f1", "1", Field.Store.NO));
    doc1.add(newTextField("f2", "a b c d e", Field.Store.NO));
    writer.addDocument(doc1);
    Document doc2 = new Document();
    doc2.add(newStringField("f1", "2", Field.Store.NO));
    doc2.add(new TextField("f2", "a c", Field.Store.NO));
    writer.addDocument(doc2);
    Document doc3 = new Document();
    doc3.add(newStringField("f1", "3", Field.Store.NO));
    doc3.add(newTextField("f2", "a f", Field.Store.NO));
    writer.addDocument(doc3);
    writer.commit();

    writer.close();
    dir.close();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    reader.close();
    dir.close();
  }

}
