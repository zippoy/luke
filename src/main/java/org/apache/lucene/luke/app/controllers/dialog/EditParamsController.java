package org.apache.lucene.luke.app.controllers.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.lucene.luke.app.controllers.dto.Param;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EditParamsController implements DialogWindowController {

  @FXML
  private Label target;

  @FXML
  private TableView<Param> paramsTable;

  @FXML
  private TableColumn<Param, Boolean> delColumn;

  @FXML
  private TableColumn<Param, String> nameColumn;

  @FXML
  private TableColumn<Param, String> valueColumn;

  private ObservableList<Param> paramList;

  @FXML
  private Button ok;

  @FXML
  private Button cancel;

  @FXML
  private void initialize() {
    delColumn.setCellValueFactory(data -> {
      BooleanProperty prop = data.getValue().getDeletedProperty();
      prop.addListener((obs, oldV, newV) ->
          paramsTable.getSelectionModel().getSelectedItem().setDeleted(newV)
      );
      return prop;
    });
    delColumn.setCellFactory(col -> {
      CheckBoxTableCell<Param, Boolean> cell = new CheckBoxTableCell<>();
      cell.setAlignment(Pos.CENTER);
      return cell;
    });

    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    nameColumn.setOnEditCommit(e -> {
      int rowIdx = paramsTable.getSelectionModel().getFocusedIndex();
      paramList.get(rowIdx).setName(e.getNewValue());
    });

    valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    valueColumn.setOnEditCommit(e -> {
      int rowIdx = paramsTable.getSelectionModel().getFocusedIndex();
      paramList.get(rowIdx).setValue(e.getNewValue());
    });

    paramList = FXCollections.observableArrayList();
    paramsTable.setItems(paramList);

    ok.setOnAction(e -> onOk());
    cancel.setOnAction(e -> closeWindow(cancel));
  }

  private void onOk() {
    callback.accept(paramList);
    closeWindow(ok);
  }

  private Consumer<List<Param>> callback;

  public void setTarget(String targetName) {
    this.target.setText(targetName);
  }

  public void setCallback(Consumer<List<Param>> callback) {
    this.callback = callback;
  }

  public void populate(@Nullable Map<String, String> paramMap) {
    List<Param> li;
    if (paramMap != null) {
      li = new ArrayList<>();
      for (Map.Entry<String, String> entry : paramMap.entrySet()) {
        li.add(Param.of(entry.getKey(), entry.getValue()));
      }
      li.addAll(IntStream.range(0, Math.max(10 - paramMap.size(), 0))
          .mapToObj(i -> Param.newInstance())
          .collect(Collectors.toList()));
    } else {
      li = IntStream.range(0, 10).mapToObj(i -> Param.newInstance()).collect(Collectors.toList());
    }
    paramList.addAll(li);
  }
}
