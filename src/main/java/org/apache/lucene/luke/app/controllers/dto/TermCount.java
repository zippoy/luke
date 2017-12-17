package org.apache.lucene.luke.app.controllers.dto;

public class TermCount {
  private String field;
  private long count;
  private String ratio;

  public static TermCount of(String field, long count, double numTerms) {
    TermCount tc = new TermCount();
    tc.field = field;
    tc.count = count;
    tc.ratio = String.format("%.2f %%", count / numTerms * 100);
    return tc;
  }

  private TermCount() {
  }

  public String getField() {
    return field;
  }

  public long getCount() {
    return count;
  }

  public String getRatio() {
    return ratio;
  }
}
