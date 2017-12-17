package org.apache.lucene.luke.app.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class StoredValueController implements DialogWindowController {

  @FXML
  private Label field;

  @FXML
  private TextArea value;

  @FXML
  private Button copy;

  @FXML
  private Button close;


  @FXML
  private void initialize() {
    copy.setOnAction(e -> copyToClipboard());
    close.setOnAction(e -> closeWindow(close));
  }

  public void setValue(String fieldName, String stored) {
    field.setText(fieldName);
    value.setText(stored);
  }

  private void copyToClipboard() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(value.getText());
    clipboard.setContent(content);
  }

}
