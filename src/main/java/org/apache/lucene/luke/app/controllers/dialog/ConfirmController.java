package org.apache.lucene.luke.app.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.lucene.luke.models.LukeException;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class ConfirmController implements DialogWindowController {

  @FXML
  private Label message;

  @FXML
  private Button ok;

  @FXML
  private Button cancel;

  @FXML
  private void initialize() {
    ok.setOnAction(e -> runnableWrapper(() -> {
      callback.exec();
      closeWindow(ok);
    }));
    cancel.setOnAction(e -> closeWindow(cancel));
  }

  public void setContent(String content) {
    message.setText(content);
  }

  private Callback callback = () -> {
  };

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @FunctionalInterface
  public interface Callback {
    void exec() throws LukeException;
  }
}
