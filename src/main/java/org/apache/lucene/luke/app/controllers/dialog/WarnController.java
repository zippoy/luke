package org.apache.lucene.luke.app.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WarnController implements DialogWindowController {

  @FXML
  private Label message;

  @FXML
  private Button close;

  @FXML
  private void initialize() {
    close.setOnAction(e -> closeWindow(close));
  }

  public void setContent(String content) {
    message.setText(content);
  }

}
