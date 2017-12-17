package org.apache.lucene.luke.app.controllers.dialog;

import javafx.scene.Node;
import javafx.stage.Stage;

public interface DialogWindowController {

  default void closeWindow(Node node) {
    Stage stage = (Stage) node.getScene().getWindow();
    stage.close();
  }

}
