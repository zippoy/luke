package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TermVectorsAdapterImpl implements TermVectorsAdapter {

  private static Logger logger = LoggerFactory.getLogger(TermVectorsAdapterImpl.class);

  private IndexReader reader;

  TermVectorsAdapterImpl() {
  }

  public void reset(IndexReader reader) {
    this.reader = reader;
  }

  @Override
  public List<TermVectorEntry> getTermVector(int docid, String field) throws IOException {
    Terms termVector = reader.getTermVector(docid, field);
    if (termVector == null) {
      // no term vector available
      logger.warn("No term vector indexed for doc: #{} and field: {}", docid, field);
      return Collections.emptyList();
    }

    List<TermVectorEntry> res = new ArrayList<>();
    TermsEnum te = termVector.iterator();
    PostingsEnum pe = null;
    while (te.next() != null) {
      res.add(TermVectorEntry.of(te, pe));
    }
    return res;
  }

}
