package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.util.StyleConstants;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.overview.Overview;
import org.apache.lucene.luke.models.overview.OverviewFactory;
import org.apache.lucene.luke.models.overview.TermCountsOrder;
import org.apache.lucene.luke.models.overview.TermStats;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OverviewPanelProvider implements Provider<JPanel> {

  private static final int GRIDX_DESC = 0;
  private static final int GRIDX_VAL = 1;
  private static final double WEIGHTX_DESC = 0.1;
  private static final double WEIGHTX_VAL = 0.9;

  private final OverviewFactory overviewFactory;

  private final ComponentOperatorRegistry operatorRegistry;

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  private final MessageBroker messageBroker;

  private final JPanel panel = new JPanel();

  private final JLabel indexPathLbl = new JLabel();

  private final JLabel numFieldsLbl = new JLabel();

  private final JLabel numDocsLbl = new JLabel();

  private final JLabel numTermsLbl = new JLabel();

  private final JLabel delOptLbl = new JLabel();

  private final JLabel indexVerLbl = new JLabel();

  private final JLabel indexFmtLbl = new JLabel();

  private final JLabel dirImplLbl = new JLabel();

  private final JLabel commitPointLbl = new JLabel();

  private final JLabel commitUserDataLbl = new JLabel();

  private final JTable termCountsTable = new JTable();

  private final JTextField selectedField = new JTextField();

  private final JButton showTopTermsBtn = new JButton();

  private final JSpinner numTopTermsSpnr = new JSpinner();

  private final JTable topTermsTable = new JTable();

  private final JPopupMenu topTermsContextMenu = new JPopupMenu();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private Overview overviewModel;

  class ListenerFunctions {

    void selectField(MouseEvent e) {
      String field = getSelectedField();
      selectedField.setText(field);
      showTopTermsBtn.setEnabled(true);
    }

    void showTopTerms(ActionEvent e) {
      String field = getSelectedField();
      int numTerms = (int)numTopTermsSpnr.getModel().getValue();
      List<TermStats> termStats = overviewModel.getTopTerms(field, numTerms);

      // update top terms table
      topTermsTable.setModel(new TopTermsTableModel(termStats, numTerms));
      topTermsTable.getColumnModel().getColumn(TopTermsTableModel.Column.RANK.getIndex()).setMaxWidth(50);
      topTermsTable.getColumnModel().getColumn(TopTermsTableModel.Column.FREQ.getIndex()).setMaxWidth(80);
      messageBroker.clearStatusMessage();
    }

    void showTopTermsContextMenu(MouseEvent e) {
      if (e.getClickCount() == 2 && !e.isConsumed()) {
        int row = topTermsTable.rowAtPoint(e.getPoint());
        if (row != topTermsTable.getSelectedRow()) {
          topTermsTable.changeSelection(row, topTermsTable.getSelectedColumn(), false, false);
        }
        topTermsContextMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    void browseByTerm(ActionEvent e) {
      String field = getSelectedField();
      String term = getSelectedTerm();
      operatorRegistry.get(DocumentsPanelProvider.DocumentsTabOperator.class).ifPresent(operator -> {
        operator.browseTerm(field, term);
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.DOCUMENTS);
      });
    }

    void searchByTerm(ActionEvent e) {
      String field = getSelectedField();
      String term = getSelectedTerm();
      operatorRegistry.get(SearchPanelProvider.SearchTabOperator.class).ifPresent(operator -> {
        operator.searchByTerm(field, term);
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.SEARCH);
      });
    }

    private String getSelectedField() {
      int row = termCountsTable.getSelectedRow();
      if (row < 0 || row >= termCountsTable.getRowCount()) {
        throw new IllegalStateException("Field is not selected.");
      }
      return (String)termCountsTable.getModel().getValueAt(row, TermCountsTableModel.Column.NAME.getIndex());
    }

    private String getSelectedTerm() {
      int rowTerm = topTermsTable.getSelectedRow();
      if (rowTerm < 0 || rowTerm >= topTermsTable.getRowCount()) {
        throw new IllegalStateException("Term is not selected.");
      }
      return (String)topTermsTable.getModel().getValueAt(rowTerm, TopTermsTableModel.Column.TEXT.getIndex());
    }

  }

  public class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      overviewModel = overviewFactory.newInstance(state.getIndexReader(), state.getIndexPath());

      indexPathLbl.setText(overviewModel.getIndexPath());
      indexPathLbl.setToolTipText(overviewModel.getIndexPath());
      numFieldsLbl.setText(Integer.toString(overviewModel.getNumFields()));
      numDocsLbl.setText(Integer.toString(overviewModel.getNumDocuments()));
      numTermsLbl.setText(Long.toString(overviewModel.getNumTerms()));
      String del = overviewModel.hasDeletions() ? String.format("Yes (%d)", overviewModel.getNumDeletedDocs()) : "No";
      String opt = overviewModel.isOptimized().map(b -> b ? "Yes" : "No").orElse("?");
      delOptLbl.setText(String.format("%s / %s", del, opt));
      indexVerLbl.setText(overviewModel.getIndexVersion().map(v -> Long.toString(v)).orElse("?"));
      indexFmtLbl.setText(overviewModel.getIndexFormat().orElse(""));
      dirImplLbl.setText(overviewModel.getDirImpl().orElse(""));
      commitPointLbl.setText(overviewModel.getCommitDescription().orElse("---"));
      commitUserDataLbl.setText(overviewModel.getCommitUserData().orElse("---"));

      // term counts table
      Map<String, Long> termCounts = overviewModel.getSortedTermCounts(TermCountsOrder.COUNT_DESC);
      long numTerms = overviewModel.getNumTerms();
      termCountsTable.setModel(new TermCountsTableModel(numTerms, termCounts));
      termCountsTable.setRowSorter(new TableRowSorter<>(termCountsTable.getModel()));
      termCountsTable.getColumnModel().getColumn(TermCountsTableModel.Column.NAME.getIndex()).setMaxWidth(120);
      termCountsTable.getColumnModel().getColumn(TermCountsTableModel.Column.TERM_COUNT.getIndex()).setMaxWidth(100);
      DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
      rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
      termCountsTable.getColumnModel().getColumn(TermCountsTableModel.Column.RATIO.getIndex()).setCellRenderer(rightRenderer);

      // top terms table
      topTermsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      topTermsTable.getColumnModel().getColumn(TopTermsTableModel.Column.RANK.getIndex()).setMaxWidth(50);
      topTermsTable.getColumnModel().getColumn(TopTermsTableModel.Column.FREQ.getIndex()).setMaxWidth(80);
      topTermsTable.getColumnModel().setColumnMargin(StyleConstants.TABLE_COLUMN_MARGIN_DEFAULT);
    }

    @Override
    public void closeIndex() {
      indexPathLbl.setText("");
      numFieldsLbl.setText("");
      numDocsLbl.setText("");
      numTermsLbl.setText("");
      delOptLbl.setText("");
      indexVerLbl.setText("");
      indexFmtLbl.setText("");
      dirImplLbl.setText("");
      commitPointLbl.setText("");
      commitUserDataLbl.setText("");

      selectedField.setText("");
      showTopTermsBtn.setEnabled(false);
      numTopTermsSpnr.setEnabled(false);

      termCountsTable.setRowSorter(null);
      termCountsTable.setModel(new TermCountsTableModel());
      topTermsTable.setModel(new TopTermsTableModel());
    }

    private Observer() {}
  }

  @Inject
  public OverviewPanelProvider(
      OverviewFactory overviewFactory,
      MessageBroker messageBroker,
      ComponentOperatorRegistry operatorRegistry,
      IndexHandler indexHandler,
      TabbedPaneProvider.TabSwitcherProxy tabSwitcher) {
    this.overviewFactory = overviewFactory;
    this.messageBroker = messageBroker;
    this.operatorRegistry = operatorRegistry;
    this.tabSwitcher = tabSwitcher;

    indexHandler.addObserver(new Observer());
  }

  @Override
  public JPanel get() {
    panel.setLayout(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(0.4);
    panel.add(splitPane);

    setUpTopTermsContextMenu();

    return panel;
  }

  private JPanel createUpperPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(2, 10, 2, 2);
    c.gridy = 0;

    c.gridx = GRIDX_DESC;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_path"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    indexPathLbl.setText("?");
    panel.add(indexPathLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_fields"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    numFieldsLbl.setText("?");
    panel.add(numFieldsLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_docs"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    numDocsLbl.setText("?");
    panel.add(numDocsLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_terms"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    numTermsLbl.setText("?");
    panel.add(numTermsLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.del_opt"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    delOptLbl.setText("?");
    panel.add(delOptLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_version"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    indexVerLbl.setText("?");
    panel.add(indexVerLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_format"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    indexFmtLbl.setText("?");
    panel.add(indexFmtLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.dir_impl"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    dirImplLbl.setText("?");
    panel.add(dirImplLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.commit_point"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    commitPointLbl.setText("?");
    panel.add(commitPointLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.commit_userdata"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    commitUserDataLbl.setText("?");
    panel.add(commitUserDataLbl, c);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("overview.label.select_fields"));
    label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    panel.add(label, BorderLayout.PAGE_START);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTermCountsPanel(), createTopTermsPanel());
    splitPane.setDividerLocation(320);
    splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(splitPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createTermCountsPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("overview.label.available_fields"));
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    panel.add(label, BorderLayout.PAGE_START);

    TableUtil.setupTable(termCountsTable, ListSelectionModel.SINGLE_SELECTION, new TermCountsTableModel(), new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        listeners.selectField(e);
      }
    });
    JScrollPane scrollPane = new JScrollPane(termCountsTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createTopTermsPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));

    JPanel selectedPanel = new JPanel(new BorderLayout());
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS));
    innerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    selectedPanel.add(innerPanel, BorderLayout.PAGE_START);

    JPanel innerPanel1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    innerPanel1.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.selected_field")));
    innerPanel.add(innerPanel1);

    selectedField.setColumns(10);
    selectedField.setFont(StyleConstants.FONT_MONOSPACE_LARGE);
    selectedField.setEditable(false);
    selectedField.setBackground(Color.white);
    JPanel innerPanel2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    innerPanel2.add(selectedField);
    innerPanel.add(innerPanel2);

    showTopTermsBtn.setText(MessageUtils.getLocalizedMessage("overview.button.show_terms"));
    showTopTermsBtn.setPreferredSize(new Dimension(170, 40));
    showTopTermsBtn.setFont(StyleConstants.BUTTON_FONT_LARGE);
    showTopTermsBtn.addActionListener(listeners::showTopTerms);
    showTopTermsBtn.setEnabled(false);
    JPanel innerPanel3 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    innerPanel3.add(showTopTermsBtn);
    innerPanel.add(innerPanel3);

    JPanel innerPanel4 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    innerPanel4.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_top_terms")));
    innerPanel.add(innerPanel4);

    SpinnerNumberModel numberModel = new SpinnerNumberModel(50, 0, 1000, 1);
    numTopTermsSpnr.setModel(numberModel);
    JPanel innerPanel5 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    innerPanel5.add(numTopTermsSpnr);
    innerPanel.add(innerPanel5);

    JPanel termsPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("overview.label.top_terms"));
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    termsPanel.add(label, BorderLayout.PAGE_START);

    TableUtil.setupTable(topTermsTable, ListSelectionModel.SINGLE_SELECTION, new TopTermsTableModel(), new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        listeners.showTopTermsContextMenu(e);
      }
    });
    JScrollPane scrollPane = new JScrollPane(topTermsTable);
    termsPanel.add(scrollPane, BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, selectedPanel, termsPanel);
    splitPane.setDividerLocation(180);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    panel.add(splitPane);

    return panel;
  }

  private void setUpTopTermsContextMenu() {
    JMenuItem item1 = new JMenuItem(MessageUtils.getLocalizedMessage("overview.toptermtable.menu.item1"));
    item1.addActionListener(listeners::browseByTerm);
    topTermsContextMenu.add(item1);

    JMenuItem item2 = new JMenuItem(MessageUtils.getLocalizedMessage("overview.toptermtable.menu.item2"));
    item2.addActionListener(listeners::searchByTerm);
    topTermsContextMenu.add(item2);
  }

}

class TermCountsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {

    NAME("Name", 0, String.class),
    TERM_COUNT("Term count", 1, Long.class),
    RATIO("%", 2, String.class);

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

  TermCountsTableModel() {
    data = new Object[0][colNames.length];
  }

  TermCountsTableModel(double numTerms, Map<String, Long> termCounts) {
    data = new Object[termCounts.size()][colNames.length];
    int i = 0;
    for (Map.Entry<String, Long> e : termCounts.entrySet()) {
      String term = e.getKey();
      Long count = e.getValue();
      data[i++] = new Object[]{ term, count, String.format("%.2f %%", count / numTerms * 100) };
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

  public Class<?> getColumnClass(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).type;
    }
    return Object.class;
  }

  @Override
  public Object getValueAt(int rowIndex, int colIndex) {
    return data[rowIndex][colIndex];
  }

}

class TopTermsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    RANK("Rank", 0, Integer.class),
    FREQ("Freq", 1, Integer.class),
    TEXT("Text", 2, String.class);

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


  TopTermsTableModel() {
    data = new Object[0][colNames.length];
  }

  TopTermsTableModel(List<TermStats> termStats, int numTerms) {
    int rows = Math.min(numTerms, termStats.size());
    data = new Object[rows][colNames.length];
    for (int i = 0; i < data.length; i++) {
      int rank = i + 1;
      int freq = termStats.get(i).getDocFreq();
      String termText = termStats.get(i).getDecodedTermText();
      data[i] = new Object[]{ rank, freq, termText };
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
  public Object getValueAt(int rowIndex, int colIndex) {
    return data[rowIndex][colIndex];
  }
}