package org.apache.lucene.luke.app.controllers.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class FilterFactory {
  private BooleanProperty deleted = new SimpleBooleanProperty(false);
  private int order;
  private String factory;

  public static FilterFactory of(int order, String factory) {
    FilterFactory ff = new FilterFactory();
    ff.order = order;
    ff.factory = factory;
    return ff;
  }

  private FilterFactory() {
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

  public int getOrder() {
    return order;
  }

  public String getFactory() {
    return factory;
  }

}
