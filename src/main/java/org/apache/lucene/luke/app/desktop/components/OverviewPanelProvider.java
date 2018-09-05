package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.components.util.StyleConstants;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.listeners.OverviewPanelListeners;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.overview.Overview;
import org.apache.lucene.luke.models.overview.OverviewFactory;
import org.apache.lucene.luke.models.overview.TermCountsOrder;
import org.apache.lucene.luke.models.overview.TermStats;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OverviewPanelProvider implements Provider<JPanel> {

  private static final int GRIDX_DESC = 0;
  private static final int GRIDX_VAL = 1;
  private static final double WEIGHTX_DESC = 0.1;
  private static final double WEIGHTX_VAL = 0.9;

  private final OverviewFactory overviewFactory;

  private final OverviewPanelListeners listeners;

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

  public class Controller {

    public Optional<String> getCurrentTermCountsField() {
      int row = termCountsTable.getSelectedRow();
      if (row < 0) {
        return Optional.empty();
      }
      if (row >= termCountsTable.getRowCount()) {
        return Optional.empty();
      }
      return Optional.of((String)termCountsTable.getModel().getValueAt(row, 0));
    }

    public String getSelectedField() {
      return selectedField.getText();
    }

    public void setSelectedField(String field) {
      selectedField.setText(field);
    }

    public void enableShowTopTermBtn() {
      showTopTermsBtn.setEnabled(true);
    }

    public Integer getNumTopTerms() {
      return (Integer)numTopTermsSpnr.getModel().getValue();
    }

    public void updateTopTerms(List<TermStats> termStats, int numTerms) {
      topTermsTable.setModel(new TopTermsTableModel(termStats, numTerms));
      topTermsTable.getColumnModel().getColumn(0).setMaxWidth(50);
      topTermsTable.getColumnModel().getColumn(1).setMaxWidth(80);
      messageBroker.clearStatusMessage();
    }

    public String getSelectedTerm() {
      int row = topTermsTable.getSelectedRow();
      if (row < 0) {
        throw new IllegalStateException("Term is not selected.");
      }
      return (String)topTermsTable.getModel().getValueAt(row, 2);
    }

    private Controller() {}

  }

  public class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      Overview overviewModel = overviewFactory.newInstance(state.getIndexReader(), state.getIndexPath());
      listeners.setOverviewModel(overviewModel);

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
      termCountsTable.getColumnModel().getColumn(0).setMaxWidth(120);
      termCountsTable.getColumnModel().getColumn(1).setMaxWidth(100);
      DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
      rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
      termCountsTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

      // top terms table
      topTermsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      topTermsTable.getColumnModel().getColumn(0).setMaxWidth(50);
      topTermsTable.getColumnModel().getColumn(1).setMaxWidth(80);
      topTermsTable.getColumnModel().setColumnMargin(StyleConstants.TABLE_COLUMN_MARGIN_DEFAULT);
      topTermsTable.addMouseListener(listeners.getTopTermsTableListener());
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
  public OverviewPanelProvider(OverviewFactory overviewFactory, MessageBroker messageBroker, IndexHandler indexHandler, TabbedPaneProvider.TabSwitcherProxy tabSwitcher) {
    this.overviewFactory = overviewFactory;
    this.messageBroker = messageBroker;
    this.listeners = new OverviewPanelListeners(new Controller(), tabSwitcher);

    indexHandler.addObserver(new Observer());
  }

  @Override
  public JPanel get() {
    panel.setLayout(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(0.4);
    panel.add(splitPane);
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

    TableUtil.setupTable(termCountsTable, ListSelectionModel.SINGLE_SELECTION, new TermCountsTableModel(), listeners.getTermCountsTableListener());
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

    selectedField.setColumns(15);
    selectedField.setEditable(false);
    JPanel innerPanel2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    innerPanel2.add(selectedField);
    innerPanel.add(innerPanel2);

    showTopTermsBtn.setText(MessageUtils.getLocalizedMessage("overview.button.show_terms"));
    showTopTermsBtn.setPreferredSize(new Dimension(150, 40));
    showTopTermsBtn.setFont(StyleConstants.BUTTON_FONT_LARGE);
    showTopTermsBtn.addActionListener(listeners.getShowTopTermsBtnListener());
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

    TableUtil.setupTable(topTermsTable, ListSelectionModel.SINGLE_SELECTION, new TopTermsTableModel(), listeners.getTermCountsTableListener());
    JScrollPane scrollPane = new JScrollPane(topTermsTable);
    termsPanel.add(scrollPane, BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, selectedPanel, termsPanel);
    splitPane.setDividerLocation(160);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    panel.add(splitPane);

    return panel;
  }

}

class TermCountsTableModel extends AbstractTableModel {

  private final String[] colNames = new String[]{"Name", "Term Count", "%"};

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
    return colNames[colIndex];
  }

  public Class<?> getColumnClass(int colIndex) {
    switch (colIndex) {
      case 0:
        return String.class;
      case 1:
        return Long.class;
      case 2:
        return String.class;
      default:
        return Object.class;
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int colIndex) {
    return data[rowIndex][colIndex];
  }

}

class TopTermsTableModel extends AbstractTableModel {

  private final String[] colNames = new String[]{"Rank", "Freq", "Text"};

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
    return colNames[colIndex];
  }

  @Override
  public Class<?> getColumnClass(int colIndex) {
    switch (colIndex) {
      case 0:
        return Integer.class;
      case 1:
        return Integer.class;
      case 2:
        return String.class;
      default:
        return Object.class;
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int colIndex) {
    return data[rowIndex][colIndex];
  }
}