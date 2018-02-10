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

package org.apache.lucene.luke.models.tools;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.PrintStream;
import java.util.Collection;

public interface IndexTools {

  void reset(Directory dir, String indexPath, boolean useCompound, boolean keepAllCommits);

  void reset(IndexReader reader, String indexPath, boolean useCompound, boolean keepAllCommits) throws LukeException;

  void optimize(boolean expunge, int maxNumSegments, PrintStream ps) throws LukeException;

  CheckIndex.Status checkIndex(PrintStream ps) throws LukeException;

  void repairIndex(CheckIndex.Status st, PrintStream ps) throws LukeException;

  void addDocument(Document doc, Analyzer analyzer) throws LukeException;

  void deleteDocuments(Query query) throws LukeException;

  Collection<Class<? extends Field>> getPresetFields();
}
