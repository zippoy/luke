package org.apache.lucene.luke.app.util;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.util.MessageUtils;

import java.util.function.Consumer;

public class DialogOpener<T> {

  private Window owner;

  private String styleResourceName;

  public DialogOpener(LukeController parent) {
    this.owner = parent.getPrimaryWindow();
    this.styleResourceName = parent.getStyleResourceName();
  }

  public Stage show(Stage stage, String title, String resourceName, int width, int height, Consumer<? super T> initializer,
                    String... styleSheets)
      throws Exception {
    FXMLLoader loader = new FXMLLoader(DialogOpener.class.getResource(resourceName), MessageUtils.getBundle());
    Parent root = loader.load();
    initializer.accept(loader.getController());

    if (stage == null) {
      stage = new Stage();
      stage.initOwner(owner);
    }

    stage.setTitle(title);
    stage.setScene(new Scene(root, width, height));
    stage.getScene().getStylesheets().addAll(
        getClass().getResource("/styles/luke.css").toExternalForm(),
        getClass().getResource(styleResourceName).toExternalForm());
    for (String styleSheet : styleSheets) {
      stage.getScene().getStylesheets().add(getClass().getResource(styleSheet).toExternalForm());
    }
    stage.show();
    // move this window to the front
    stage.toFront();
    return stage;
  }

}
