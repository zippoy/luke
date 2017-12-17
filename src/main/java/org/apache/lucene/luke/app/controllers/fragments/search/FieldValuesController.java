package org.apache.lucene.luke.app.controllers.fragments.search;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.lucene.luke.app.controllers.dto.SelectedField;
import org.apache.lucene.luke.models.search.Search;

import java.util.Set;
import java.util.stream.Collectors;

public class FieldValuesController {

  @FXML
  private CheckBox loadAll;

  @FXML
  private TableView<SelectedField> fieldsTable;

  @FXML
  private TableColumn<SelectedField, Boolean> selectColumn;

  @FXML
  private TableColumn<SelectedField, String> fieldColumn;

  private ObservableList<SelectedField> fieldList;

  @FXML
  private void initialize() {
    loadAll.setSelected(true);
    loadAll.setOnAction(e ->
        fieldList.forEach(f -> f.setSelected(loadAll.isSelected()))
    );

    selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
    selectColumn.setCellFactory(col -> {
      CheckBoxTableCell<SelectedField, Boolean> cell = new CheckBoxTableCell<>();
      cell.setSelectedStateCallback(idx -> {
        BooleanProperty prop = fieldsTable.getItems().get(idx).selectedProperty();
        if (!prop.get()) {
          loadAll.setSelected(false);
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

  private Search modelSearch;

  @Inject
  public FieldValuesController(Search modelSearch) {
    this.modelSearch = modelSearch;
  }

  public void populateFields() {
    fieldList.clear();
    fieldList.addAll(modelSearch.getFieldNames().stream()
        .map(SelectedField::of).collect(Collectors.toList()));
  }

  public Set<String> getFieldsToLoad() {
    return fieldList.stream()
        .filter(SelectedField::isSelected)
        .map(SelectedField::getField).collect(Collectors.toSet());
  }

  public void setFieldsToLoad(Set<String> fields) {
    loadAll.setSelected(false);
    fieldList.forEach(f -> f.setSelected(fields.contains(f.getField())));
  }
}
