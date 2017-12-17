package org.apache.lucene.luke.models.search;

public class SimilarityConfig {

  private boolean useClassicSimilarity = false;

  /* BM25Similarity parameters */

  private float k1 = 1.2f;

  private float b = 0.75f;

  /* Common parameters */

  private boolean discountOverlaps = true;

  public boolean isUseClassicSimilarity() {
    return useClassicSimilarity;
  }

  public void setUseClassicSimilarity(boolean useClassicSimilarity) {
    this.useClassicSimilarity = useClassicSimilarity;
  }

  public float getK1() {
    return k1;
  }

  public void setK1(float k1) {
    this.k1 = k1;
  }

  public float getB() {
    return b;
  }

  public void setB(float b) {
    this.b = b;
  }

  public boolean isDiscountOverlaps() {
    return discountOverlaps;
  }

  public void setDiscountOverlaps(boolean discountOverlaps) {
    this.discountOverlaps = discountOverlaps;
  }

  public String toString() {
    return "SimilarityConfig: [" +
        String.format(" use classic similarity=%s;", useClassicSimilarity) +
        String.format(" discount overlaps=%s;", discountOverlaps) +
        String.format(" k1=%f;", k1) +
        String.format(" b=%f;", b) +
        "]";
  }
}
