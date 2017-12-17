package org.apache.lucene.luke.app.util;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import org.apache.lucene.luke.util.MessageUtils;

public abstract class IndexTask<T> extends Task<T> {

  private Pane indicatorPane;

  protected IndexTask(Pane indicatorPane) {
    this.indicatorPane = indicatorPane;
  }

  @Override
  protected void running() {
    updateMessage("Running...");
    ProgressIndicator pi = new ProgressIndicator();
    pi.setPrefHeight(20);
    pi.setPrefWidth(20);
    indicatorPane.getChildren().add(pi);
  }

  @Override
  protected void succeeded() {
    updateMessage("Done.");
    indicatorPane.getChildren().clear();
  }

  @Override
  protected void failed() {
    updateMessage(MessageUtils.getLocalizedMessage("message.error.unknown"));
    indicatorPane.getChildren().clear();
  }
}
