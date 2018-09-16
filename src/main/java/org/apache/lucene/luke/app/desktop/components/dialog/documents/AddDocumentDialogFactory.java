package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.DocumentsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.dialog.HelpDialogFactory;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.components.util.FontUtil;
import org.apache.lucene.luke.app.desktop.components.util.HelpHeaderRenderer;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.dto.documents.NewField;
import org.apache.lucene.luke.app.desktop.listeners.dialog.documents.AddDocumentDialogListeners;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.models.tools.IndexToolsFactory;

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
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AddDocumentDialogFactory implements DialogOpener.DialogFactory {

  private final static int ROW_COUNT = 50;

  private final AddDocumentDialogListeners listeners;

  private final JLabel analyzerNameLbl = new JLabel();

  private final JTable fieldsTable = new JTable();

  private final List<NewField> newFieldList;

  private final JButton addBtn = new JButton();

  private final JButton closeBtn = new JButton();

  private final JTextArea infoTA = new JTextArea();

  private final IndexOptionsDialogFactory indexOptionsDialogFactory;

  private final HelpDialogFactory helpDialogFactory;

  private final IndexHandler indexHandler;

  private final IndexToolsFactory toolsFactory;

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  //private final Optional<DocumentsPanelProvider.DocumentsTabOperator> documentsTabOperator;
  private final ComponentOperatorRegistry operatorRegistry;

  private IndexTools toolsModel;

  private JDialog dialog;

  private Analyzer currentAnalyzer = new StandardAnalyzer();

  private String analyzerName = "org.apache.lucene.analysis.standard.StandardAnalyzer";

  public class Controller {

    public List<NewField> getNewFieldList() {
      return ImmutableList.copyOf(newFieldList);
    }

    public void addDocument(Document doc) {
      try {
        toolsModel.addDocument(doc, currentAnalyzer);
        indexHandler.reOpen();
        operatorRegistry.get(DocumentsPanelProvider.DocumentsTabOperator.class).ifPresent(DocumentsPanelProvider.DocumentsTabOperator::displayLatestDoc);
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.DOCUMENTS);
        infoTA.setText(MessageUtils.getLocalizedMessage("add_document.message.success"));
        addBtn.setEnabled(false);
        closeBtn.setText(MessageUtils.getLocalizedMessage("button.close"));
      } catch (LukeException e) {
        infoTA.setText(MessageUtils.getLocalizedMessage("add_document.message.fail"));
        throw e;
      } catch (Exception e) {
        infoTA.setText(MessageUtils.getLocalizedMessage("add_document.message.fail"));
        throw new LukeException(e.getMessage(), e);
      }
    }

    public void setInfo(String text) {
      infoTA.setText(text);
    }
  }

  public class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      toolsModel = toolsFactory.newInstance(state.getIndexReader(), state.useCompound(), state.keepAllCommits());
    }

    @Override
    public void closeIndex() {

    }
  }

  @Inject
  public AddDocumentDialogFactory(IndexOptionsDialogFactory indexOptionsDialogFactory, HelpDialogFactory helpDialogFactory,
                                  IndexHandler indexHandler, IndexToolsFactory toolsFactory,
                                  TabbedPaneProvider.TabSwitcherProxy tabSwitcher,
                                  ComponentOperatorRegistry operatorRegistry) {
    this.indexOptionsDialogFactory = indexOptionsDialogFactory;
    this.helpDialogFactory = helpDialogFactory;
    this.indexHandler = indexHandler;
    this.toolsFactory = toolsFactory;
    this.tabSwitcher = tabSwitcher;
    this.operatorRegistry = operatorRegistry;
    this.listeners = new AddDocumentDialogListeners(new Controller());

    indexHandler.addObserver(new Observer());
    newFieldList = IntStream.range(0, ROW_COUNT).mapToObj(i -> NewField.newInstance()).collect(Collectors.toList());
  }

  public void setAnalyzerName(String analyzerName) {
    this.analyzerName = analyzerName;
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
    changeLbl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        dialog.dispose();
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.ANALYZER);
      }
    });
    analyzerHeader.add(FontUtil.toLinkText(changeLbl));
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
    addBtn.setText(MessageUtils.getLocalizedMessage("add_document.button.add"));
    addBtn.addActionListener(listeners.getAddBtnListener());
    tableFooter.add(addBtn);
    closeBtn.setText(MessageUtils.getLocalizedMessage("button.cancel"));
    closeBtn.addActionListener(e -> dialog.dispose());
    tableFooter.add(closeBtn);
    panel.add(tableFooter, BorderLayout.PAGE_END);

    return panel;
  }

  private JTable fieldsTable() {
    TableUtil.setupTable(fieldsTable, ListSelectionModel.SINGLE_SELECTION, new FieldsTableModel(newFieldList), null, 30, 150, 120, 80);
    fieldsTable.setShowGrid(true);
    JComboBox<Class> typesCombo = new JComboBox<>(presetFieldClasses);
    typesCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getSimpleName()));
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.TYPE.getIndex()).setCellEditor(new DefaultCellEditor(typesCombo));
    for (int i = 0; i < fieldsTable.getModel().getRowCount(); i++) {
      fieldsTable.getModel().setValueAt(TextField.class, i, FieldsTableModel.Column.TYPE.getIndex());
    }
    TableCellRenderer renderer = new HelpHeaderRenderer(
        "About Type", "Select Field Class:",
        createTypeHelpDialog(),
        helpDialogFactory);
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.TYPE.getIndex()).setHeaderRenderer(renderer);
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.TYPE.getIndex()).setCellRenderer(new TypeCellRenderer());
    fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.OPTIONS.getIndex()).setCellRenderer(new OptionsCellRenderer(indexOptionsDialogFactory, newFieldList));
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

  private final Class[] presetFieldClasses = new Class[]{
      TextField.class, StringField.class,
      IntPoint.class, LongPoint.class, FloatPoint.class, DoublePoint.class,
      SortedDocValuesField.class, SortedSetDocValuesField.class,
      NumericDocValuesField.class, SortedNumericDocValuesField.class,
      StoredField.class, Field.class
  };

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
    TYPE("Type", 2, Class.class),
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

  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  private final List<NewField> newFieldList;

  FieldsTableModel(List<NewField> newFieldList) {
    this.data = new Object[newFieldList.size()][colNames.length];
    this.newFieldList = newFieldList;
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
    NewField selectedField = newFieldList.get(rowIndex);
    if (columnIndex == Column.DEL.getIndex()) {
      selectedField.setDeleted((Boolean)value);
    } else if (columnIndex == Column.NAME.getIndex()) {
      selectedField.setName((String)value);
    } else if (columnIndex == Column.TYPE.getIndex()) {
      selectedField.setType((Class)value);
      selectedField.resetFieldType((Class)value);
      selectedField.setStored(selectedField.getFieldType().stored());
    } else if (columnIndex == Column.VALUE.getIndex()) {
      selectedField.setValue((String)value);
    }
  }
}

class TypeCellRenderer implements TableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String simpleName = ((Class)value).getSimpleName();
    return new JLabel(simpleName);
  }
}

class OptionsCellRenderer implements TableCellRenderer {

  private JTable table;

  private final JPanel panel = new JPanel();

  private final IndexOptionsDialogFactory indexOptionsDialogFactory;

  private final List<NewField> newFieldList;

  public OptionsCellRenderer(IndexOptionsDialogFactory indexOptionsDialogFactory, List<NewField> newFieldList) {
    this.indexOptionsDialogFactory = indexOptionsDialogFactory;
    this.newFieldList = newFieldList;
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
        table.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (row >= 0 && col == FieldsTableModel.Column.OPTIONS.getIndex()) {
              String title = "Index options for:";
              new DialogOpener<>(indexOptionsDialogFactory).open(title, 500, 500,
                  (factory) -> {
                    factory.setNewField(newFieldList.get(row));
                  });
            }
          }
        });
        panel.add(FontUtil.toLinkText(optionsLbl));
      }
    }
    return panel;
  }

}