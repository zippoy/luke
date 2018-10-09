/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.luke.models.commits;

import org.apache.lucene.analysis.MockAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.NoDeletionPolicy;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Path;

public abstract class CommitsTestBase extends LuceneTestCase {
  protected DirectoryReader reader;

  protected Directory dir;

  protected Path indexDir;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    indexDir = createIndex();
    dir = newFSDirectory(indexDir);
    reader = DirectoryReader.open(dir);
  }

  private Path createIndex() throws IOException {
    Path indexDir = createTempDir();

    Directory dir = newFSDirectory(indexDir);

    IndexWriterConfig config = new IndexWriterConfig(new MockAnalyzer(random()));
    config.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);
    RandomIndexWriter writer = new RandomIndexWriter(random(), dir, config);

    Document doc1 = new Document();
    doc1.add(newStringField("f1", "1", Field.Store.NO));
    writer.addDocument(doc1);

    writer.commit();

    Document doc2 = new Document();
    doc2.add(newStringField("f1", "2", Field.Store.NO));
    writer.addDocument(doc2);

    Document doc3 = new Document();
    doc3.add(newStringField("f1", "3", Field.Store.NO));
    writer.addDocument(doc3);

    writer.commit();

    writer.close();
    dir.close();

    return indexDir;
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    reader.close();
    dir.close();
  }

}
