package org.apache.lucene.luke.app.controllers.dto;

import org.apache.lucene.luke.util.BytesRefUtils;

public class TermPosting {

  private int position = -1;
  private String offset = "";
  private String payload = "";

  public static TermPosting of(org.apache.lucene.luke.models.documents.TermPosting p) {
    TermPosting posting = new TermPosting();
    posting.position = p.getPosition();
    if (p.getStartOffset() >= 0 && p.getEndOffset() >= 0) {
      posting.offset = String.format("%d-%d", p.getStartOffset(), p.getEndOffset());
    }
    if (p.getPayload() != null) {
      posting.payload = BytesRefUtils.decode(p.getPayload());
    }
    return posting;
  }

  private TermPosting() {
  }

  public int getPosition() {
    return position;
  }

  public String getOffset() {
    return offset;
  }

  public String getPayload() {
    return payload;
  }
}
