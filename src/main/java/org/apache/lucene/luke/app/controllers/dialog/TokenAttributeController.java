package org.apache.lucene.luke.app.controllers.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.lucene.luke.app.controllers.dto.TokenAttValue;
import org.apache.lucene.luke.models.analysis.Analysis;

import java.util.List;
import java.util.stream.Collectors;


public class TokenAttributeController implements DialogWindowController {
  @FXML
  private Label term;

  @FXML
  private TableView<TokenAttValue> attTable;

  @FXML
  private TableColumn<TokenAttValue, String> attClassColumn;

  @FXML
  private TableColumn<TokenAttValue, String> nameColumn;

  @FXML
  private TableColumn<TokenAttValue, String> valueColumn;

  private ObservableList<TokenAttValue> attList;

  @FXML
  private Button ok;

  @FXML
  private void initialize() {
    attClassColumn.setCellValueFactory(new PropertyValueFactory<>("attClass"));
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    attList = FXCollections.observableArrayList();
    attTable.setItems(attList);

    ok.setOnAction(e -> closeWindow(ok));
  }

  public void populate(String termText, List<Analysis.TokenAttribute> attributes) {
    term.setText(termText);
    attList.addAll(attributes.stream()
        .flatMap(att -> att.attValues.entrySet().stream()
            .map(e -> TokenAttValue.of(att.attClass, e.getKey(), e.getValue())
            )).collect(Collectors.toList()));
  }
}
