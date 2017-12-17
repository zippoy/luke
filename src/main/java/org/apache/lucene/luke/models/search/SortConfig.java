package org.apache.lucene.luke.models.search;

import java.util.ArrayList;
import java.util.List;

public class SortConfig {

  private List<SortField> sortFieldList;

  public SortConfig() {
    this.sortFieldList = new ArrayList<>();
  }

  public void addSort(String name, Order order) {
    sortFieldList.add(new SortField(name, order));
  }

  public List<SortField> getSortFields() {
    return sortFieldList;
  }

  static class SortField {
    private String name;
    private Order order;

    SortField(String name, Order order) {
      this.name = name;
      this.order = order;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Order getOrder() {
      return order;
    }

    public void setOrder(Order order) {
      this.order = order;
    }
  }

  public enum Order {
    ASC, DESC
  }
}
