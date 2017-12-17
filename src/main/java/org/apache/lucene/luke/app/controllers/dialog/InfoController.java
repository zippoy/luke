package org.apache.lucene.luke.app.controllers.dialog;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;


public class InfoController implements DialogWindowController {

  @FXML
  private Button back;

  @FXML
  private WebView webView;

  @FXML
  private Button close;

  @FXML
  private void initialize() {
    back.setOnAction(e -> goBack());
    close.setOnAction(e -> closeWindow(close));
  }

  private void goBack() {
    Platform.runLater(() -> {
      webView.getEngine().executeScript("history.back()");
    });
  }

  public void setContent(String content) {
    webView.getEngine().load(content);
  }

}
