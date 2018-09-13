package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;

public class FieldValuesPaneProvider implements Provider<JScrollPane> {

  private final JCheckBox loadAllCB = new JCheckBox();

  private final JTable fieldTable = new JTable();

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    panel.add(fieldsSettings());

    return new JScrollPane(panel);
  }

  private JPanel fieldsSettings() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(500, 300));
    panel.setMaximumSize(new Dimension(1000, 500));

    JPanel header = new JPanel(new GridLayout(1, 2));
    header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_values.label.description")));
    loadAllCB.setText(MessageUtils.getLocalizedMessage("search_values.checkbox.load_all"));
    header.add(loadAllCB);
    panel.add(header, BorderLayout.PAGE_START);

    TableUtil.setupTable(fieldTable, ListSelectionModel.SINGLE_SELECTION, new FieldTableModel(), null, 50);
    panel.add(new JScrollPane(fieldTable), BorderLayout.CENTER);

    return panel;
  }

}

class FieldTableModel extends AbstractTableModel {

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

  FieldTableModel() {
    this.data = new Object[0][colNames.length];
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