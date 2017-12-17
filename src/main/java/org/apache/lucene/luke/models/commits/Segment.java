package org.apache.lucene.luke.models.commits;

import org.apache.lucene.index.SegmentCommitInfo;

import java.io.IOException;

public class Segment {
  private String name;
  private int maxDoc;
  private long delGen;
  private int delCount;
  private String luceneVer;
  private String codecName;
  private String displaySize;
  private boolean useCompoundFile;

  static Segment of(SegmentCommitInfo segInfo) {
    Segment segment = new Segment();
    segment.name = segInfo.info.name;
    segment.maxDoc = segInfo.info.maxDoc();
    segment.delGen = segInfo.getDelGen();
    segment.delCount = segInfo.getDelCount();
    segment.luceneVer = segInfo.info.getVersion().toString();
    segment.codecName = segInfo.info.getCodec().getName();
    try {
      segment.displaySize = CommitsImpl.toDisplaySize(segInfo.sizeInBytes());
    } catch (IOException e) {
    }
    segment.useCompoundFile = segInfo.info.getUseCompoundFile();
    return segment;
  }

  public String getName() {
    return name;
  }

  public int getMaxDoc() {
    return maxDoc;
  }

  public long getDelGen() {
    return delGen;
  }

  public int getDelCount() {
    return delCount;
  }

  public String getLuceneVer() {
    return luceneVer;
  }

  public String getCodecName() {
    return codecName;
  }

  public String getDisplaySize() {
    return displaySize;
  }

  public boolean isUseCompoundFile() {
    return useCompoundFile;
  }

  private Segment() {
  }
}
