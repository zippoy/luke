package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FieldValuesPaneProvider implements Provider<JScrollPane> {

  private final JCheckBox loadAllCB = new JCheckBox();

  private final JTable fieldsTable = new JTable();

  private ListenerFunctions listners = new ListenerFunctions();

  class FieldValuesTabOperatorImpl implements FieldValuesTabOperator {
    @Override
    public void setFields(Collection<String> fields) {
      fieldsTable.setModel(new FieldsTableModel(fields));
      fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.LOAD.getIndex()).setMinWidth(50);
      fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.LOAD.getIndex()).setMaxWidth(50);
      fieldsTable.getModel().addTableModelListener(listners::tableDataChenged);
    }

    @Override
    public Set<String> getFieldsToLoad() {
      Set<String> fieldsToLoad = new HashSet<>();
      for (int row = 0; row < fieldsTable.getRowCount(); row++) {
        boolean loaded = (boolean)fieldsTable.getValueAt(row, FieldsTableModel.Column.LOAD.getIndex());
        if (loaded) {
          fieldsToLoad.add((String)fieldsTable.getValueAt(row, FieldsTableModel.Column.FIELD.getIndex()));
        }
      }
      return fieldsToLoad;
    }
  }

  class ListenerFunctions {

    void loadAllFields(ActionEvent e) {
      for (int i = 0; i < fieldsTable.getModel().getRowCount(); i++) {
        if (loadAllCB.isSelected()) {
          fieldsTable.setValueAt(true, i, FieldsTableModel.Column.LOAD.getIndex());
        } else {
          fieldsTable.setValueAt(false, i, FieldsTableModel.Column.LOAD.getIndex());
        }
      }
    }

    void tableDataChenged(TableModelEvent e) {
      int row = e.getFirstRow();
      int col = e.getColumn();
      if (col == FieldsTableModel.Column.LOAD.getIndex()) {
        boolean isLoad = (boolean)fieldsTable.getModel().getValueAt(row, col);
        if (!isLoad) {
          loadAllCB.setSelected(false);
        }
      }
    }
  }

  @Inject
  public FieldValuesPaneProvider(ComponentOperatorRegistry operatorRegistry) {
    operatorRegistry.register(FieldValuesTabOperator.class, new FieldValuesTabOperatorImpl());
  }

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    panel.add(fieldsConfig());

    return new JScrollPane(panel);
  }

  private JPanel fieldsConfig() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel header = new JPanel(new GridLayout(1, 2));
    header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_values.label.description")));
    loadAllCB.setText(MessageUtils.getLocalizedMessage("search_values.checkbox.load_all"));
    loadAllCB.setSelected(true);
    loadAllCB.addActionListener(listners::loadAllFields);
    header.add(loadAllCB);
    panel.add(header, BorderLayout.PAGE_START);

    TableUtil.setupTable(fieldsTable, ListSelectionModel.SINGLE_SELECTION, new FieldsTableModel(), null);
    fieldsTable.setShowGrid(true);
    fieldsTable.setPreferredScrollableViewportSize(fieldsTable.getPreferredSize());
    panel.add(new JScrollPane(fieldsTable), BorderLayout.CENTER);

    return panel;
  }

  public interface FieldValuesTabOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setFields(Collection<String> fields);
    Set<String> getFieldsToLoad();
  }

}

class FieldsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    LOAD("Load", 0, Boolean.class),
    FIELD("Field", 1, String.class);

    private String colName;
    private int index;
    private Class<?> type;

    Column(String colName, int index, Class<?> type) {
      this.colName = colName;
      this.index = index;
      this.type = type;
    }

    @Override
    public String getColName() {
      return colName;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public Class<?> getType() {
      return type;
    }
  }

  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  FieldsTableModel() {
    this.data = new Object[0][colNames.length];
  }

  FieldsTableModel(Collection<String> fields) {
    this.data = new Object[fields.size()][colNames.length];
    int i = 0;
    for (String field : fields) {
      data[i][Column.LOAD.getIndex()] = true;
      data[i][Column.FIELD.getIndex()] = field;
      i++;
    }
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public int getColumnCount() {
    return colNames.length;
  }

  @Override
  public String getColumnName(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).colName;
    }
    return "";
  }

  @Override
  public Class<?> getColumnClass(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).type;
    }
    return Object.class;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data[rowIndex][columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == Column.LOAD.getIndex()) {
      return true;
    }
    return false;
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}