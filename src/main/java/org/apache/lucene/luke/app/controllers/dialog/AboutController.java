package org.apache.lucene.luke.app.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AboutController implements DialogWindowController {

  @FXML
  private Button close;

  @FXML
  private void initialize() {
    close.setOnAction(e -> closeWindow(close));
  }

}
