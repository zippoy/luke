package org.apache.lucene.luke.app.desktop.components;

public interface TableColumnInfo {

  String getColName();

  int getIndex();

  Class<?> getType();
}
