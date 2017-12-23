package org.apache.lucene.luke.app.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class HelpController implements DialogWindowController {

  @FXML
  private Label description;

  @FXML
  private AnchorPane content;

  @FXML
  private Button close;

  @FXML
  private void initialize() {
    close.setOnAction(e -> closeWindow(close));
  }

  public void setDescription(String desc) {
    description.setText(desc);
  }

  public void setContent(Node child) {
    AnchorPane.setTopAnchor(child, 0.0);
    AnchorPane.setBottomAnchor(child, 0.0);
    AnchorPane.setLeftAnchor(child, 0.0);
    AnchorPane.setRightAnchor(child, 0.0);
    content.getChildren().add(child);
  }
}
