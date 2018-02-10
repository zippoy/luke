/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
