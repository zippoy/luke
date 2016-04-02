package org.apache.lucene.luke.ui;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.luke.ui.TermVectorTableComparator;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.*;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import java.io.IOException;
import java.net.URL;

public class TermVectorDialog extends Dialog implements Bindable {

  @BXML
  private Label field;
  @BXML
  private TableView tvTable;
  @BXML
  private PushButton tvCopyButton;

  private Resources resources;

  private List<Map<String, String>> tableData;

  public static String TVROW_KEY_TERM = "term";
  public static String TVROW_KEY_FREQ = "freq";
  public static String TVROW_KEY_POSITION = "pos";
  public static String TVROW_KEY_OFFSETS = "offsets";

  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  public void initTermVector(String fieldName, Terms tv) throws IOException {
    field.setText(fieldName);
    tableData = new ArrayList<Map<String, String>>();
    TermsEnum te = tv.iterator();
    BytesRef term = null;

    // populate table data with term vector info
    while((term = te.next()) != null) {
      Map<String, String> row = new HashMap<String, String>();
      tableData.add(row);
      row.put(TVROW_KEY_TERM, term.utf8ToString());
      // try to get DocsAndPositionsEnum
      DocsEnum de = te.docsAndPositions(null, null);
      if (de == null) {
        // if positions are not indexed, get DocsEnum
        de = te.docs(null, null);
      }
      // must have one doc
      if (de.nextDoc() == DocIdSetIterator.NO_MORE_DOCS) {
        continue;
      }
      row.put(TVROW_KEY_FREQ, String.valueOf(de.freq()));
      if (de instanceof DocsAndPositionsEnum) {
        // positions are available
        DocsAndPositionsEnum dpe = (DocsAndPositionsEnum) de;
        StringBuilder bufPos = new StringBuilder();
        StringBuilder bufOff = new StringBuilder();
        // enumerate all positions info
        for (int i = 0; i < de.freq(); i++) {
          int pos = dpe.nextPosition();
          bufPos.append(String.valueOf(pos));
          if (i < de.freq() - 1) {
            bufPos.append((","));
          }
          // offsets are indexed?
          int sOffset = dpe.startOffset();
          int eOffset = dpe.endOffset();
          if (sOffset >= 0 && eOffset >= 0) {
            String offsets = String.valueOf(sOffset) + "-" + String.valueOf(eOffset);
            bufOff.append(offsets);
            if (i < de.freq() - 1) {
              bufOff.append(",");
            }
          }
        }
        row.put(TVROW_KEY_POSITION, bufPos.toString());
        row.put(TVROW_KEY_OFFSETS, (bufOff.length() == 0) ? "----" : bufOff.toString());
      } else {
        // positions are not available
        row.put(TVROW_KEY_POSITION, "----");
        row.put(TVROW_KEY_OFFSETS, "----");
      }
    }
    // register sort listener
    tvTable.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
      @Override
      public void sortChanged(TableView tableView) {
        List<Map<String, String>> tableData = (List<Map<String, String>>) tableView.getTableData();
        tableData.setComparator(new TermVectorTableComparator(tableView));
      }
    });
    // default sort : by ascending order of term
    Sequence<Dictionary.Pair<String, SortDirection>> sort = new ArrayList<Dictionary.Pair<String, SortDirection>>();
    sort.add(new Dictionary.Pair<String, SortDirection>(TVROW_KEY_TERM, SortDirection.ASCENDING));
    sort.add(new Dictionary.Pair<String, SortDirection>(TVROW_KEY_FREQ, SortDirection.DESCENDING));
    tvTable.setSort(sort);

    tvTable.setTableData(tableData);
    addPushButtonListener();
  }

  private void addPushButtonListener() {

    tvCopyButton.getButtonPressListeners().add(new ButtonPressListener() {
      @Override
      public void buttonPressed(Button button) {
        // fired when 'Copy to Clipboard' button pressed
        Sequence<Map<String, String>> selectedRows = (Sequence<Map<String, String>>) tvTable.getSelectedRows();
        if (selectedRows == null || selectedRows.getLength() == 0) {
          Alert.alert(MessageType.INFO, "No rows selected.", getWindow());
        } else {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < selectedRows.getLength(); i++) {
            Map<String, String> row = selectedRows.get(i);
            sb.append(row.get(TVROW_KEY_TERM) + "\t");
            sb.append(row.get(TVROW_KEY_FREQ) + "\t");
            sb.append(row.get(TVROW_KEY_POSITION) + "\t");
            sb.append(row.get(TVROW_KEY_OFFSETS));
            if (i < selectedRows.getLength() - 1) {
              sb.append("\n");
            }
          }
          LocalManifest content = new LocalManifest();
          content.putText(sb.toString());
          Clipboard.setContent(content);
        }
      }
    });
  }

}
