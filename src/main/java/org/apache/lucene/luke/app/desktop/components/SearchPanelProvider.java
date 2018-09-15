package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.components.fragments.search.FieldValuesPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.MLTPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.QueryParserPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SortPaneProvider;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.search.Search;
import org.apache.lucene.luke.models.search.SearchFactory;
import org.apache.lucene.luke.models.tools.IndexToolsFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Map;

public class SearchPanelProvider implements Provider<JPanel> {

  private final SearchFactory searchFactory;

  private final IndexToolsFactory toolsFactory;

  private final IndexHandler indexHandler;

  private final QueryParserPaneProvider.QueryParserTabProxy queryParserTab;

  private final SortPaneProvider.SortTabProxy sortTab;

  private final FieldValuesPaneProvider.FieldValuesTabProxy fieldValuesTab;

  private final MLTPaneProvider.MLTTabProxy mltTab;

  private final JScrollPane qparser;

  private final JScrollPane analyzer;

  private final JScrollPane similarity;

  private final JScrollPane sort;

  private final JScrollPane values;

  private final JScrollPane mlt;

  private final JCheckBox termQueryCB = new JCheckBox();

  private final JTextArea queryStringTA = new JTextArea();

  private final JTextArea parsedQueryTA = new JTextArea();

  private final JCheckBox rewriteCB = new JCheckBox();

  private final JTextField mltDocTF = new JTextField();

  private final JLabel totalDocsLbl = new JLabel();

  private final JTable resultsTable = new JTable();

  class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      Search searchModel = searchFactory.newInstance(state.getIndexReader());
      queryParserTab.setSearchableFields(searchModel.getSearchableFieldNames());
      queryParserTab.setRangeSearchableFields(searchModel.getRangeSearchableFieldNames());
      sortTab.setSearchModel(searchModel);
      sortTab.setSortableFields(searchModel.getSortableFieldNames());
      fieldValuesTab.setFields(searchModel.getFieldNames());
      mltTab.setFields(searchModel.getFieldNames());
    }

    @Override
    public void closeIndex() {

    }

    private Observer() {}
  }

  @Inject
  public SearchPanelProvider(SearchFactory searchFactory,
                             IndexToolsFactory toolsFactory,
                             IndexHandler indexHandler,
                             QueryParserPaneProvider.QueryParserTabProxy queryParserTab,
                             SortPaneProvider.SortTabProxy sortTab,
                             FieldValuesPaneProvider.FieldValuesTabProxy fieldValuesTab,
                             MLTPaneProvider.MLTTabProxy mltTab,
                             @Named("search_qparser") JScrollPane qparser,
                             @Named("search_analyzer") JScrollPane analyzer,
                             @Named("search_similarity") JScrollPane similarity,
                             @Named("search_sort") JScrollPane sort,
                             @Named("search_values") JScrollPane values,
                             @Named("search_mlt") JScrollPane mlt) {
    this.searchFactory = searchFactory;
    this.toolsFactory = toolsFactory;
    this.indexHandler = indexHandler;
    this.queryParserTab = queryParserTab;
    this.sortTab = sortTab;
    this.fieldValuesTab = fieldValuesTab;
    this.mltTab = mltTab;
    this.qparser = qparser;
    this.analyzer = analyzer;
    this.similarity = similarity;
    this.sort = sort;
    this.values = values;
    this.mlt = mlt;

    indexHandler.addObserver(new Observer());
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(350);
    panel.add(splitPane);

    return panel;
  }

  private JSplitPane createUpperPanel() {
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createQuerySettingsPane(), createQueryPane());
    splitPane.setDividerLocation(570);
    return splitPane;
  }

  private JPanel createQuerySettingsPane() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("search.label.settings"));
    panel.add(label, BorderLayout.PAGE_START);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Query Parser", qparser);
    tabbedPane.addTab("Analyzer", analyzer);
    tabbedPane.addTab("Similarity", similarity);
    tabbedPane.addTab("Sort", sort);
    tabbedPane.addTab("Field Values", values);
    tabbedPane.addTab("More Like This", mlt);
    panel.add(tabbedPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createQueryPane() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;

    JLabel labelQE = new JLabel(MessageUtils.getLocalizedMessage("search.label.expression"));
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.weightx = 0.5;
    c.insets = new Insets(2, 0, 2, 2);
    panel.add(labelQE, c);

    termQueryCB.setText(MessageUtils.getLocalizedMessage("search.checkbox.term"));
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.2;
    c.insets = new Insets(2, 0, 2, 2);
    panel.add(termQueryCB, c);

    queryStringTA.setRows(4);
    queryStringTA.setLineWrap(true);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 3;
    c.weightx = 0.0;
    c.insets = new Insets(2, 0, 2, 2);
    panel.add(new JScrollPane(queryStringTA, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);

    JLabel labelPQ = new JLabel(MessageUtils.getLocalizedMessage("search.label.parsed"));
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 3;
    c.weightx = 0.0;
    c.insets = new Insets(8, 0, 2, 2);
    panel.add(labelPQ, c);

    parsedQueryTA.setRows(4);
    parsedQueryTA.setLineWrap(true);
    parsedQueryTA.setEditable(false);
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 3;
    c.weightx = 0.0;
    c.insets = new Insets(2, 0, 2, 2);
    panel.add(new JScrollPane(parsedQueryTA), c);

    JButton parseBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.parse"),
        ImageUtils.createImageIcon("/img/icon_flowchart_alt.png", 20, 20));
    parseBtn.setFont(new Font(parseBtn.getFont().getFontName(), Font.PLAIN, 15));
    parseBtn.setMargin(new Insets(2, 2, 2, 2));
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.weightx = 0.2;
    c.insets = new Insets(5, 0, 0, 2);
    panel.add(parseBtn, c);

    rewriteCB.setText(MessageUtils.getLocalizedMessage("search.checkbox.rewrite"));
    c.gridx = 1;
    c.gridy = 4;
    c.gridwidth = 2;
    c.weightx = 0.2;
    c.insets = new Insets(5, 0, 0, 2);
    panel.add(rewriteCB, c);

    JButton searchBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.search"),
        ImageUtils.createImageIcon("/img/icon_search2.png", 20, 20));
    searchBtn.setFont(new Font(searchBtn.getFont().getFontName(), Font.PLAIN, 15));
    searchBtn.setMargin(new Insets(2, 2, 2, 2));
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.insets = new Insets(5, 0, 5, 0);
    panel.add(searchBtn, c);

    JButton mltBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.mlt"),
        ImageUtils.createImageIcon("/img/icon_heart_alt.png", 20, 20));
    mltBtn.setFont(new Font(mltBtn.getFont().getFontName(), Font.PLAIN, 15));
    mltBtn.setMargin(new Insets(2, 2, 2, 2));
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.weightx = 0.3;
    c.insets = new Insets(10, 0, 2, 0);
    panel.add(mltBtn, c);

    JPanel docNo = new JPanel(new FlowLayout());
    JLabel docNoLabel = new JLabel("with doc #");
    docNo.add(docNoLabel);
    mltDocTF.setColumns(3);
    docNo.add(mltDocTF);
    c.gridx = 1;
    c.gridy = 6;
    c.gridwidth = 2;
    c.weightx = 0.3;
    c.insets = new Insets(8, 0, 0, 2);
    panel.add(docNo, c);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    panel.add(createSearchResultsHeaderPane(), BorderLayout.PAGE_START);
    panel.add(createSearchResultsTablePane(), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSearchResultsHeaderPane() {
    JPanel panel = new JPanel(new GridLayout(1, 2));

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("search.label.results"),
        ImageUtils.createImageIcon("/img/icon_table.png", 20, 20),
        JLabel.LEFT);
    panel.add(label);

    JPanel resultsInfo = new JPanel(new FlowLayout());

    JLabel totalLabel = new JLabel(MessageUtils.getLocalizedMessage("search.label.total"));
    resultsInfo.add(totalLabel);

    totalDocsLbl.setText("?");
    resultsInfo.add(totalDocsLbl);

    JButton prevBtn = new JButton(ImageUtils.createImageIcon("/img/arrow_triangle-left.png", 20, 20));
    prevBtn.setMargin(new Insets(5, 5,5, 5));
    resultsInfo.add(prevBtn);

    JLabel start = new JLabel("");
    resultsInfo.add(start);

    resultsInfo.add(new JLabel(" ~ "));

    JLabel end = new JLabel("");
    resultsInfo.add(end);

    JButton nextBtn = new JButton(ImageUtils.createImageIcon("/img/arrow_triangle-right.png", 20, 20));
    nextBtn.setMargin(new Insets(5, 5, 5, 5));
    resultsInfo.add(nextBtn);

    JSeparator sep = new JSeparator(JSeparator.VERTICAL);
    sep.setPreferredSize(new Dimension(5, 1));
    resultsInfo.add(sep);

    JButton delBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.del_all"),
        ImageUtils.createImageIcon("/img/icon_trash.png", 20, 20));
    delBtn.setFont(new Font(delBtn.getFont().getFontName(), Font.PLAIN, 15));
    delBtn.setMargin(new Insets(5, 5, 5, 5));
    resultsInfo.add(delBtn);

    panel.add(resultsInfo, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSearchResultsTablePane() {
    JPanel panel = new JPanel(new GridLayout(1, 1));

    TableUtil.setupTable(resultsTable, ListSelectionModel.SINGLE_SELECTION, new SearchResultsTableModel(), null, 50, 100);
    JScrollPane scrollPane = new JScrollPane(resultsTable);
    panel.add(scrollPane);

    return panel;
  }

}

class SearchResultsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    DOCID("Doc ID", 0, Integer.class),
    SCORE("Score", 1, Float.class),
    VALUE("Field Values", 2, String.class);

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

  SearchResultsTableModel() {
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

}