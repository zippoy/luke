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
import org.apache.lucene.luke.util.IndexUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TermCountsImpl implements TermCounts {

  private IndexReader reader;

  private Map<String, Long> termCountMap;

  TermCountsImpl() {
  }

  public void reset(IndexReader reader) {
    this.reader = reader;
    this.termCountMap = null;
  }

  // lazy population
  private Map<String, Long> getTermCountMap() throws IOException {
    if (termCountMap == null) {
      termCountMap = IndexUtils.countTerms(reader, IndexUtils.getFieldNames(reader));
    }
    return termCountMap;
  }

  @Override
  public Long numTerms() throws IOException {
    return getTermCountMap().values().stream().mapToLong(Long::longValue).sum();
  }

  @Override
  public Map<String, Long> sortedTermCounts(@Nonnull Order order) throws IOException {
    Comparator<Map.Entry<String, Long>> comparator;
    switch (order) {
      case NAME_ASC:
        comparator = Map.Entry.comparingByKey();
        break;
      case NAME_DESC:
        comparator = Map.Entry.<String, Long>comparingByKey().reversed();
        break;
      case COUNT_ASC:
        comparator = Map.Entry.comparingByValue();
        break;
      case COUNT_DESC:
        comparator = Map.Entry.<String, Long>comparingByValue().reversed();
        break;
      default:
        comparator = Map.Entry.comparingByKey();
    }
    return sortedTermCounts(comparator);
  }

  private Map<String, Long> sortedTermCounts(Comparator<Map.Entry<String, Long>> comparator) throws IOException {
    return getTermCountMap().entrySet().stream()
        .sorted(comparator)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
  }
}
