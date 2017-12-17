package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Optional;

public interface DocValuesAdapter {

  Optional<DocValues> getDocValues(int docid, String field) throws IOException;

  void reset(IndexReader reader);
}
