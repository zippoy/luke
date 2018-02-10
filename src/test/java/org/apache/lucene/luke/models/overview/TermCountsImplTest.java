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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class TermCountsImplTest extends OverviewTestBase {

  @Test
  public void testNumTerms() throws Exception {
    TermCountsImpl termCounts = new TermCountsImpl();
    termCounts.reset(reader);
    assertEquals(9, (long) termCounts.numTerms());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSortedTermCounts() throws Exception {
    TermCountsImpl termCounts = new TermCountsImpl();
    termCounts.reset(reader);

    LinkedHashMap<String, Long> countsMap = (LinkedHashMap) termCounts.sortedTermCounts(TermCounts.Order.COUNT_ASC);
    assertEquals(Arrays.asList("f1", "f2"), new ArrayList<>(countsMap.keySet()));

    countsMap = (LinkedHashMap) termCounts.sortedTermCounts(TermCounts.Order.COUNT_DESC);
    assertEquals(Arrays.asList("f2", "f1"), new ArrayList<>(countsMap.keySet()));

    countsMap = (LinkedHashMap) termCounts.sortedTermCounts(TermCounts.Order.NAME_ASC);
    assertEquals(Arrays.asList("f1", "f2"), new ArrayList<>(countsMap.keySet()));

    countsMap = (LinkedHashMap) termCounts.sortedTermCounts(TermCounts.Order.NAME_DESC);
    assertEquals(Arrays.asList("f2", "f1"), new ArrayList<>(countsMap.keySet()));

    assertEquals(3, (long) countsMap.get("f1"));
    assertEquals(6, (long) countsMap.get("f2"));
  }

}