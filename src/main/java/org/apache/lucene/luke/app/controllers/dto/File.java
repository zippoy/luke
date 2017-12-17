package org.apache.lucene.luke.app.controllers.dto;

public class File {
  private String name;
  private String size;

  public static File of(org.apache.lucene.luke.models.commits.File f) {
    File file = new File();
    file.name = f.getFileName();
    file.size = f.getDisplaySize();
    return file;
  }

  private File() {
  }

  public String getName() {
    return name;
  }

  public String getSize() {
    return size;
  }
}
