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

package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.models.LukeException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Documents {

  void reset(IndexReader reader) throws LukeException;

  int getMaxDoc();

  Collection<String> getFieldNames();

  boolean isLive(int docid);

  Optional<List<DocumentField>> getDocumentFields(int docid) throws LukeException;

  String getCurrentField();

  Optional<Term> firstTerm(String field) throws LukeException;

  Optional<Term> nextTerm() throws LukeException;

  Optional<Term> seekTerm(String termText) throws LukeException;

  Optional<Integer> firstTermDoc() throws LukeException;

  Optional<Integer> nextTermDoc() throws LukeException;

  List<TermPosting> getTermPositions() throws LukeException;

  Optional<Integer> getDocFreq() throws LukeException;

  List<TermVectorEntry> getTermVectors(int docid, String field) throws LukeException;

  Optional<DocValues> getDocValues(int docid, String field) throws LukeException;
}
