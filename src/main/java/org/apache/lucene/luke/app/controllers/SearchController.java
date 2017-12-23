package org.apache.lucene.luke.app.controllers;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.app.controllers.dialog.ConfirmController;
import org.apache.lucene.luke.app.controllers.dialog.ExplanationController;
import org.apache.lucene.luke.app.controllers.dto.SearchResult;
import org.apache.lucene.luke.app.controllers.fragments.search.AnalyzerController;
import org.apache.lucene.luke.app.controllers.fragments.search.FieldValuesController;
import org.apache.lucene.luke.app.controllers.fragments.search.MLTController;
import org.apache.lucene.luke.app.controllers.fragments.search.QueryParserController;
import org.apache.lucene.luke.app.controllers.fragments.search.SimilarityController;
import org.apache.lucene.luke.app.controllers.fragments.search.SortController;
import org.apache.lucene.luke.app.util.DialogOpener;
import org.apache.lucene.luke.app.util.IntegerTextFormatter;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.search.MLTConfig;
import org.apache.lucene.luke.models.search.QueryParserConfig;
import org.apache.lucene.luke.models.search.Search;
import org.apache.lucene.luke.models.search.SearchResults;
import org.apache.lucene.luke.models.search.SimilarityConfig;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.util.MessageUtils;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class SearchController implements ChildController {

  private static final int DEFAULT_PAGE_SIZE = 10;

  @FXML
  private Accordion settings;

  @FXML
  private TitledPane parserPane;

  @FXML
  private TitledPane mltPane;

  @FXML
  private ScrollPane parser;

  @FXML
  private QueryParserController parserController;

  @FXML
  private ScrollPane analyzer;

  @FXML
  private AnalyzerController analyzerController;

  @FXML
  private ScrollPane similarity;

  @FXML
  private SimilarityController similarityController;

  @FXML
  private ScrollPane sort;

  @FXML
  private SortController sortController;

  @FXML
  private ScrollPane values;

  @FXML
  private FieldValuesController valuesController;

  @FXML
  private MLTController mltController;

  @FXML
  private CheckBox termQuery;

  @FXML
  private TextArea queryExpr;

  @FXML
  private TextArea parsedQuery;

  @FXML
  private Button parseBtn;

  @FXML
  private CheckBox rewrite;

  @FXML
  private Button searchBtn;

  @FXML
  private Button mltBtn;

  @FXML
  private TextField mltDoc;

  @FXML
  private Label totalHits;

  @FXML
  private Label start;

  @FXML
  private Label end;

  @FXML
  private Button prev;

  @FXML
  private Button next;

  @FXML
  private Button delAll;

  @FXML
  private TableView<SearchResult> resultsTable;

  @FXML
  private TableColumn<SearchResult, Integer> docIdColumn;

  @FXML
  private TableColumn<SearchResult, Float> scoreColumn;

  @FXML
  private TableColumn<SearchResult, String> valuesColumn;

  private ObservableList<SearchResult> resultList;

  @FXML
  private void initialize() throws LukeException {
    settings.setExpandedPane(parserPane);
    parseBtn.setOnAction(e -> runnableWrapper(this::execParse));
    searchBtn.setOnAction(e -> runnableWrapper(this::execSearch));
    termQuery.setOnAction(e -> toggleTermQuery());

    mltDoc.setTextFormatter(new IntegerTextFormatter(new IntegerStringConverter(), 0));
    mltBtn.setOnAction(e -> runnableWrapper(this::execMLTSearch));

    totalHits.setText("0");
    start.setText("0");
    end.setText("0");

    next.setDisable(true);
    next.setOnAction(e -> runnableWrapper(this::nextPage));

    prev.setDisable(true);
    prev.setOnAction(e -> runnableWrapper(this::prevPage));

    delAll.setDisable(true);
    delAll.setOnAction(e -> runnableWrapper(this::showDeleteConfirmDialog));

    // initialize results table
    docIdColumn.setCellValueFactory(new PropertyValueFactory<>("docId"));
    scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
    valuesColumn.setCellValueFactory(new PropertyValueFactory<>("values"));
    resultList = FXCollections.observableArrayList();
    resultsTable.setItems(resultList);
    resultsTable.setContextMenu(createResultTableMenu());
  }

  private void toggleTermQuery() {
    if (termQuery.isSelected()) {
      settings.setDisable(true);
      parseBtn.setDisable(true);
      rewrite.setDisable(true);
      mltBtn.setDisable(true);
      mltDoc.setDisable(true);
      parsedQuery.setText("");
    } else {
      settings.setDisable(false);
      parseBtn.setDisable(false);
      rewrite.setDisable(false);
      mltBtn.setDisable(false);
      mltDoc.setDisable(false);
    }
  }

  @Override
  public void onIndexOpen() throws LukeException {
    queryExpr.setText("*:*");
    parserController.populateFields();
    sortController.populateFields();
    valuesController.populateFields();
    mltController.populateFields();
  }

  @Override
  public void onClose() {
    queryExpr.setText("");
    parsedQuery.setText("");
    totalHits.setText("0");
    start.setText("0");
    end.setText("0");
    next.setDisable(true);
    prev.setDisable(true);
    delAll.setDisable(true);
    resultList.clear();
  }

  private void execParse() throws LukeException {
    Query query = parse(rewrite.isSelected());
    parsedQuery.setText(query.toString());
    parent.clearStatusMessage();
  }

  private void execSearch() throws LukeException {
    Query query;
    if (termQuery.isSelected()) {
      // term query
      if (Strings.isNullOrEmpty(queryExpr.getText())) {
        throw new LukeException("Query is not set.");
      }
      String[] tmp = queryExpr.getText().split(":");
      if (tmp.length < 2) {
        throw new LukeException(String.format("Invalid query [ %s ]", queryExpr.getText()));
      }
      query = new TermQuery(new Term(tmp[0].trim(), tmp[1].trim()));
    } else {
      query = parse(false);
    }
    SimilarityConfig simConfig = similarityController.getConfig();
    Sort sort = sortController.getSort();
    Set<String> fieldsToLoad = valuesController.getFieldsToLoad();
    resultList.clear();
    searchModel.search(query, simConfig, sort, fieldsToLoad, DEFAULT_PAGE_SIZE).ifPresent(this::populateResults);
  }

  private void execMLTSearch() throws LukeException {
    if (Strings.isNullOrEmpty(mltDoc.getText())) {
      throw new LukeException("Doc num is not set.");
    }
    int docNum = Integer.parseInt(mltDoc.getText());
    MLTConfig mltConfig = mltController.getMLTConfig();

    Query query = searchModel.mltQuery(docNum, mltConfig, curAnalyzer);
    Set<String> fieldsToLoad = valuesController.getFieldsToLoad();
    resultList.clear();
    searchModel.search(query, new SimilarityConfig(), fieldsToLoad, DEFAULT_PAGE_SIZE).ifPresent(this::populateResults);
  }

  private void nextPage() throws LukeException {
    resultList.clear();
    searchModel.nextPage().ifPresent(this::populateResults);
  }

  private void prevPage() throws LukeException {
    resultList.clear();
    searchModel.prevPage().ifPresent(this::populateResults);
  }

  private Query parse(boolean rewrite) throws LukeException {
    String expr = Strings.isNullOrEmpty(queryExpr.getText()) ? "*:*" : queryExpr.getText();
    String df = parserController.getDefField();
    QueryParserConfig config = parserController.getConfig();
    return searchModel.parseQuery(expr, df, curAnalyzer, config, rewrite);
  }

  private void populateResults(SearchResults res) {
    totalHits.setText(String.valueOf(res.getTotalHits()));
    if (res.getTotalHits() > 0) {
      start.setText(String.valueOf(res.getOffset() + 1));
      end.setText(String.valueOf(res.getOffset() + res.size()));
      resultList.addAll(res.getHits().stream().map(SearchResult::of).collect(Collectors.toList()));

      prev.setDisable(res.getOffset() == 0);
      next.setDisable(res.getTotalHits() <= res.getOffset() + res.size());

      if (!parent.isReadOnly() && parent.hasDirectoryReader()) {
        delAll.setDisable(false);
      }
    } else {
      start.setText("0");
      end.setText("0");
      prev.setDisable(true);
      next.setDisable(true);
      delAll.setDisable(true);
    }
  }

  private Stage confirmDialog;

  private void showDeleteConfirmDialog() throws Exception {
    confirmDialog = new DialogOpener<ConfirmController>(parent).show(
        confirmDialog,
        "Confirm Deletion",
        "/fxml/dialog/confirm.fxml",
        400, 200,
        (controller) -> {
          controller.setContent(MessageUtils.getLocalizedMessage("search.message.delete_confirm"));
          controller.setCallback(this::deleteAllDocs);
        },
        "/styles/confirm.css"
    );
  }

  private void deleteAllDocs() throws LukeException {
    Query query = searchModel.getCurrentQuery();
    if (query != null) {
      indexToolsModel.deleteDocuments(query);
      parent.onIndexReopen();
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("search.message.delete_success", query.toString()));
    }
    delAll.setDisable(true);
  }

  private Stage explanationDialog;

  private ContextMenu createResultTableMenu() {
    ContextMenu menu = new ContextMenu();
    MenuItem item1 = new MenuItem(MessageUtils.getLocalizedMessage("search.results.menu.explain"));
    item1.setOnAction(e -> runnableWrapper(() -> {
      SearchResult result = resultsTable.getSelectionModel().getSelectedItem();
      Explanation explanation = searchModel.explain(parse(false), result.getDocId());
      explanationDialog = new DialogOpener<ExplanationController>(parent).show(
          explanationDialog,
          "Explanation",
          "/fxml/dialog/explanation.fxml",
          600, 400,
          (controller) -> {
            controller.setDocNum(result.getDocId());
            controller.setExplanation(explanation);
          }
      );
    }));

    MenuItem item2 = new MenuItem(MessageUtils.getLocalizedMessage("search.results.menu.showdoc"));
    item2.setOnAction(e -> runnableWrapper(() -> {
      SearchResult result = resultsTable.getSelectionModel().getSelectedItem();
      parent.getDocumentsController().displayDoc(result.getDocId());
      parent.switchTab(LukeController.Tab.DOCUMENTS);
    }));

    menu.getItems().addAll(item1, item2);
    return menu;
  }

  private Search searchModel;

  private IndexTools indexToolsModel;

  private LukeController parent;

  private Analyzer curAnalyzer;

  @Override
  public void setParent(LukeController parent) {
    this.parent = parent;
    analyzerController.setParent(this, parent);
    mltController.setParent(this, parent);
  }

  @Inject
  public SearchController(Search searchModel, IndexTools indexToolsModel) {
    this.searchModel = searchModel;
    this.indexToolsModel = indexToolsModel;
  }

  // -------------------------------------------------
  // methods for interaction with other controllers
  // -------------------------------------------------

  void setCurrentAnalyzer(Analyzer analyzer) {
    this.curAnalyzer = analyzer;
    analyzerController.setCurrentAnalyzer(analyzer);
    mltController.setCurrentAnalyzer(analyzer);
  }

  void searchByTerm(@Nonnull String fieldName, @Nonnull String termText) throws LukeException {
    termQuery.selectedProperty().setValue(true);
    toggleTermQuery();
    queryExpr.setText(String.format("%s:%s", fieldName, termText));
    resultList.clear();
    execSearch();
  }

  void mltSearch(int docNum) throws LukeException {
    mltDoc.setText(String.valueOf(docNum));
    settings.setExpandedPane(mltPane);
    execMLTSearch();
  }

}
