package org.apache.lucene.luke.models.overview;

import org.apache.lucene.luke.util.BytesRefUtils;

public class TermStats {
  private String decodedTermText;
  private String field;
  private int docFreq;

  static TermStats of(org.apache.lucene.misc.TermStats stats) {
    TermStats termStats = new TermStats();
    termStats.decodedTermText = BytesRefUtils.decode(stats.termtext);
    termStats.field = stats.field;
    termStats.docFreq = stats.docFreq;
    return termStats;
  }

  public String getDecodedTermText() {
    return decodedTermText;
  }

  public String getField() {
    return field;
  }

  public int getDocFreq() {
    return docFreq;
  }

  private TermStats() {
  }
}
