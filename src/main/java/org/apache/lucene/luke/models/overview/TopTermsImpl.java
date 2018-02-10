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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class TopTermsImpl implements TopTerms {

  private IndexReader reader;

  private Map<String, List<TermStats>> topTermsCache;

  TopTermsImpl() {
    this.topTermsCache = new WeakHashMap<>();
  }

  @Override
  public void reset(IndexReader reader) {
    this.reader = reader;
    this.topTermsCache.clear();
  }

  @Override
  public List<TermStats> getTopTerms(String field, int numTerms) throws Exception {
    if (!topTermsCache.containsKey(field) || topTermsCache.get(field).size() < numTerms) {
      org.apache.lucene.misc.TermStats[] stats =
          HighFreqTerms.getHighFreqTerms(reader, numTerms, field, new HighFreqTerms.DocFreqComparator());
      List<TermStats> topTerms = Arrays.stream(stats)
          .map(TermStats::of)
          .collect(Collectors.toList());
      topTermsCache.put(field, topTerms);
    }
    return topTermsCache.get(field);
  }
}
