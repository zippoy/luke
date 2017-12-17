package org.apache.lucene.luke.app.controllers.dialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.app.controllers.MenubarController;
import org.apache.lucene.luke.app.util.IndexTask;
import org.apache.lucene.luke.app.util.IntegerTextFormatter;
import org.apache.lucene.luke.app.util.TextAreaPrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class OptimizeController implements DialogWindowController {

  private static final Logger logger = LoggerFactory.getLogger(OptimizeController.class);

  @FXML
  private Label dirPath;

  @FXML
  private CheckBox expunge;

  @FXML
  private Spinner<Integer> numSegments;

  @FXML
  private Button optimize;

  @FXML
  private Button close;

  @FXML
  private Label status;

  @FXML
  private Pane indicatorPane;

  @FXML
  private TextArea info;

  private PrintStream ps;

  @FXML
  private void initialize() {
    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1, -1);
    numSegments.setValueFactory(valueFactory);
    TextFormatter<Integer> textFormatter = new IntegerTextFormatter(valueFactory.getConverter(), 1);
    valueFactory.valueProperty().bindBidirectional(textFormatter.valueProperty());
    numSegments.getEditor().setTextFormatter(textFormatter);
    numSegments.focusedProperty().addListener((obs, oldV, newV) -> {
      if (newV) {
        // won't not change value, but commit editor
        // https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
        numSegments.increment(0);
      }
    });

    ps = new TextAreaPrintStream(info, new ByteArrayOutputStream(), logger);

    optimize.setOnAction(e -> optimize());
    close.setOnAction(e -> closeWindow(close));
  }

  private void optimize() {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    Task task = new IndexTask<Void>(indicatorPane) {

      @Override
      protected Void call() throws Exception {
        try {
          menubarController.optimize(expunge.isSelected(), numSegments.getValue(), ps);
        } catch (Exception e) {
          Platform.runLater(() -> logger.error(e.getMessage(), e));
          throw e;
        } finally {
          ps.flush();
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> runnableWrapper(() -> parent.onIndexReopen()));
    status.textProperty().bind(task.messageProperty());

    executor.submit(task);
    executor.shutdown();
  }

  private LukeController parent;

  private MenubarController menubarController;

  public void setIndexPath(String indexPath) {
    dirPath.setText(indexPath);
  }

  public void setParent(LukeController parent, MenubarController menubarController) {
    this.parent = parent;
    this.menubarController = menubarController;
  }

}
