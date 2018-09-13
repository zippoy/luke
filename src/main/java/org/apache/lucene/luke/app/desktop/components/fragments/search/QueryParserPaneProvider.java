package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Map;

public class QueryParserPaneProvider implements Provider<JScrollPane> {

  private final JRadioButton standardRB = new JRadioButton();

  private final JRadioButton classicRB = new JRadioButton();

  private final JComboBox<String> dfCB = new JComboBox<>();

  private final JComboBox<String> defOpCB = new JComboBox<>(new String[]{"OR", "AND"});

  private final JCheckBox posIncCB = new JCheckBox();

  private final JCheckBox wildCardCB = new JCheckBox();

  private final JCheckBox splitWS = new JCheckBox();

  private final JCheckBox genPhraseQueryCB = new JCheckBox();

  private final JCheckBox genMultiTermSynonymsPhraseQueryCB = new JCheckBox();

  private final JTextField slopTF = new JTextField();

  private final JTextField minSimTF = new JTextField();

  private final JTextField prefLenTF = new JTextField();

  private final JComboBox<String> dateResCB = new JComboBox<>();

  private final JTextField locationTF = new JTextField();

  private final JTextField timezoneTF = new JTextField();

  private final JTable pointRangeQueryTable = new JTable();

  private final Controller controller;

  class Controller {

    void selectStandardQParser() {
      splitWS.setEnabled(false);
      genPhraseQueryCB.setEnabled(false);
      genMultiTermSynonymsPhraseQueryCB.setEnabled(false);
      pointRangeQueryTable.setEnabled(false);
    }

    void selectClassicQparser() {
      splitWS.setEnabled(true);
      genPhraseQueryCB.setEnabled(true);
      genMultiTermSynonymsPhraseQueryCB.setEnabled(true);
      pointRangeQueryTable.setEnabled(true);
    }
  }

  public QueryParserPaneProvider() {
    this.controller = new Controller();
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
    standardRB.addActionListener(e -> controller.selectStandardQParser());

    classicRB.setText("Classic QueryParser");
    classicRB.addActionListener(e -> controller.selectClassicQparser());

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
    defOp.add(defOpCB);
    panel.add(defOp);

    posIncCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.pos_incr"));
    posIncCB.setSelected(true);
    panel.add(posIncCB);

    wildCardCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.lead_wildcard"));
    panel.add(wildCardCB);

    splitWS.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.split_ws"));
    splitWS.setEnabled(false);
    panel.add(splitWS);

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
    genPhraseQueryCB.setEnabled(false);
    genPQ.add(genPhraseQueryCB);
    panel.add(genPQ);

    JPanel genMTPQ = new JPanel(new FlowLayout(FlowLayout.LEADING));
    genMTPQ.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    genMultiTermSynonymsPhraseQueryCB.setText(MessageUtils.getLocalizedMessage("search_parser.checkbox.gen_mts"));
    genMultiTermSynonymsPhraseQueryCB.setEnabled(false);
    genMTPQ.add(genMultiTermSynonymsPhraseQueryCB);
    panel.add(genMTPQ);

    JPanel slop = new JPanel(new FlowLayout(FlowLayout.LEADING));
    slop.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel slopLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.phrase_slop"));
    slop.add(slopLabel);
    slopTF.setColumns(5);
    slop.add(slopTF);
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
    minSimTF.setColumns(5);
    minSim.add(minSimTF);
    minSim.add(new JLabel(MessageUtils.getLocalizedMessage("label.float_required")));
    panel.add(minSim);

    JPanel prefLen = new JPanel(new FlowLayout(FlowLayout.LEADING));
    prefLen.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel prefLenLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_preflen"));
    prefLen.add(prefLenLabel);
    prefLenTF.setColumns(5);
    prefLen.add(prefLenTF);
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
    resolution.add(dateResCB);
    panel.add(resolution);

    JPanel locale = new JPanel(new FlowLayout(FlowLayout.LEADING));
    locale.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel locLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.locale"));
    locale.add(locLabel);
    locationTF.setColumns(10);
    locale.add(locationTF);
    JLabel tzLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.timezone"));
    locale.add(tzLabel);
    timezoneTF.setColumns(10);
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
    JScrollPane scrollPane = new JScrollPane(pointRangeQueryTable);
    panel.add(scrollPane);

    return panel;
  }
}

class PointRangeQueryTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {

    FIELD("Field", 0, String.class),
    TYPE("Numeric Type", 1, String.class);

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

  PointRangeQueryTableModel() {
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
    return true;
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}