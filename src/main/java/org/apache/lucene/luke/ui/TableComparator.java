package org.apache.lucene.luke.ui;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;

import java.util.Comparator;

public class TableComparator implements Comparator<FieldsTableRow> {
  private TableView tableView;

  public TableComparator(TableView fieldsTable) {
    if (fieldsTable == null) {
      throw new IllegalArgumentException();
    }

    this.tableView = fieldsTable;
  }

  @Override
  public int compare(FieldsTableRow row1, FieldsTableRow row2) {
    Dictionary.Pair<String, SortDirection> sort = tableView.getSort().get(0);

    int result;
    if (sort.key.equals("name")) {
      // sort by name
      result = row1.getName().compareTo(row2.getName());
    } else if (sort.key.equals("termCount")) {
      // sort by termCount
      Integer c1 = Integer.parseInt(row1.getTermCount());
      Integer c2 = Integer.parseInt(row2.getTermCount());
      result = c1.compareTo(c2);
    } else {
      // other (ignored)
      result = 0;
    }
    //int result = o1.get("name").compareTo(o2.get("name"));
    //SortDirection sortDirection = tableView.getSort().get("name");
    SortDirection sortDirection = sort.value;
    result *= (sortDirection == SortDirection.DESCENDING ? 1 : -1);

    return result * -1;
  }
}

/*
public class TableComparator implements Comparator<Map<String,String>> {
  private TableView tableView;
  
  public TableComparator(TableView fieldsTable) {
    if (fieldsTable == null) {
      throw new IllegalArgumentException();
    }
    
    this.tableView = fieldsTable;
  }
  
  @Override
  public int compare(Map<String,String> o1, Map<String,String> o2) {
    Dictionary.Pair<String, SortDirection> sort = tableView.getSort().get(0);

    int result;
    if (sort.key.equals("name")) {
      // sort by name
      result = o1.get(sort.key).compareTo(o2.get(sort.key));
    } else if (sort.key.equals("termCount")) {
      // sort by termCount
      Integer c1 = Integer.parseInt(o1.get(sort.key));
      Integer c2 = Integer.parseInt(o2.get(sort.key));
      result = c1.compareTo(c2);
    } else {
      // other (ignored)
      result = 0;
    }
    //int result = o1.get("name").compareTo(o2.get("name"));
    //SortDirection sortDirection = tableView.getSort().get("name");
    SortDirection sortDirection = sort.value;
    result *= (sortDirection == SortDirection.DESCENDING ? 1 : -1);

    return result * -1;
  }
  
}
*/
