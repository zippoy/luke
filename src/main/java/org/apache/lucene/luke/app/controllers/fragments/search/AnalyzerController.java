package org.apache.lucene.luke.app.controllers.fragments.search;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.app.controllers.SearchController;

import java.util.stream.Collectors;

public class AnalyzerController {

  private static final int LISTVIEW_ROW_HEIGHT = 25;

  @FXML
  private Label analyzerName;

  @FXML
  private Hyperlink change;

  @FXML
  private VBox analysisChain;

  @FXML
  private ListView<String> charFilters;

  private ObservableList<String> charFilterList;

  @FXML
  private TextField tokenizer;

  @FXML
  private ListView<String> tokenFilters;

  private ObservableList<String> tokenFilterList;

  @FXML
  private void initialize() {
    change.setOnAction(e -> parent.switchTab(LukeController.Tab.ANALYZER));

    analysisChain.setDisable(true);
    charFilterList = FXCollections.observableArrayList();
    charFilters.setItems(charFilterList);
    tokenFilterList = FXCollections.observableArrayList();
    tokenFilters.setItems(tokenFilterList);
  }

  private LukeController parent;

  private SearchController searchController;

  public void setParent(SearchController searchController, LukeController parent) {
    this.searchController = searchController;
    this.parent = parent;
  }

  public void setCurrentAnalyzer(Analyzer analyzer) {
    analyzerName.setText(analyzer.getClass().getSimpleName());
    charFilterList.clear();
    tokenizer.clear();
    tokenFilterList.clear();

    if (analyzer instanceof CustomAnalyzer) {
      CustomAnalyzer customAnalyzer = (CustomAnalyzer) analyzer;

      charFilterList.addAll(customAnalyzer.getCharFilterFactories().stream()
          .map(f -> f.getClass().getSimpleName()).collect(Collectors.toList()));
      tokenizer.setText(customAnalyzer.getTokenizerFactory().getClass().getSimpleName());
      tokenFilterList.clear();
      tokenFilterList.addAll(customAnalyzer.getTokenFilterFactories().stream()
          .map(f -> f.getClass().getSimpleName()).collect(Collectors.toList()));
      analysisChain.setDisable(false);
    } else {
      analysisChain.setDisable(true);
    }

    charFilters.setPrefHeight(Math.max(LISTVIEW_ROW_HEIGHT * charFilterList.size(), LISTVIEW_ROW_HEIGHT));
    tokenFilters.setPrefHeight(Math.max(LISTVIEW_ROW_HEIGHT * tokenFilterList.size(), LISTVIEW_ROW_HEIGHT));
  }

}
