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
