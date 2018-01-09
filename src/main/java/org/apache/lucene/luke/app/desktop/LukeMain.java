package org.apache.lucene.luke.app.desktop;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.models.analysis.Analysis;
import org.apache.lucene.luke.models.analysis.AnalysisImpl;
import org.apache.lucene.luke.models.commits.Commits;
import org.apache.lucene.luke.models.commits.CommitsImpl;
import org.apache.lucene.luke.models.documents.Documents;
import org.apache.lucene.luke.models.documents.DocumentsImpl;
import org.apache.lucene.luke.models.overview.Overview;
import org.apache.lucene.luke.models.overview.OverviewImpl;
import org.apache.lucene.luke.models.search.Search;
import org.apache.lucene.luke.models.search.SearchImpl;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.models.tools.IndexToolsImpl;
import org.apache.lucene.luke.util.MessageUtils;

import java.io.File;

import static org.apache.lucene.luke.app.util.ExceptionHandler.handle;

public class LukeMain extends Application {

  private LukeController mainController;

  @Override
  public void start(Stage primaryStage) throws Exception {

    Thread.setDefaultUncaughtExceptionHandler((thread, cause) ->
        handle(cause, mainController)
    );

    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Overview.class).to(OverviewImpl.class).in(Scopes.SINGLETON);
        bind(Documents.class).to(DocumentsImpl.class).in(Scopes.SINGLETON);
        bind(Analysis.class).to(AnalysisImpl.class).in(Scopes.SINGLETON);
        bind(Search.class).to(SearchImpl.class).in(Scopes.SINGLETON);
        bind(Commits.class).to(CommitsImpl.class).in(Scopes.SINGLETON);
        bind(IndexTools.class).to(IndexToolsImpl.class).in(Scopes.SINGLETON);

        bind(Preferences.class).to(PreferencesImpl.class).in(Scopes.SINGLETON);
      }
    });

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/luke.fxml"),
        MessageUtils.getBundle());
    loader.setControllerFactory(injector::getInstance);

    Parent root = loader.load();
    this.mainController = loader.getController();
    primaryStage.setTitle(MessageUtils.getLocalizedMessage("window.title"));
    primaryStage.setScene(new Scene(root, 900, 650));
    System.out.println(new File(".").getAbsolutePath() + " <--- we are here.");
    primaryStage.getIcons().add(new Image("file:src/main/resources/img/lucene.gif"));
    primaryStage.show();

    mainController.resetStyles();
    mainController.showOpenIndexDialog();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
