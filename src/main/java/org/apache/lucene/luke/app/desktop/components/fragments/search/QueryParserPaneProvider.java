package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.search.QueryParserConfig;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.apache.lucene.luke.app.desktop.components.fragments.search.PointRangeQueryTableModel.NumType.INT;

public class QueryParserPaneProvider implements Provider<JScrollPane> {

  private final JRadioButton standardRB = new JRadioButton();

  private final JRadioButton classicRB = new JRadioButton();

  private final JComboBox<String> dfCB = new JComboBox<>();

  private final JComboBox<String> defOpCombo = new JComboBox<>(new String[]{QueryParserConfig.Operator.OR.name(), QueryParserConfig.Operator.AND.name()});

  private final JCheckBox posIncCB = new JCheckBox();

  private final JCheckBox wildCardCB = new JCheckBox();

  private final JCheckBox splitWSCB = new JCheckBox();

  private final JCheckBox genPhraseQueryCB = new JCheckBox();

  private final JCheckBox genMultiTermSynonymsPhraseQueryCB = new JCheckBox();

  private final JFormattedTextField slopFTF = new JFormattedTextField();

  private final JFormattedTextField minSimFTF = new JFormattedTextField();

  private final JFormattedTextField prefLenFTF = new JFormattedTextField();

  private final JComboBox<String> dateResCombo = new JComboBox<>();

  private final JTextField locationTF = new JTextField();

  private final JTextField timezoneTF = new JTextField();

  private final JTable pointRangeQueryTable = new JTable();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private final QueryParserConfig config = new QueryParserConfig.Builder().build();

  class ListenerFunctions {

    void selectStandardQParser(ActionEvent e) {
      splitWSCB.setEnabled(false);
      genPhraseQueryCB.setEnabled(false);
      genMultiTermSynonymsPhraseQueryCB.setEnabled(false);
      TableUtil.setEnabled(pointRangeQueryTable, true);
    }

    void selectClassicQparser(ActionEvent e) {
      splitWSCB.setEnabled(true);
      if (splitWSCB.isSelected()) {
        genPhraseQueryCB.setEnabled(true);
      } else {
        genPhraseQueryCB.setEnabled(false);
        genPhraseQueryCB.setSelected(false);
      }
      genMultiTermSynonymsPhraseQueryCB.setEnabled(true);
      pointRangeQueryTable.setEnabled(false);
      pointRangeQueryTable.setForeground(Color.gray);
      TableUtil.setEnabled(pointRangeQueryTable, false);
    }

    void toggleSplitOnWhiteSpace(ActionEvent e) {
      if (splitWSCB.isSelected()) {
        genPhraseQueryCB.setEnabled(true);
      } else {
        genPhraseQueryCB.setEnabled(false);
        genPhraseQueryCB.setSelected(false);
      }
    }

  }

  class QueryParserTabOperatorImpl implements QueryParserTabOperator {
    @Override
    public void setSearchableFields(Collection<String> searchableFields) {
      for (String field : searchableFields) {
        dfCB.addItem(field);
      }
    }

    @Override
    public void setRangeSearchableFields(Collection<String> rangeSearchableFields) {
      pointRangeQueryTable.setModel(new PointRangeQueryTableModel(rangeSearchableFields));
      pointRangeQueryTable.setShowGrid(true);
      String[] numTypes = Arrays.stream(PointRangeQueryTableModel.NumType.values())
          .map(PointRangeQueryTableModel.NumType::name)
          .toArray(String[]::new);
      JComboBox<String> numTypesCombo = new JComboBox<>(numTypes);
      numTypesCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value));
      pointRangeQueryTable.getColumnModel().getColumn(PointRangeQueryTableModel.Column.TYPE.getIndex()).setCellEditor(new DefaultCellEditor(numTypesCombo));
      pointRangeQueryTable.getColumnModel().getColumn(PointRangeQueryTableModel.Column.TYPE.getIndex()).setCellRenderer(
          (table, value, isSelected, hasFocus, row, column) -> new JLabel((String)value)
      );
      pointRangeQueryTable.getColumnModel().getColumn(PointRangeQueryTableModel.Column.FIELD.getIndex()).setPreferredWidth(300);
      pointRangeQueryTable.setPreferredScrollableViewportSize(pointRangeQueryTable.getPreferredSize());

      // set default type to Integer
      for (int i = 0; i < rangeSearchableFields.size(); i++) {
        pointRangeQueryTable.setValueAt(INT.name(), i, PointRangeQueryTableModel.Column.TYPE.getIndex());
      }

    }

    @Override
    public QueryParserConfig getConfig() {
      int phraseSlop = (int)slopFTF.getValue();
      float fuzzyMinSimFloat = (float)minSimFTF.getValue();
      int fuzzyPrefLenInt = (int)prefLenFTF.getValue();

      Map<String, Class<? extends Number>> typeMap = new HashMap<>();
      for (int row = 0; row < pointRangeQueryTable.getModel().getRowCount(); row++) {
        String field = (String)pointRangeQueryTable.getValueAt(row, PointRangeQueryTableModel.Column.FIELD.getIndex());
        String type = (String)pointRangeQueryTable.getValueAt(row, PointRangeQueryTableModel.Column.TYPE.getIndex());
        switch (PointRangeQueryTableModel.NumType.valueOf(type)) {
          case INT:
            typeMap.put(field, Integer.class);
            break;
          case LONG:
            typeMap.put(field, Long.class);
            break;
          case FLOAT:
            typeMap.put(field, Float.class);
            break;
          case DOUBLE:
            typeMap.put(field, Double.class);
            break;
          default:
            break;
        }
      }

      return new QueryParserConfig.Builder()
          .useClassicParser(classicRB.isSelected())
          .defaultOperator(QueryParserConfig.Operator.valueOf((String)defOpCombo.getSelectedItem()))
          .enablePositionIncrements(posIncCB.isSelected())
          .allowLeadingWildcard(wildCardCB.isSelected())
          .splitOnWhitespace(splitWSCB.isSelected())
          .autoGeneratePhraseQueries(genPhraseQueryCB.isSelected())
          .autoGenerateMultiTermSynonymsPhraseQuery(genMultiTermSynonymsPhraseQueryCB.isSelected())
          .phraseSlop(phraseSlop)
          .fuzzyMinSim(fuzzyMinSimFloat)
          .fuzzyPrefixLength(fuzzyPrefLenInt)
          .dateResolution(DateTools.Resolution.valueOf((String)dateResCombo.getSelectedItem()))
          .locale(new Locale(locationTF.getText()))
          .timeZone(TimeZone.getTimeZone(timezoneTF.getText()))
          .typeMap(typeMap)
          .build();
    }

    @Override
    public String getDefaultField() {
      return (String)dfCB.getSelectedItem();
    }
  }

  @Inject
  public QueryParserPaneProvider(ComponentOperatorRegistry operatorRegistry) {
    operatorRegistry.register(QueryParserTabOperator.class, new QueryParserTabOperatorImpl());
  }

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(selectParserPane());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(parserSettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(phraseQuerySettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(fuzzyQuerySettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(dateRangeQuerySettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(pointRangeQuerySettings());

    return new JScrollPane(panel);
  }

  private JPanel selectParserPane() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    standardRB.setText("StandardQueryParser");
    standardRB.setSelected(true);
    standardRB.addActionListener(listeners::selectStandardQParser);

    classicRB.setText("Classic QueryParser");
    classicRB.addActionListener(listeners::selectClassicQparser);

    ButtonGroup group = new ButtonGroup();
    group.add(standardRB);
    group.add(classicRB);

    panel.add(standardRB);
    panel.add(classicRB);

    return panel;
  }

  private JPanel parserSettings() {
    JPanel panel = new JPanel(new GridLayout(3, 2));

    JPanel defField = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JLabel dfLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.df"));
    defField.add(dfLabel);
    defField.add(dfCB);
    panel.add(defField);

    JPanel defOp = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JLabel defOpLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.dop"));
    defOp.add(defOpLabel);
    defOpCombo.setSelectedItem(config.getDefaultOperator().name());
    defOp.add(defOpCombo);
    panel.add(defOp);

    posIncCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.pos_incr"));
    posIncCB.setSelected(config.isEnablePositionIncrements());
    panel.add(posIncCB);

    wildCardCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.lead_wildcard"));
    wildCardCB.setSelected(config.isAllowLeadingWildcard());
    panel.add(wildCardCB);

    splitWSCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.split_ws"));
    splitWSCB.setEnabled(config.isSplitOnWhitespace());
    splitWSCB.addActionListener(listeners::toggleSplitOnWhiteSpace);
    panel.add(splitWSCB);

    return panel;
  }

  private JPanel phraseQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.phrase_query")));
    panel.add(header);

    JPanel genPQ = new JPanel(new FlowLayout(FlowLayout.LEADING));
    genPQ.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    genPhraseQueryCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.gen_pq"));
    genPhraseQueryCB.setEnabled(config.isAutoGeneratePhraseQueries());
    genPQ.add(genPhraseQueryCB);
    panel.add(genPQ);

    JPanel genMTPQ = new JPanel(new FlowLayout(FlowLayout.LEADING));
    genMTPQ.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    genMultiTermSynonymsPhraseQueryCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.gen_mts"));
    genMultiTermSynonymsPhraseQueryCB.setEnabled(config.isAutoGenerateMultiTermSynonymsPhraseQuery());
    genMTPQ.add(genMultiTermSynonymsPhraseQueryCB);
    panel.add(genMTPQ);

    JPanel slop = new JPanel(new FlowLayout(FlowLayout.LEADING));
    slop.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel slopLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.phrase_slop"));
    slop.add(slopLabel);
    slopFTF.setColumns(5);
    slopFTF.setValue(config.getPhraseSlop());
    slop.add(slopFTF);
    slop.add(new JLabel(MessageUtils.getLocalizedMessage("label.int_required")));
    panel.add(slop);

    return panel;
  }

  private JPanel fuzzyQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_query")));
    panel.add(header);

    JPanel minSim = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minSim.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel minSimLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_minsim"));
    minSim.add(minSimLabel);
    minSimFTF.setColumns(5);
    minSimFTF.setValue(config.getFuzzyMinSim());
    minSim.add(minSimFTF);
    minSim.add(new JLabel(MessageUtils.getLocalizedMessage("label.float_required")));
    panel.add(minSim);

    JPanel prefLen = new JPanel(new FlowLayout(FlowLayout.LEADING));
    prefLen.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel prefLenLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_preflen"));
    prefLen.add(prefLenLabel);
    prefLenFTF.setColumns(5);
    prefLenFTF.setValue(config.getFuzzyPrefixLength());
    prefLen.add(prefLenFTF);
    prefLen.add(new JLabel(MessageUtils.getLocalizedMessage("label.int_required")));
    panel.add(prefLen);

    return panel;
  }

  private JPanel dateRangeQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.daterange_query")));
    panel.add(header);

    JPanel resolution = new JPanel(new FlowLayout(FlowLayout.LEADING));
    resolution.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel resLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.date_res"));
    resolution.add(resLabel);
    Arrays.stream(DateTools.Resolution.values()).map(DateTools.Resolution::name).forEach(dateResCombo::addItem);
    dateResCombo.setSelectedItem(config.getDateResolution().name());
    resolution.add(dateResCombo);
    panel.add(resolution);

    JPanel locale = new JPanel(new FlowLayout(FlowLayout.LEADING));
    locale.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel locLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.locale"));
    locale.add(locLabel);
    locationTF.setColumns(10);
    locationTF.setText(config.getLocale().toString());
    locale.add(locationTF);
    JLabel tzLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.timezone"));
    locale.add(tzLabel);
    timezoneTF.setColumns(10);
    timezoneTF.setText(config.getTimeZone().getID());
    locale.add(timezoneTF);
    panel.add(locale);

    return panel;
  }

  private JPanel pointRangeQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.pointrange_query")));
    panel.add(header);

    JPanel headerNote = new JPanel(new FlowLayout(FlowLayout.LEADING));
    headerNote.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.pointrange_hint")));
    panel.add(headerNote);

    TableUtil.setupTable(pointRangeQueryTable, ListSelectionModel.SINGLE_SELECTION, new PointRangeQueryTableModel(), null);
    pointRangeQueryTable.setShowGrid(true);
    JScrollPane scrollPane = new JScrollPane(pointRangeQueryTable);
    panel.add(scrollPane);

    return panel;
  }

  public interface QueryParserTabOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setSearchableFields(Collection<String> searchableFields);
    void setRangeSearchableFields(Collection<String> rangeSearchableFields);
    QueryParserConfig getConfig();
    String getDefaultField();
  }

}

class PointRangeQueryTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {

    FIELD("Field", 0, String.class),
    TYPE("Numeric Type", 1, NumType.class);

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

  enum NumType {

    INT, LONG, FLOAT, DOUBLE

  }

  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  PointRangeQueryTableModel() {
    this.data = new Object[0][colNames.length];
  }

  PointRangeQueryTableModel(Collection<String> rangeSearchableFields) {
    this.data = new Object[rangeSearchableFields.size()][colNames.length];
    int i = 0;
    for (String field : rangeSearchableFields) {
      data[i++][Column.FIELD.getIndex()] = field;
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
    if (columnIndex == Column.TYPE.getIndex()) {
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

