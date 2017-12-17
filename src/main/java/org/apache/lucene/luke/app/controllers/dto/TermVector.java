package org.apache.lucene.luke.app.controllers.dto;

import org.apache.lucene.luke.models.documents.TermVectorEntry;

import java.util.stream.Collectors;

public class TermVector {
  private String termText;
  private Long freq;
  private String positions;
  private String offsets;

  public static TermVector of(TermVectorEntry entry) {
    TermVector tv = new TermVector();
    tv.termText = entry.getTermText();
    tv.freq = entry.getFreq();
    tv.positions = String.join(",",
        entry.getPositions().stream()
            .map(pos -> Integer.toString(pos.getPosition()))
            .collect(Collectors.toList()));
    tv.offsets = String.join(",",
        entry.getPositions().stream()
            .filter(pos -> pos.getStartOffset().isPresent() && pos.getEndOffset().isPresent())
            .map(pos -> String.format("%d-%d", pos.getStartOffset().orElse(-1), pos.getEndOffset().orElse(-1)))
            .collect(Collectors.toList())
    );
    return tv;
  }

  private TermVector() {
  }

  public String getTermText() {
    return termText;
  }

  public Long getFreq() {
    return freq;
  }

  public String getPositions() {
    return positions;
  }

  public String getOffsets() {
    return offsets;
  }
}
