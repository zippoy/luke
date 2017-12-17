package org.apache.lucene.luke.app.controllers.fragments.search;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.IntegerStringConverter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.app.controllers.SearchController;
import org.apache.lucene.luke.app.controllers.dto.SelectedField;
import org.apache.lucene.luke.app.util.IntegerTextFormatter;
import org.apache.lucene.luke.models.search.MLTConfig;
import org.apache.lucene.luke.models.search.Search;

import java.util.stream.Collectors;


public class MLTController {

  @FXML
  private TextField maxDocFreq;

  @FXML
  private TextField minDocFreq;

  @FXML
  private TextField minTermFreq;

  @FXML
  private Label analyzerName;

  @FXML
  private Hyperlink change;

  @FXML
  private CheckBox selectAll;

  @FXML
  private TableView<SelectedField> fieldsTable;

  @FXML
  private TableColumn<SelectedField, Boolean> selectColumn;

  @FXML
  private TableColumn<SelectedField, String> fieldColumn;

  private ObservableList<SelectedField> fieldList;

  @FXML
  private void initialize() {
    this.config = new MLTConfig();

    maxDocFreq.setTextFormatter(new IntegerTextFormatter(new IntegerStringConverter(), config.getMaxDocFreq()));
    minDocFreq.setTextFormatter(new IntegerTextFormatter(new IntegerStringConverter(), config.getMinDocFreq()));
    minTermFreq.setTextFormatter(new IntegerTextFormatter(new IntegerStringConverter(), config.getMinTermFreq()));

    selectAll.setSelected(true);
    selectAll.setOnAction(e ->
        fieldList.forEach(f -> f.setSelected(selectAll.isSelected()))
    );

    change.setOnAction(e -> parent.switchTab(LukeController.Tab.ANALYZER));

    selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
    selectColumn.setCellFactory(col -> {
      CheckBoxTableCell<SelectedField, Boolean> cell = new CheckBoxTableCell<>();
      cell.setSelectedStateCallback(idx -> {
        BooleanProperty prop = fieldsTable.getItems().get(idx).selectedProperty();
        if (!prop.get()) {
          selectAll.setSelected(false);
        }
        return prop;
      });
      cell.setAlignment(Pos.CENTER);
      return cell;
    });

    fieldColumn.setCellValueFactory(new PropertyValueFactory<>("field"));

    fieldList = FXCollections.observableArrayList();
    fieldsTable.setItems(fieldList);
  }

  private Search searchModel;

  private LukeController parent;

  private SearchController searchController;

  private MLTConfig config;

  @Inject
  public MLTController(Search searchModel) {
    this.searchModel = searchModel;
  }

  public void setParent(SearchController searchController, LukeController parent) {
    this.searchController = searchController;
    this.parent = parent;
  }

  public void setCurrentAnalyzer(Analyzer analyzer) {
    this.analyzerName.setText(analyzer.getClass().getSimpleName());
  }

  public void populateFields() {
    fieldList.clear();
    fieldList.addAll(searchModel.getFieldNames().stream()
        .map(SelectedField::of).collect(Collectors.toList()));
  }

  public MLTConfig getMLTConfig() {
    if (Strings.isNullOrEmpty(maxDocFreq.getText())) {
      config.setMaxDocFreq(0);
    } else {
      config.setMaxDocFreq(Integer.parseInt(maxDocFreq.getText()));
    }
    if (Strings.isNullOrEmpty(minDocFreq.getText())) {
      config.setMinDocFreq(0);
    } else {
      config.setMinDocFreq(Integer.parseInt(minDocFreq.getText()));
    }
    if (Strings.isNullOrEmpty(minTermFreq.getText())) {
      config.setMinTermFreq(0);
    } else {
      config.setMinTermFreq(Integer.parseInt(minTermFreq.getText()));
    }
    config.clearFields();
    fieldList.stream()
        .filter(SelectedField::isSelected)
        .map(SelectedField::getField)
        .forEach(f -> config.addField(f));
    return config;
  }

}
