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
