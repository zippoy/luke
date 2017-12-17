package org.apache.lucene.luke.models.overview;

import org.apache.lucene.index.IndexReader;

import java.util.List;

public interface TopTerms {

  void reset(IndexReader reader);

  List<TermStats> getTopTerms(String field, int numTerms) throws Exception;
}
