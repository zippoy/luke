package org.apache.lucene.luke.models.overview;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Map;

public interface TermCounts {

  enum Order {
    NAME_ASC, NAME_DESC,
    COUNT_ASC, COUNT_DESC
  }

  void reset(IndexReader reader);

  Long numTerms() throws IOException;

  Map<String, Long> sortedTermCounts(TermCounts.Order order) throws IOException;

}
