package org.apache.lucene.luke.models.search;

import org.apache.lucene.queries.mlt.MoreLikeThis;

import java.util.ArrayList;
import java.util.List;

public class MLTConfig {

  private List<String> fields;

  private int maxDocFreq = MoreLikeThis.DEFAULT_MAX_DOC_FREQ;

  private int minDocFreq = MoreLikeThis.DEFAULT_MIN_DOC_FREQ;

  private int minTermFreq = MoreLikeThis.DEFAULT_MIN_TERM_FREQ;

  public MLTConfig() {
    this.fields = new ArrayList<>();
  }

  public void clearFields() {
    fields.clear();
  }

  public void addField(String field) {
    fields.add(field);
  }

  public String[] getFieldNames() {
    return fields.toArray(new String[fields.size()]);
  }

  public int getMaxDocFreq() {
    return maxDocFreq;
  }

  public void setMaxDocFreq(int maxDocFreq) {
    this.maxDocFreq = maxDocFreq;
  }

  public int getMinDocFreq() {
    return minDocFreq;
  }

  public void setMinDocFreq(int minDocFreq) {
    this.minDocFreq = minDocFreq;
  }

  public int getMinTermFreq() {
    return minTermFreq;
  }

  public void setMinTermFreq(int minTermFreq) {
    this.minTermFreq = minTermFreq;
  }
}
