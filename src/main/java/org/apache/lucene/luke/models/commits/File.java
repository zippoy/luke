package org.apache.lucene.luke.models.commits;

public class File {
  private String fileName;
  private String displaySize;

  static File of(String indexPath, String name) {
    File file = new File();
    file.fileName = name;
    java.io.File fileObject = new java.io.File(indexPath, name);
    file.displaySize = CommitsImpl.toDisplaySize(fileObject.length());
    return file;
  }

  public String getFileName() {
    return fileName;
  }

  public String getDisplaySize() {
    return displaySize;
  }

  private File() {
  }
}
