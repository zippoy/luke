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

package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.luke.util.BytesRefUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class TermVectorEntry {
  private String termText;
  private long freq;
  private List<TermVectorPosition> positions;

  static TermVectorEntry of(@Nonnull TermsEnum te, PostingsEnum pe) throws IOException {
    TermVectorEntry entry = new TermVectorEntry();
    entry.termText = BytesRefUtils.decode(te.term());
    entry.freq = te.totalTermFreq();

    List<TermVectorEntry.TermVectorPosition> tvPositions = new ArrayList<>();
    pe = te.postings(pe, PostingsEnum.OFFSETS);
    pe.nextDoc();
    int freq = pe.freq();
    for (int i = 0; i < freq; i++) {
      int pos = pe.nextPosition();
      if (pos < 0) {
        // no position information available
        continue;
      }
      TermVectorPosition tvPos = TermVectorPosition.of(pos, pe);
      tvPositions.add(tvPos);
    }
    entry.positions = tvPositions;
    return entry;
  }

  public String getTermText() {
    return termText;
  }

  public long getFreq() {
    return freq;
  }

  public List<TermVectorPosition> getPositions() {
    return positions;
  }

  public static class TermVectorPosition {
    private int position;
    private int startOffset = -1;
    private int endOffset = -1;

    static TermVectorPosition of(int pos, @Nonnull PostingsEnum pe) throws IOException {
      TermVectorPosition tvPos = new TermVectorPosition();
      tvPos.position = pos;
      int sOffset = pe.startOffset();
      int eOffset = pe.endOffset();
      if (sOffset >= 0 && eOffset >= 0) {
        tvPos.startOffset = sOffset;
        tvPos.endOffset = eOffset;
      }
      return tvPos;
    }

    public int getPosition() {
      return position;
    }

    public OptionalInt getStartOffset() {
      return startOffset >= 0 ? OptionalInt.of(startOffset) : OptionalInt.empty();
    }

    public OptionalInt getEndOffset() {
      return endOffset >= 0 ? OptionalInt.of(endOffset) : OptionalInt.empty();
    }

    private TermVectorPosition() {
    }
  }
}
