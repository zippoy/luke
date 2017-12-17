package org.apache.lucene.luke.app.controllers.dto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import static org.apache.lucene.luke.app.controllers.dto.PVField.Type.INT;

public class PVField {
  private String field;
  private ObjectProperty<Type> typeProperty = new SimpleObjectProperty<>(INT);

  public static PVField of(String field) {
    PVField pvField = new PVField();
    pvField.field = field;
    return pvField;
  }

  private PVField() {
  }

  public String getField() {
    return field;
  }

  public ObjectProperty<Type> getTypeProperty() {
    return typeProperty;
  }

  public Type getType() {
    return typeProperty.get();
  }

  public void setType(Type value) {
    typeProperty.set(value);
  }

  public enum Type {
    INT, LONG, FLOAT, DOUBLE
  }
}
