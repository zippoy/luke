package org.apache.lucene.luke.app.controllers.dto;

import org.apache.lucene.luke.models.analysis.Analysis;

import java.util.List;
import java.util.stream.Collectors;

public class Token {
  private String term;
  private String attributes;
  private Analysis.Token originalToken;

  public static Token of(Analysis.Token token) {
    Token t = new Token();
    t.term = token.term;
    t.originalToken = token;
    List<String> attValues = token.attributes.stream()
        .flatMap(att -> att.attValues.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue()))
        .collect(Collectors.toList());
    t.attributes = String.join(",", attValues);
    return t;
  }

  private Token() {
  }

  public String getTerm() {
    return term;
  }

  public String getAttributes() {
    return attributes;
  }

  public Analysis.Token getOriginalToken() {
    return originalToken;
  }
}
