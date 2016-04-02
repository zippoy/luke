package org.apache.lucene.luke.ui;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;

import java.util.Comparator;

import static org.apache.lucene.luke.ui.TermVectorDialog.TVROW_KEY_FREQ;
import static org.apache.lucene.luke.ui.TermVectorDialog.TVROW_KEY_TERM;


public class TermVectorTableComparator implements Comparator<Map<String, String>> {
  private TableView tableView;

  public TermVectorTableComparator(TableView tableView) {
    if (tableView == null) {
      throw new IllegalArgumentException();
    }
    this.tableView = tableView;
  }

  @Override
  public int compare(Map<String, String> row1, Map<String, String> row2) {
    Dictionary.Pair<String, SortDirection> sort = tableView.getSort().get(0);

    int result;
    if (sort.key.equals(TVROW_KEY_TERM)) {
      // sort by name
      result = row1.get(TVROW_KEY_TERM).compareTo(row2.get(TVROW_KEY_TERM));
    } else if (sort.key.equals(TVROW_KEY_FREQ)) {
      // sort by termCount
      Integer f1 = Integer.parseInt(row1.get(TVROW_KEY_FREQ));
      Integer f2 = Integer.parseInt(row2.get(TVROW_KEY_FREQ));
      result = f1.compareTo(f2);
    } else {
      // other (ignored)
      result = 0;
    }
    SortDirection sortDirection = sort.value;
    result *= (sortDirection == SortDirection.DESCENDING ? 1 : -1);

    return result * -1;
  }
}
