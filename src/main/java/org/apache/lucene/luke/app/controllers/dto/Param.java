package org.apache.lucene.luke.app.controllers.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Param {
  private BooleanProperty deleted = new SimpleBooleanProperty(false);
  private String name = "";
  private String value = "";

  public static Param newInstance() {
    return new Param();
  }

  public static Param of(String name, String value) {
    Param param = new Param();
    param.name = name;
    param.value = value;
    return param;
  }

  private Param() {
  }

  public Boolean isDeleted() {
    return deleted.get();
  }

  public void setDeleted(boolean val) {
    deleted.set(val);
  }

  public BooleanProperty getDeletedProperty() {
    return deleted;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isValid() {
    return name != null && name.length() > 0;
  }
}
