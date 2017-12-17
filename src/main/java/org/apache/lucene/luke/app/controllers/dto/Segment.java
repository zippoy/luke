package org.apache.lucene.luke.app.controllers.dto;

public class Segment {
  private String name;
  private int maxDoc;
  private long delGen;
  private int delCount;
  private String luceneVer;
  private String codecName;
  private String size;
  private boolean useCompoundFile;

  public static Segment of(org.apache.lucene.luke.models.commits.Segment seg) {
    Segment segment = new Segment();
    segment.name = seg.getName();
    segment.maxDoc = seg.getMaxDoc();
    segment.delGen = seg.getDelGen();
    segment.delCount = seg.getDelCount();
    segment.luceneVer = seg.getLuceneVer();
    segment.size = seg.getDisplaySize();
    segment.useCompoundFile = seg.isUseCompoundFile();
    return segment;
  }

  private Segment() {
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

  public String getSize() {
    return size;
  }

  public boolean isUseCompoundFile() {
    return useCompoundFile;
  }
}
