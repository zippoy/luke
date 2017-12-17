package org.apache.lucene.luke.models.overview;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.models.LukeException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Overview {

  void reset(IndexReader reader, String indexPath) throws LukeException;

  String getIndexPath();

  Integer getNumFields();

  Integer getNumDocuments();

  Long getNumTerms() throws LukeException;

  Boolean hasDeletions();

  Integer getNumDeletedDocs();

  Optional<Boolean> isOptimized();

  Optional<Long> getIndexVersion();

  String getIndexFormat() throws LukeException;

  String getDirImpl();

  Optional<String> getCommitDescription();

  Optional<String> getCommitUserData() throws LukeException;

  Map<String, Long> getSortedTermCounts(TermCounts.Order order) throws LukeException;

  List<TermStats> getTopTerms(String field, int numTerms) throws LukeException;
}
