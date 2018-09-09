package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.dialog.HelpDialogFactory;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.components.util.HelpHeaderRenderer;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.listeners.dialog.documents.AddDocumentDialogListeners;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AddDocumentDialogFactory implements DialogOpener.DialogFactory {

  private final AddDocumentDialogListeners listeners;

  private final JLabel analyzerNameLbl = new JLabel();

  private final JTable fieldsTable = new JTable();

  private final JTextArea infoTA = new JTextArea();

  private IndexHandler indexHandler;

  private IndexOptionsDialogFactory indexOptionsDialogFactory;

  private HelpDialogFactory helpDialogFactory;

  private JDialog dialog;

  private String analyzerName = "org.apache.lucene.analysis.standard.StandardAnalyzer";

  public AddDocumentDialogFactory() {
    this.listeners = new AddDocumentDialogListeners(new Controller());
  }

  public void setIndexOptionsDialogFactory(IndexOptionsDialogFactory indexOptionsDialogFactory) {
    this.indexOptionsDialogFactory = indexOptionsDialogFactory;
  }

  public void setHelpDialogFactory(HelpDialogFactory helpDialogFactory) {
    this.helpDialogFactory = helpDialogFactory;
  }

  public void setIndexHandler(IndexHandler indexHandler) {
    this.indexHandler = indexHandler;
  }

  public void setAnalyzerName(String analyzerName) {
    this.analyzerName = analyzerName;
  }

  public class Controller {
    public void showIndexOptionsDialog() {
      String title = "Index options for:";
      new DialogOpener<>(indexOptionsDialogFactory).open(title, 500, 500,
          (factory) -> {});
    }
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
    TableUtil.setupTable(fieldsTable, ListSelectionModel.SINGLE_SELECTION, new FieldsTableModel(), null, 30, 150, 120, 80);
    fieldsTable.setShowGrid(true);
    String[] types = presetFields().stream().map(Class::getSimpleName).toArray(String[]::new);
    JComboBox<String> typesCombo = new JComboBox<>(types);
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.TYPE.getIndex()).setCellEditor(new DefaultCellEditor(typesCombo));
    for (int i = 0; i < fieldsTable.getModel().getRowCount(); i++) {
      fieldsTable.getModel().setValueAt(TextField.class.getSimpleName(), i, FieldsTableModel.Column.TYPE.getIndex());
    }
    TableCellRenderer renderer = new HelpHeaderRenderer(
        "About Type", "Select Field Class:",
        createTypeHelpDialog(),
        helpDialogFactory);
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.TYPE.getIndex()).setHeaderRenderer(renderer);
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.OPTIONS.getIndex()).setCellRenderer(new OptionsCellRenderer(indexOptionsDialogFactory));
    return fieldsTable;
  }

  private JComponent createTypeHelpDialog() {
    JPanel panel = new JPanel(new BorderLayout());

    JTextArea descTA = new JTextArea();

    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));
    String[] typeList = new String[]{
        "TextField",
        "StringField",
        "IntPoint",
        "LongPoint",
        "FloatPoint",
        "DoublePoint",
        "SortedDocValuesField",
        "SortedSetDocValuesField",
        "NumericDocValuesField",
        "SortedNumericDocValuesField",
        "StoredField",
        "Field"
    };
    JPanel wrapper1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JComboBox<String> typeCombo = new JComboBox<>(typeList);
    typeCombo.setSelectedItem(typeList[0]);
    typeCombo.addActionListener(e -> {
      String selected = (String)typeCombo.getSelectedItem();
      descTA.setText(MessageUtils.getLocalizedMessage("help.fieldtype." + selected));
    });
    wrapper1.add(typeCombo);
    header.add(wrapper1);
    JPanel wrapper2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    wrapper2.add(new JLabel("Brief description and Examples"));
    header.add(wrapper2);
    panel.add(header, BorderLayout.PAGE_START);

    descTA.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    descTA.setEditable(false);
    descTA.setLineWrap(true);
    descTA.setRows(10);
    descTA.setText(MessageUtils.getLocalizedMessage("help.fieldtype." + typeList[0]));
    JScrollPane scrollPane = new JScrollPane(descTA);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
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

  @SuppressWarnings("unchecked")
  private static List<Class<? extends Field>> presetFields() {
    final Class[] presetFieldClasses = new Class[]{
        TextField.class, StringField.class,
        IntPoint.class, LongPoint.class, FloatPoint.class, DoublePoint.class,
        SortedDocValuesField.class, SortedSetDocValuesField.class,
        NumericDocValuesField.class, SortedNumericDocValuesField.class,
        StoredField.class, Field.class
    };
    return Arrays.asList(presetFieldClasses);
  }


}

class FieldsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    DEL("Del", 0, Boolean.class),
    NAME("Name", 1, String.class),
    TYPE("Type", 2, String.class),
    OPTIONS("Options", 3, String.class),
    VALUE("Value", 4, String.class);

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
    if (columnIndex == Column.OPTIONS.getIndex()) {
      return "";
    }
    return data[rowIndex][columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == Column.OPTIONS.getIndex()) {
      return false;
    }
    return true;
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}

class OptionsCellRenderer implements TableCellRenderer {

  private JTable table;

  private final JPanel panel = new JPanel();

  private final IndexOptionsDialogFactory indexOptionsDialogFactory;

  public OptionsCellRenderer(IndexOptionsDialogFactory indexOptionsDialogFactory) {
    this.indexOptionsDialogFactory = indexOptionsDialogFactory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (table != null && this.table != table) {
      this.table = table;
      final JTableHeader header = table.getTableHeader();
      if (header != null) {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        panel.add(new JLabel(value.toString()));

        JLabel optionsLbl = new JLabel("options");
        optionsLbl.setForeground(Color.decode("#0099ff"));
        optionsLbl.setBackground(Color.white);
        Font font = optionsLbl.getFont();
        Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        optionsLbl.setFont(font.deriveFont(attributes));
        table.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (row >= 0 && col == FieldsTableModel.Column.OPTIONS.getIndex()) {
              String title = "Index options for:";
              new DialogOpener<>(indexOptionsDialogFactory).open(title, 500, 500,
                  (factory) -> {
                  });
            }
          }
        });
        panel.add(optionsLbl);
      }
    }
    return panel;
  }

}