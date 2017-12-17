package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class TermPosting {
  // position
  private int position = -1;

  // start and end offset (optional)
  private int startOffset = -1;
  private int endOffset = -1;

  // payload (optional)
  private BytesRef payload;

  static TermPosting of(int position, PostingsEnum penum) throws IOException {
    TermPosting posting = new TermPosting();
    // set position
    posting.position = position;
    // set offset (if available)
    int sOffset = penum.startOffset();
    int eOffset = penum.endOffset();
    if (sOffset >= 0 && eOffset >= 0) {
      posting.startOffset = sOffset;
      posting.endOffset = eOffset;
    }
    // set payload (if available)
    BytesRef payload = penum.getPayload();
    if (payload != null) {
      posting.payload = payload;
    }
    return posting;

  }

  public int getPosition() {
    return position;
  }

  public int getStartOffset() {
    return startOffset;
  }

  public int getEndOffset() {
    return endOffset;
  }

  public BytesRef getPayload() {
    return payload;
  }

  private TermPosting() {
  }
}
