/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
