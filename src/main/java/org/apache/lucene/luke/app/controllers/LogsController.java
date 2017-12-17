package org.apache.lucene.luke.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.lucene.luke.app.util.TextAreaAppender;

import java.io.File;

public class LogsController {

  private static final String LOG_FILE = System.getProperty("user.home") + File.separator + ".luke.d" + File.separator + "luke.log";

  @FXML
  private Label logFile;

  @FXML
  private TextArea textArea;

  @FXML
  private void initialize() {
    logFile.setText(LOG_FILE);
    TextAreaAppender.textArea = textArea;
  }

}
