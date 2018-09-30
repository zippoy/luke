package org.apache.lucene.luke.app.desktop.components;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.components.dialog.ConfirmDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.search.ExplainDialogProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.FieldValuesPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.MLTPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.QueryParserPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SimilarityPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SortPaneProvider;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.search.MLTConfig;
import org.apache.lucene.luke.models.search.QueryParserConfig;
import org.apache.lucene.luke.models.search.Search;
import org.apache.lucene.luke.models.search.SearchFactory;
import org.apache.lucene.luke.models.search.SearchResults;
import org.apache.lucene.luke.models.search.SimilarityConfig;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.models.tools.IndexToolsFactory;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchPanelProvider implements Provider<JPanel> {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final SearchFactory searchFactory;

  private final IndexToolsFactory toolsFactory;

  private final IndexHandler indexHandler;

  private final MessageBroker messageBroker;

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  private final ComponentOperatorRegistry operatorRegistry;

  private final ConfirmDialogFactory confirmDialogFactory;

  private final ExplainDialogProvider explainDialogProvider;

  private final JTabbedPane tabbedPane = new JTabbedPane();

  private final JScrollPane qparser;

  private final JScrollPane analyzer;

  private final JScrollPane similarity;

  private final JScrollPane sort;

  private final JScrollPane values;

  private final JScrollPane mlt;

  private final JCheckBox termQueryCB = new JCheckBox();

  private final JTextArea queryStringTA = new JTextArea();

  private final JTextArea parsedQueryTA = new JTextArea();

  private final JButton parseBtn = new JButton();

  private final JCheckBox rewriteCB = new JCheckBox();

  private final JButton searchBtn = new JButton();

  private final JButton mltBtn = new JButton();

  private final JFormattedTextField mltDocFTF = new JFormattedTextField();

  private final JLabel totalHitsLbl = new JLabel();

  private final JLabel startLbl = new JLabel();

  private final JLabel endLbl = new JLabel();

  private final JButton prevBtn = new JButton();

  private final JButton nextBtn = new JButton();

  private final JButton delBtn = new JButton();

  private final JTable resultsTable = new JTable();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private Search searchModel;

  private IndexTools toolsModel;

  class ListenerFunctions {

    void toggleTermQuery(ActionEvent e) {
      if (termQueryCB.isSelected()) {
        enableTermQuery();
      } else {
        disableTermQuery();
      }
    }

    private void enableTermQuery() {
      tabbedPane.setEnabledAt(Tab.QPARSER.index(), false);
      tabbedPane.setEnabledAt(Tab.ANALYZER.index(), false);
      tabbedPane.setEnabledAt(Tab.SIMILARITY.index(), false);
      tabbedPane.setEnabledAt(Tab.MLT.index(), false);
      if (tabbedPane.getSelectedIndex() == Tab.QPARSER.index() ||
          tabbedPane.getSelectedIndex() == Tab.ANALYZER.index() ||
          tabbedPane.getSelectedIndex() == Tab.SIMILARITY.index() ||
          tabbedPane.getSelectedIndex() == Tab.MLT.index()) {
        tabbedPane.setSelectedIndex(Tab.SORT.index());
      }
      parseBtn.setEnabled(false);
      rewriteCB.setEnabled(false);
      mltBtn.setEnabled(false);
      mltDocFTF.setEnabled(false);
    }

    private void disableTermQuery() {
      tabbedPane.setEnabledAt(Tab.QPARSER.index(), true);
      tabbedPane.setEnabledAt(Tab.ANALYZER.index(), true);
      tabbedPane.setEnabledAt(Tab.SIMILARITY.index(), true);
      tabbedPane.setEnabledAt(Tab.MLT.index(), true);
      parseBtn.setEnabled(true);
      rewriteCB.setEnabled(true);
      mltBtn.setEnabled(true);
      mltDocFTF.setEnabled(true);
    }

    void execParse(ActionEvent e) {
      Query query = parse(rewriteCB.isSelected());
      parsedQueryTA.setText(query.toString());
      messageBroker.clearStatusMessage();
    }

    void execSearch(ActionEvent e) {
      doSearch();
    }

    private void doSearch() {
      Query query;
      if (termQueryCB.isSelected()) {
        // term query
        if (Strings.isNullOrEmpty(queryStringTA.getText())) {
          throw new LukeException("Query is not set.");
        }
        String[] tmp = queryStringTA.getText().split(":");
        if (tmp.length < 2) {
          throw new LukeException(String.format("Invalid query [ %s ]", queryStringTA.getText()));
        }
        query = new TermQuery(new Term(tmp[0].trim(), tmp[1].trim()));
      } else {
        query = parse(false);
      }
      SimilarityConfig simConfig = operatorRegistry.get(SimilarityPaneProvider.SimilarityTabOperator.class)
          .map(SimilarityPaneProvider.SimilarityTabOperator::getConfig)
          .orElse(new SimilarityConfig.Builder().build());
      Sort sort = operatorRegistry.get(SortPaneProvider.SortTabOperator.class)
          .map(SortPaneProvider.SortTabOperator::getSort)
          .orElse(null);
      Set<String> fieldsToLoad = operatorRegistry.get(FieldValuesPaneProvider.FieldValuesTabOperator.class)
          .map(FieldValuesPaneProvider.FieldValuesTabOperator::getFieldsToLoad)
          .orElse(Collections.emptySet());
      SearchResults results = searchModel.search(query, simConfig, sort, fieldsToLoad, DEFAULT_PAGE_SIZE);

      TableUtil.setupTable(resultsTable, ListSelectionModel.SINGLE_SELECTION, new SearchResultsTableModel(), null, 50, 100);
      populateResults(results);

      messageBroker.clearStatusMessage();
    }

    void nextPage(ActionEvent e) {
      searchModel.nextPage().ifPresent(this::populateResults);
      messageBroker.clearStatusMessage();
    }

    void prevPage(ActionEvent e) {
      searchModel.prevPage().ifPresent(this::populateResults);
      messageBroker.clearStatusMessage();
    }

    void execMLTSearch(ActionEvent e) {
      doMLTSearch();
    }

    void doMLTSearch() {
      if (Objects.isNull(mltDocFTF.getValue())) {
        throw new LukeException("Doc num is not set.");
      }
      int docNum = (int)mltDocFTF.getValue();
      MLTConfig mltConfig = operatorRegistry.get(MLTPaneProvider.MLTTabOperator.class)
          .map(MLTPaneProvider.MLTTabOperator::getConfig)
          .orElse(new MLTConfig.Builder().build());
      Analyzer analyzer = operatorRegistry.get(AnalysisPanelProvider.AnalysisPanelOperator.class)
          .map(AnalysisPanelProvider.AnalysisPanelOperator::getCurrentAnalyzer)
          .orElse(new StandardAnalyzer());
      Query query = searchModel.mltQuery(docNum, mltConfig, analyzer);
      Set<String> fieldsToLoad = operatorRegistry.get(FieldValuesPaneProvider.FieldValuesTabOperator.class)
          .map(FieldValuesPaneProvider.FieldValuesTabOperator::getFieldsToLoad)
          .orElse(Collections.emptySet());
      SearchResults results = searchModel.search(query, new SimilarityConfig.Builder().build(), fieldsToLoad, DEFAULT_PAGE_SIZE);

      TableUtil.setupTable(resultsTable, ListSelectionModel.SINGLE_SELECTION, new SearchResultsTableModel(), null, 50, 100);
      populateResults(results);

      messageBroker.clearStatusMessage();
    }

    private Query parse(boolean rewrite) {
      String expr = Strings.isNullOrEmpty(queryStringTA.getText()) ? "*:*" : queryStringTA.getText();
      String df = operatorRegistry.get(QueryParserPaneProvider.QueryParserTabOperator.class)
          .map(QueryParserPaneProvider.QueryParserTabOperator::getDefaultField)
          .orElse("");
      QueryParserConfig config = operatorRegistry.get(QueryParserPaneProvider.QueryParserTabOperator.class)
          .map(QueryParserPaneProvider.QueryParserTabOperator::getConfig)
          .orElse(new QueryParserConfig.Builder().build());
      Analyzer analyzer = operatorRegistry.get(AnalysisPanelProvider.AnalysisPanelOperator.class)
          .map(AnalysisPanelProvider.AnalysisPanelOperator::getCurrentAnalyzer)
          .orElse(new StandardAnalyzer());
      return searchModel.parseQuery(expr, df, analyzer, config, rewrite);
    }

    private void populateResults(SearchResults res) {
      totalHitsLbl.setText(String.valueOf(res.getTotalHits()));
      if (res.getTotalHits() > 0) {
        startLbl.setText(String.valueOf(res.getOffset() + 1));
        endLbl.setText(String.valueOf(res.getOffset() + res.size()));

        prevBtn.setEnabled(res.getOffset() > 0);
        nextBtn.setEnabled(res.getTotalHits() > res.getOffset() + res.size());

        if (!indexHandler.getState().readOnly() && indexHandler.getState().hasDirectoryReader()) {
          delBtn.setEnabled(true);
        }

        resultsTable.setModel(new SearchResultsTableModel(res));
        resultsTable.getColumnModel().getColumn(SearchResultsTableModel.Column.DOCID.getIndex()).setPreferredWidth(50);
        resultsTable.getColumnModel().getColumn(SearchResultsTableModel.Column.SCORE.getIndex()).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(SearchResultsTableModel.Column.VALUE.getIndex()).setPreferredWidth(800);
      } else {
        startLbl.setText("0");
        endLbl.setText("0");
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        delBtn.setEnabled(false);
      }
    }

    void confirmDeletion(ActionEvent e) {
      new DialogOpener<>(confirmDialogFactory).open("Confirm Deletion", 400, 200, (factory) -> {
        factory.setMessage(MessageUtils.getLocalizedMessage("search.message.delete_confirm"));
        factory.setCallback(listeners::deleteDocs);
      });
    }

    private void deleteDocs() {
      Query query = searchModel.getCurrentQuery();
      if (query != null) {
        toolsModel.deleteDocuments(query);
        indexHandler.reOpen();
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("search.message.delete_success", query.toString()));
      }
      delBtn.setEnabled(false);
    }

    void showContextMenuInResultsTable(MouseEvent e) {
      if (e.getClickCount() == 2 && !e.isConsumed()) {
        createResultsContextMenuPopup().show(e.getComponent(), e.getX(), e.getY());
      }
    }

    private JPopupMenu createResultsContextMenuPopup() {
      JPopupMenu popup = new JPopupMenu();

      // show explanation
      JMenuItem item1 = new JMenuItem(MessageUtils.getLocalizedMessage("search.results.menu.explain"));
      item1.addActionListener(e -> {
        int docid = (int)resultsTable.getModel().getValueAt(resultsTable.getSelectedRow(), SearchResultsTableModel.Column.DOCID.getIndex());
        Explanation explanation = searchModel.explain(parse(false), docid);
        new DialogOpener<>(explainDialogProvider).open("Explanation", 600, 400,
            (factory) -> {
              factory.setDocid(docid);
              factory.setExplanation(explanation);
            });
      });
      popup.add(item1);

      // show all fields
      JMenuItem item2 = new JMenuItem(MessageUtils.getLocalizedMessage("search.results.menu.showdoc"));
      item2.addActionListener(e -> {
        int docid = (int)resultsTable.getModel().getValueAt(resultsTable.getSelectedRow(), SearchResultsTableModel.Column.DOCID.getIndex());
        operatorRegistry.get(DocumentsPanelProvider.DocumentsTabOperator.class).ifPresent(operator -> operator.displayDoc(docid));
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.DOCUMENTS);
      });
      popup.add(item2);

      return popup;
    }

  }

  class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      searchModel = searchFactory.newInstance(state.getIndexReader());
      toolsModel = toolsFactory.newInstance(state.getIndexReader(), state.useCompound(), state.keepAllCommits());
      operatorRegistry.get(QueryParserPaneProvider.QueryParserTabOperator.class).ifPresent(operator -> {
        operator.setSearchableFields(searchModel.getSearchableFieldNames());
        operator.setRangeSearchableFields(searchModel.getRangeSearchableFieldNames());
      });
      operatorRegistry.get(SortPaneProvider.SortTabOperator.class).ifPresent(operator -> {
        operator.setSearchModel(searchModel);
        operator.setSortableFields(searchModel.getSortableFieldNames());
      });
      operatorRegistry.get(FieldValuesPaneProvider.FieldValuesTabOperator.class).ifPresent(operator -> {
        operator.setFields(searchModel.getFieldNames());
      });
      operatorRegistry.get(MLTPaneProvider.MLTTabOperator.class).ifPresent(operator -> {
        operator.setFields(searchModel.getFieldNames());
      });

      queryStringTA.setText("*:*");
      parsedQueryTA.setText("");
      parseBtn.setEnabled(true);
      searchBtn.setEnabled(true);
      mltBtn.setEnabled(true);
    }

    @Override
    public void closeIndex() {
      searchModel = null;
      toolsModel = null;

      queryStringTA.setText("");
      parsedQueryTA.setText("");
      parseBtn.setEnabled(false);
      searchBtn.setEnabled(false);
      mltBtn.setEnabled(false);
      totalHitsLbl.setText("0");
      startLbl.setText("0");
      endLbl.setText("0");
      nextBtn.setEnabled(false);
      prevBtn.setEnabled(false);
      delBtn.setEnabled(false);
      TableUtil.setupTable(resultsTable, ListSelectionModel.SINGLE_SELECTION, new SearchResultsTableModel(), null, 50, 100);
    }

    private Observer() {}
  }

  class SearchTabOperatorImpl implements SearchTabOperator {

    @Override
    public void searchByTerm(String field, String term) {
      termQueryCB.setSelected(true);
      listeners.enableTermQuery();
      queryStringTA.setText(String.format("%s:%s", field, term));
      listeners.doSearch();
    }

    @Override
    public void mltSearch(int docNum) {
      mltDocFTF.setValue(docNum);
      listeners.doMLTSearch();
      tabbedPane.setSelectedIndex(Tab.MLT.index());
    }
  }

  @Inject
  public SearchPanelProvider(SearchFactory searchFactory,
                             IndexToolsFactory toolsFactory,
                             IndexHandler indexHandler,
                             MessageBroker messageBroker,
                             TabbedPaneProvider.TabSwitcherProxy tabSwitcher,
                             ComponentOperatorRegistry operatorRegistry,
                             ConfirmDialogFactory confirmDialogFactory,
                             ExplainDialogProvider explainDialogProvider,
                             @Named("search_qparser") JScrollPane qparser,
                             @Named("search_analyzer") JScrollPane analyzer,
                             @Named("search_similarity") JScrollPane similarity,
                             @Named("search_sort") JScrollPane sort,
                             @Named("search_values") JScrollPane values,
                             @Named("search_mlt") JScrollPane mlt) {
    this.searchFactory = searchFactory;
    this.toolsFactory = toolsFactory;
    this.indexHandler = indexHandler;
    this.messageBroker = messageBroker;
    this.tabSwitcher = tabSwitcher;
    this.operatorRegistry = operatorRegistry;
    this.confirmDialogFactory = confirmDialogFactory;
    this.explainDialogProvider = explainDialogProvider;
    this.qparser = qparser;
    this.analyzer = analyzer;
    this.similarity = similarity;
    this.sort = sort;
    this.values = values;
    this.mlt = mlt;

    indexHandler.addObserver(new Observer());
    operatorRegistry.register(SearchTabOperator.class, new SearchTabOperatorImpl());
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
    termQueryCB.addActionListener(listeners::toggleTermQuery);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.2;
    c.insets = new Insets(2, 0, 2, 2);
    panel.add(termQueryCB, c);

    queryStringTA.setRows(4);
    queryStringTA.setLineWrap(true);
    queryStringTA.setText("*:*");
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

    parseBtn.setText(MessageUtils.getLocalizedMessage("search.button.parse"));
    parseBtn.setIcon(ImageUtils.createImageIcon("/img/icon_flowchart_alt.png", 20, 20));
    parseBtn.setFont(new Font(parseBtn.getFont().getFontName(), Font.PLAIN, 15));
    parseBtn.setMargin(new Insets(2, 2, 2, 2));
    parseBtn.addActionListener(listeners::execParse);
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

    searchBtn.setText(MessageUtils.getLocalizedMessage("search.button.search"));
    searchBtn.setIcon(ImageUtils.createImageIcon("/img/icon_search2.png", 20, 20));
    searchBtn.setFont(new Font(searchBtn.getFont().getFontName(), Font.PLAIN, 15));
    searchBtn.setMargin(new Insets(2, 2, 2, 2));
    searchBtn.addActionListener(listeners::execSearch);
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.insets = new Insets(5, 0, 5, 0);
    panel.add(searchBtn, c);

    mltBtn.setText(MessageUtils.getLocalizedMessage("search.button.mlt"));
    mltBtn.setIcon(ImageUtils.createImageIcon("/img/icon_heart_alt.png", 20, 20));
    mltBtn.setFont(new Font(mltBtn.getFont().getFontName(), Font.PLAIN, 15));
    mltBtn.setMargin(new Insets(2, 2, 2, 2));
    mltBtn.addActionListener(listeners::execMLTSearch);
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.weightx = 0.3;
    c.insets = new Insets(10, 0, 2, 0);
    panel.add(mltBtn, c);

    JPanel docNo = new JPanel(new FlowLayout());
    JLabel docNoLabel = new JLabel("with doc #");
    docNo.add(docNoLabel);
    mltDocFTF.setColumns(3);
    mltDocFTF.setValue(0);
    docNo.add(mltDocFTF);
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
    label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    panel.add(label);

    JPanel resultsInfo = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    JLabel totalLabel = new JLabel(MessageUtils.getLocalizedMessage("search.label.total"));
    resultsInfo.add(totalLabel);

    totalHitsLbl.setText("?");
    resultsInfo.add(totalHitsLbl);

    prevBtn.setIcon(ImageUtils.createImageIcon("/img/arrow_triangle-left.png", 20, 20));
    prevBtn.setMargin(new Insets(3, 3,3, 3));
    prevBtn.setEnabled(false);
    prevBtn.addActionListener(listeners::prevPage);
    resultsInfo.add(prevBtn);

    startLbl.setText("0");
    resultsInfo.add(startLbl);

    resultsInfo.add(new JLabel(" ~ "));

    endLbl.setText("0");
    resultsInfo.add(endLbl);

    nextBtn.setIcon(ImageUtils.createImageIcon("/img/arrow_triangle-right.png", 20, 20));
    nextBtn.setMargin(new Insets(3, 3, 3, 3));
    nextBtn.setEnabled(false);
    nextBtn.addActionListener(listeners::nextPage);
    resultsInfo.add(nextBtn);

    JSeparator sep = new JSeparator(JSeparator.VERTICAL);
    sep.setPreferredSize(new Dimension(5, 1));
    resultsInfo.add(sep);

    delBtn.setText(MessageUtils.getLocalizedMessage("search.button.del_all"));
    delBtn.setIcon(ImageUtils.createImageIcon("/img/icon_trash.png", 20, 20));
    delBtn.setMargin(new Insets(3, 3, 3, 3));
    delBtn.setEnabled(false);
    delBtn.addActionListener(listeners::confirmDeletion);
    resultsInfo.add(delBtn);

    panel.add(resultsInfo, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSearchResultsTablePane() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel note = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
    note.add(new JLabel(MessageUtils.getLocalizedMessage("search.label.results.note")));
    panel.add(note, BorderLayout.PAGE_START);

    MouseListener mouseListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        listeners.showContextMenuInResultsTable(e);
      }
    };
    TableUtil.setupTable(resultsTable, ListSelectionModel.SINGLE_SELECTION, new SearchResultsTableModel(), mouseListener, 50, 100);
    JScrollPane scrollPane = new JScrollPane(resultsTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  public interface SearchTabOperator extends ComponentOperatorRegistry.ComponentOperator {
    void searchByTerm(String field, String term);
    void mltSearch(int docNum);
  }

  public enum Tab {
    QPARSER(0), ANALYZER(1), SIMILARITY(2), SORT(3), VALUES(4), MLT(5);

    private int tabIdx;

    Tab(int tabIdx) {
      this.tabIdx = tabIdx;
    }

    int index() {
      return tabIdx;
    }
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

  SearchResultsTableModel(SearchResults results) {
    this.data = new Object[results.size()][colNames.length];
    for (int i = 0; i < results.size(); i++) {
      SearchResults.Doc doc = results.getHits().get(i);
      data[i][Column.DOCID.getIndex()] = doc.getDocId();
      if (!Float.isNaN(doc.getScore())) {
        data[i][Column.SCORE.getIndex()] = doc.getScore();
      } else {
        data[i][Column.SCORE.getIndex()] = 1.0f;
      }
      List<String> concatValues = doc.getFieldValues().entrySet().stream().map(e -> {
        String v = String.join(",", Arrays.asList(e.getValue()));
        return e.getKey() + "=" + v + ";";
      }).collect(Collectors.toList());
      data[i][Column.VALUE.getIndex()] = String.join(" ", concatValues);
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

}