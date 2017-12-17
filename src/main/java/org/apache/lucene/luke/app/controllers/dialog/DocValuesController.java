package org.apache.lucene.luke.app.controllers.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import org.apache.lucene.luke.models.documents.DocValues;
import org.apache.lucene.luke.util.BytesRefUtils;
import org.apache.lucene.util.NumericUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DocValuesController implements DialogWindowController {

  @FXML
  private Label field;

  @FXML
  private Label dvType;

  @FXML
  private FlowPane decoderPane;

  @FXML
  private ChoiceBox<Decoder> decoders;

  @FXML
  private ListView<String> values;

  private ObservableList<String> valueList;

  @FXML
  private Button copy;

  @FXML
  private Button close;

  private DocValues docValues;

  @FXML
  private void initialize() {
    decoders.getItems().addAll(Arrays.asList(Decoder.values()));
    decoders.setValue(Decoder.LONG);
    decoders.setOnAction(e -> {
      onChangeDecoder(decoders.getSelectionModel().getSelectedItem());
    });

    valueList = FXCollections.observableArrayList();
    values.setItems(valueList);
    values.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    copy.setOnAction(e -> copyToClipboard());
    close.setOnAction(e -> closeWindow(close));
  }

  public void setValue(String fieldName, DocValues docValues) {
    this.docValues = docValues;

    field.setText(fieldName);
    dvType.setText(docValues.getDvType().toString());

    if (docValues.getValues().size() > 0) {
      decoderPane.setDisable(true);
      valueList.setAll(
          docValues.getValues().stream()
              .map(BytesRefUtils::decode)
              .collect(Collectors.toList()));

    } else if (docValues.getNumericValues().size() > 0) {
      valueList.setAll(
          docValues.getNumericValues().stream()
              .map(String::valueOf)
              .collect(Collectors.toList())
      );
    }
  }

  private void onChangeDecoder(Decoder decoder) {
    if (docValues.getNumericValues().isEmpty()) {
      return;
    }
    valueList.clear();
    switch (decoder) {
      case LONG:
        valueList.setAll(
            docValues.getNumericValues().stream()
                .map(String::valueOf)
                .collect(Collectors.toList())
        );
        break;
      case FLOAT:
        valueList.setAll(
            docValues.getNumericValues().stream()
                .mapToInt(Long::intValue)
                .mapToObj(NumericUtils::sortableIntToFloat)
                .map(String::valueOf)
                .collect(Collectors.toList())
        );
        break;
      case DOUBLE:
        valueList.setAll(
            docValues.getNumericValues().stream()
                .map(NumericUtils::sortableLongToDouble)
                .map(String::valueOf)
                .collect(Collectors.toList())
        );
        break;
      default:
    }
  }

  private void copyToClipboard() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    ObservableList<String> selected = values.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      content.putString(String.join("\n", valueList));
    } else {
      content.putString(String.join("\n", selected));
    }
    clipboard.setContent(content);
  }

  enum Decoder {

    LONG("long"), FLOAT("float"), DOUBLE("double");

    private final String label;

    Decoder(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}

