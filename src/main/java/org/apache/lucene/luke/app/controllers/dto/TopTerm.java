package org.apache.lucene.luke.app.controllers.dto;

import org.apache.lucene.luke.models.overview.TermStats;

public class TopTerm {

  private int rank;
  private int freq;
  private String text;

  public static TopTerm of(int rank, TermStats stats) {
    TopTerm term = new TopTerm();
    term.rank = rank;
    term.freq = stats.getDocFreq();
    term.text = stats.getDecodedTermText();
    return term;
  }

  private TopTerm() {
  }

  public int getRank() {
    return rank;
  }

  public int getFreq() {
    return freq;
  }

  public String getText() {
    return text;
  }
}
