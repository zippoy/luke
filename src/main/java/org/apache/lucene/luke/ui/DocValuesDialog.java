package org.apache.lucene.luke.ui;

import org.apache.lucene.index.*;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.*;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import java.io.IOException;
import java.net.URL;

public class DocValuesDialog extends Dialog implements Bindable {

  private FieldInfo finfo;
  private LeafReader lr;

  private Resources resources;

  @BXML
  private Label name;
  @BXML
  private Label type;
  @BXML
  private Label count;
  @BXML
  private TableView data;
  @BXML
  private PushButton copyBtn;

  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  private static final String ROW_KEY_VALUE = "value";

  public void initDocValues(FieldInfo finfo, LeafReader lr, int docid) throws IOException {
    this.finfo = finfo;
    this.lr = lr;

    name.setText(finfo.name);
    type.setText(finfo.getDocValuesType().name());

    List<Map<String, Object>> tableData = new ArrayList<>();
    data.setTableData(tableData);
    Map<String, Object> row;
    switch(finfo.getDocValuesType()) {
      case BINARY:
        BinaryDocValues bdv = lr.getBinaryDocValues(finfo.name);
        count.setText("-");
        row = new HashMap<>();
        tableData.add(row);
        row.put(ROW_KEY_VALUE, bdv.get(docid).utf8ToString());
        break;
      case NUMERIC:
        NumericDocValues ndv = lr.getNumericDocValues(finfo.name);
        count.setText("-");
        row = new HashMap<>();
        tableData.add(row);
        row.put(ROW_KEY_VALUE, String.valueOf(ndv.get(docid)));
        break;
      case SORTED:
        SortedDocValues sdv = lr.getSortedDocValues(finfo.name);
        count.setText("-");
        row = new HashMap<>();
        tableData.add(row);
        row.put(ROW_KEY_VALUE, sdv.get(docid).utf8ToString());
        break;
      case SORTED_NUMERIC:
        SortedNumericDocValues sndv = lr.getSortedNumericDocValues(finfo.name);
        sndv.setDocument(docid);
        count.setText(String.valueOf(sndv.count()));
        for (int i = 0; i < sndv.count(); i++) {
          row = new HashMap<>();
          tableData.add(row);
          row.put(ROW_KEY_VALUE, String.valueOf(sndv.valueAt(i)));
        }
        count.setText(String.valueOf(tableData.getLength()));
        break;
      case SORTED_SET:
        SortedSetDocValues ssdv = lr.getSortedSetDocValues(finfo.name);
        ssdv.setDocument(docid);
        long ord;
        while ((ord = ssdv.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
          row = new HashMap<>();
          tableData.add(row);
          row.put(ROW_KEY_VALUE, ssdv.lookupOrd(ord).utf8ToString());
        }
        count.setText(String.valueOf(tableData.getLength()));
        break;
      default:
        count.setText("???");
    }
    addTableViewSelectionListener();
    addPushButtonListener();
  }

  private void addTableViewSelectionListener() {
    data.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter(){
      @Override
      public void  	selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
        if (tableView.getSelectedRows().getLength() > 0) {
          copyBtn.setEnabled(true);
        } else {
          copyBtn.setEnabled(false);
        }
      }
    });
  }

  private void addPushButtonListener() {

    copyBtn.getButtonPressListeners().add(new ButtonPressListener() {
      @Override
      public void buttonPressed(Button button) {
        // fired when 'Copy to Clipboard' button pressed
        Sequence<Map<String, String>> selectedRows = (Sequence<Map<String, String>>) data.getSelectedRows();
        if (selectedRows == null || selectedRows.getLength() == 0) {
          Alert.alert(MessageType.INFO, "No rows selected.", getWindow());
        } else {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < selectedRows.getLength(); i++) {
            Map<String, String> row = selectedRows.get(i);
            sb.append(row.get(ROW_KEY_VALUE));
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
