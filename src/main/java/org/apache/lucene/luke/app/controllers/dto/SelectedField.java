package org.apache.lucene.luke.app.controllers.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SelectedField {
  private BooleanProperty selected = new SimpleBooleanProperty(true);
  private String field;

  public static SelectedField of(String field) {
    SelectedField lf = new SelectedField();
    lf.selected = new SimpleBooleanProperty(true);
    lf.field = field;
    return lf;
  }

  private SelectedField() {
  }

  public Boolean isSelected() {
    return selected.get();
  }

  public void setSelected(Boolean val) {
    selected.set(val);
  }

  public BooleanProperty selectedProperty() {
    return selected;
  }

  public String getField() {
    return this.field;
  }

  public void setField(String field) {
    this.field = field;
  }
}
