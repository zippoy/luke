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
