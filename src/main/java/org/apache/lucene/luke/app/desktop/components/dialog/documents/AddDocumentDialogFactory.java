package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.listeners.dialog.documents.AddDocumentDialogListeners;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.TreeMap;

public class AddDocumentDialogFactory implements DialogOpener.DialogFactory {

  private final AddDocumentDialogListeners listeners;

  private final JLabel analyzerNameLbl = new JLabel();

  private final JTable fieldsTable = new JTable();

  private final JTextArea infoTA = new JTextArea();

  private IndexHandler indexHandler;

  private JDialog dialog;

  private String analyzerName = "org.apache.lucene.analysis.standard.StandardAnalyzer";

  public AddDocumentDialogFactory() {
    this.listeners = new AddDocumentDialogListeners(new Controller());
  }

  public void setIndexHandler(IndexHandler indexHandler) {
    this.indexHandler = indexHandler;
  }

  public void setAnalyzerName(String analyzerName) {
    this.analyzerName = analyzerName;
  }

  public class Controller {

  }

  @Override
  public JDialog create(JFrame owner, String title, int width, int height) {
    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    panel.add(header(), BorderLayout.PAGE_START);
    panel.add(center(), BorderLayout.CENTER);
    panel.add(footer(), BorderLayout.PAGE_END);
    return panel;
  }

  private JPanel header() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel analyzerHeader = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
    analyzerHeader.add(new JLabel(MessageUtils.getLocalizedMessage("add_document.label.analyzer")));
    analyzerNameLbl.setText(analyzerName);
    analyzerHeader.add(analyzerNameLbl);
    JLabel changeLbl = new JLabel(MessageUtils.getLocalizedMessage("add_document.hyperlink.change"));
    analyzerHeader.add(changeLbl);
    panel.add(analyzerHeader);

    return panel;
  }

  private JPanel center() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel tableHeader = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
    tableHeader.add(new JLabel(MessageUtils.getLocalizedMessage("add_document.label.fields")));
    panel.add(tableHeader, BorderLayout.PAGE_START);

    JScrollPane scrollPane = new JScrollPane(fieldsTable());
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel tableFooter = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 5));
    JButton addBtn = new JButton(MessageUtils.getLocalizedMessage("add_document.button.add"));
    addBtn.addActionListener(listeners.getAddBtnListener());
    tableFooter.add(addBtn);
    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    cancelBtn.addActionListener(e -> dialog.dispose());
    tableFooter.add(cancelBtn);
    panel.add(tableFooter, BorderLayout.PAGE_END);

    return panel;
  }

  private JTable fieldsTable() {
    TableUtil.setupTable(fieldsTable, ListSelectionModel.SINGLE_SELECTION, new FieldsTableModel(), null, 50, 200, 120);
    fieldsTable.setShowGrid(true);
    return fieldsTable;
  }

  private JPanel footer() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    infoTA.setRows(3);
    infoTA.setLineWrap(true);
    infoTA.setEditable(false);
    infoTA.setText(MessageUtils.getLocalizedMessage("add_document.info"));
    infoTA.setForeground(Color.gray);

    JScrollPane scrollPane = new JScrollPane(infoTA);
    panel.add(scrollPane);
    return panel;
  }

}

class FieldsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    DEL("Del", 0, Boolean.class),
    NAME("Name", 1, String.class),
    OPTIONS("Options", 2, String.class),
    VALUE("Value", 3, String.class);

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

  private static final int ROW_COUNT = 50;

  private static final TreeMap<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  FieldsTableModel() {
    this.data = new Object[ROW_COUNT][colNames.length];
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
    return true;
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
