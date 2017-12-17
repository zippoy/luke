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