package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.List;

public interface TermVectorsAdapter {

  List<TermVectorEntry> getTermVector(int docid, String field) throws IOException;

  void reset(IndexReader reader);
}
