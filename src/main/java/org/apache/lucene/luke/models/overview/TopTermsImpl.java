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
