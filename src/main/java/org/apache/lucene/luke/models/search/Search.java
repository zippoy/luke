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

package org.apache.lucene.luke.models.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Search {

  void reset(IndexReader reader) throws LukeException;

  Collection<String> getFieldNames();

  Collection<String> getSortableFieldNames();

  Collection<String> getSearchableFieldNames();

  Collection<String> getRangeSearchableFieldNames();

  Query getCurrentQuery();

  Query parseQuery(String expression, String defField, Analyzer analyzer, QueryParserConfig config, boolean rewrite) throws LukeException;

  Query mltQuery(int docNum, MLTConfig mltConfig, Analyzer analyzer) throws LukeException;

  Optional<SearchResults> search(Query query, SimilarityConfig simConfig, Set<String> fieldsToLoad, int pageSize) throws LukeException;

  Optional<SearchResults> search(Query query, SimilarityConfig simConfig, Sort sort, Set<String> fieldsToLoad, int pageSize) throws LukeException;

  Optional<SearchResults> nextPage() throws LukeException;

  Optional<SearchResults> prevPage() throws LukeException;

  Explanation explain(Query query, int doc) throws LukeException;

  List<SortField> guessSortTypes(String name) throws LukeException;

  Optional<SortField> getSortType(String name, String type, boolean reverse) throws LukeException;
}
