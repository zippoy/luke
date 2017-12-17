package org.apache.lucene.luke.app.controllers.dto;

import org.apache.lucene.luke.models.search.SearchResults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchResult {
  private int docId;
  private float score;
  private String values;

  public static SearchResult of(SearchResults.Doc doc) {
    SearchResult res = new SearchResult();
    res.docId = doc.getDocId();
    res.score = doc.getScore();
    List<String> concatValues = doc.getFieldValues().entrySet().stream().map(e -> {
      String v = String.join(",", Arrays.asList(e.getValue()));
      return e.getKey() + "=" + v + ";";
    }).collect(Collectors.toList());
    res.values = String.join(" ", concatValues);
    return res;
  }

  private SearchResult() {
  }

  public int getDocId() {
    return docId;
  }

  public float getScore() {
    return score;
  }

  public String getValues() {
    return values;
  }
}
