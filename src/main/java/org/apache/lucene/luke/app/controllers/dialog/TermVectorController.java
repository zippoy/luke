package org.apache.lucene.luke.app.controllers.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.lucene.luke.app.controllers.dto.TermVector;
import org.apache.lucene.luke.models.documents.TermVectorEntry;

import java.util.List;
import java.util.stream.Collectors;

public class TermVectorController implements DialogWindowController {

  @FXML
  private Label field;

  @FXML
  private TableView<TermVector> termVectorTable;

  @FXML
  private TableColumn<TermVector, String> termColumn;

  @FXML
  private TableColumn<TermVector, Integer> freqColumn;

  @FXML
  private TableColumn<TermVector, String> positionsColumn;

  @FXML
  private TableColumn<TermVector, String> offsetsColumn;

  private ObservableList<TermVector> termVectorList;

  @FXML
  private Button close;

  @FXML
  private void initialize() {
    // initialize term vector table
    termColumn.setCellValueFactory(new PropertyValueFactory<>("termText"));
    freqColumn.setCellValueFactory(new PropertyValueFactory<>("freq"));
    positionsColumn.setCellValueFactory(new PropertyValueFactory<>("positions"));
    offsetsColumn.setCellValueFactory(new PropertyValueFactory<>("offsets"));
    termVectorList = FXCollections.observableArrayList();
    termVectorTable.setItems(termVectorList);

    close.setOnAction(e -> closeWindow(close));
  }

  public void setTermVector(String fieldName, List<TermVectorEntry> tvEntries) {
    field.setText(fieldName);
    termVectorList.clear();
    termVectorList.addAll(tvEntries.stream().map(TermVector::of).collect(Collectors.toList()));
  }
}
