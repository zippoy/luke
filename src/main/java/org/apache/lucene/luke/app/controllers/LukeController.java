/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.luke.app.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.analysis.Analysis;
import org.apache.lucene.luke.models.commits.Commits;
import org.apache.lucene.luke.models.documents.Documents;
import org.apache.lucene.luke.models.overview.Overview;
import org.apache.lucene.luke.models.search.Search;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.util.IndexUtils;
import org.apache.lucene.luke.util.MessageUtils;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class LukeController {

  private static final Logger logger = LoggerFactory.getLogger(LukeController.class);

  @FXML
  private AnchorPane primary;

  @FXML
  private MenuBar menuBar;

  @FXML
  private TabPane tabPane;

  @FXML
  private AnchorPane overview;

  @FXML
  private MenubarController menubarController;

  @FXML
  private OverviewController overviewController;

  @FXML
  private DocumentsController documentsController;

  @FXML
  private SearchController searchController;

  @FXML
  private AnalysisController analysisController;

  @FXML
  private CommitsController commitsController;

  @FXML
  private Label statusMessage;

  @FXML
  private ImageView multiIcon;

  @FXML
  private ImageView roIcon;

  @FXML
  private ImageView noReaderIcon;

  private String indexPath;

  private String dirImpl;


  @FXML
  private void initialize() {
    this.children = Lists.newArrayList(
        menubarController,
        overviewController,
        documentsController,
        searchController,
        analysisController,
        commitsController);

    for (ChildController child : children) {
      child.setParent(this);
    }

    // disable tabs until an index opened.
    tabPane.getTabs().get(Tab.OVERVIEW.index()).setDisable(true);
    tabPane.getTabs().get(Tab.DOCUMENTS.index()).setDisable(true);
    tabPane.getTabs().get(Tab.SEARCH.index()).setDisable(true);
    tabPane.getTabs().get(Tab.COMMITS.index()).setDisable(true);

    tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) ->
        runnableWrapper(() -> {
          clearStatusMessage();
          if (newV.equals(Tab.DOCUMENTS.index())) {
            documentsController.setCurrentAnalyzer(analysisModel.currentAnalyzer());
          }
          if (newV.equals(Tab.SEARCH.index())) {
            searchController.setCurrentAnalyzer(analysisModel.currentAnalyzer());
          }
        }));

    multiIcon.setVisible(false);
    Tooltip.install(multiIcon, new Tooltip(MessageUtils.getLocalizedMessage("tooltip.multi_reader")));

    roIcon.setVisible(false);
    Tooltip.install(roIcon, new Tooltip(MessageUtils.getLocalizedMessage("tooltip.read_only")));

    noReaderIcon.setVisible(false);
    Tooltip.install(noReaderIcon, new Tooltip(MessageUtils.getLocalizedMessage("tooltip.no_reader")));
  }

  public void showOpenIndexDialog() throws Exception {
    menubarController.showOpenIndexDialog();
  }

  // -------------------------------------------------
  // methods for interaction with other controllers
  // -------------------------------------------------

  public Window getPrimaryWindow() {
    return primary.getScene().getWindow();
  }

  OverviewController getOverviewController() {
    return overviewController;
  }

  DocumentsController getDocumentsController() {
    return documentsController;
  }

  SearchController getSearchController() {
    return searchController;
  }

  public void onDirectoryOpen(@Nonnull String indexPath, @Nullable String dirImpl) throws Exception {
    // close old index
    onClose();

    this.dir = IndexUtils.openDirectory(indexPath, dirImpl);
    this.indexPath = indexPath;
    this.dirImpl = dirImpl;

    roIcon.setVisible(false);
    noReaderIcon.setVisible(true);

    commitsModel.reset(dir, indexPath);
    toolsModel.reset(dir, indexPath, useCompound, keepAllCommits);

    for (ChildController child : children) {
      child.onDirectoryOpen();
    }

    tabPane.getTabs().get(Tab.COMMITS.index()).setDisable(false);
  }

  public void onIndexOpen(@Nonnull String indexPath, @Nullable String dirImpl, boolean readOnly, boolean useCompound, boolean keepAllCommits)
      throws LukeException {
    // close old index
    onClose();

    try {
      this.reader = IndexUtils.openIndex(indexPath, dirImpl);
    } catch (Exception e) {
      logger.error("Failed to open index: " + indexPath, e);
      throw new LukeException(MessageUtils.getLocalizedMessage("openindex.message.index_path_invalid", indexPath), e);
    }
    this.indexPath = indexPath;
    this.dirImpl = dirImpl;
    this.readOnly = readOnly;
    this.useCompound = useCompound;
    this.keepAllCommits = keepAllCommits;

    if (hasDirectoryReader()) {
      multiIcon.setVisible(false);
    } else {
      multiIcon.setVisible(true);
    }

    if (readOnly) {
      roIcon.setVisible(true);
    } else {
      roIcon.setVisible(false);
    }

    noReaderIcon.setVisible(false);

    overviewModel.reset(reader, indexPath);
    documentsModel.reset(reader);
    searchModel.reset(reader);
    commitsModel.reset(reader, indexPath);
    toolsModel.reset(reader, indexPath, useCompound, keepAllCommits);

    for (ChildController child : children) {
      child.onIndexOpen();
    }

    // enable tabs
    tabPane.getTabs().get(Tab.OVERVIEW.index()).setDisable(false);
    tabPane.getTabs().get(Tab.DOCUMENTS.index()).setDisable(false);
    tabPane.getTabs().get(Tab.SEARCH.index()).setDisable(false);
    if (hasDirectoryReader()) {
      tabPane.getTabs().get(Tab.COMMITS.index()).setDisable(false);
    }

    documentsController.setCurrentAnalyzer(analysisModel.currentAnalyzer());
    searchController.setCurrentAnalyzer(analysisModel.currentAnalyzer());
  }

  public void onClose() {
    IndexUtils.close(dir);
    IndexUtils.close(reader);

    for (ChildController child : children) {
      child.onClose();
    }

    // disable tabs until an index opened.
    tabPane.getTabs().get(Tab.OVERVIEW.index()).setDisable(true);
    tabPane.getTabs().get(Tab.DOCUMENTS.index()).setDisable(true);
    tabPane.getTabs().get(Tab.SEARCH.index()).setDisable(true);
    tabPane.getTabs().get(Tab.COMMITS.index()).setDisable(true);

    this.indexPath = null;
    this.dirImpl = null;
  }

  public void onIndexReopen() throws LukeException {
    if (indexPath == null || indexPath.length() == 0) {
      showStatusMessage(MessageUtils.getLocalizedMessage("menu.message.index_not_opened"));
      return;
    }
    // save current settings
    String currentPath = this.indexPath;
    String currentDirImpl = this.dirImpl;

    onIndexOpen(currentPath, currentDirImpl, readOnly, useCompound, keepAllCommits);
  }

  public void onDirectoryReopen() throws Exception {
    if (indexPath == null || indexPath.length() == 0) {
      showStatusMessage(MessageUtils.getLocalizedMessage("menu.message.index_not_opened"));
      return;
    }
    // save current settings
    String currentPath = this.indexPath;
    String currentDirImpl = this.dirImpl;

    onDirectoryOpen(currentPath, currentDirImpl);
  }

  public void switchTab(Tab tab) {
    tabPane.getSelectionModel().select(tab.index());
  }

  public String getIndexPath() {
    return indexPath;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean hasDirectoryReader() {
    return reader instanceof DirectoryReader;
  }

  private Directory dir;

  private IndexReader reader;

  private boolean readOnly;

  private boolean useCompound;

  private boolean keepAllCommits;

  private Overview overviewModel;

  private Documents documentsModel;

  private Search searchModel;

  private Analysis analysisModel;

  private Commits commitsModel;

  private IndexTools toolsModel;

  private Preferences prefs;

  private List<ChildController> children;

  private ColorTheme colorTheme;

  @Inject
  public LukeController(Preferences prefs,
                        Overview overviewModel, Documents documentsModel,
                        Search searchModel, Analysis analysisModel,
                        Commits commitsModel, IndexTools toolsModel) {
    this.prefs = prefs;
    this.overviewModel = overviewModel;
    this.documentsModel = documentsModel;
    this.searchModel = searchModel;
    this.analysisModel = analysisModel;
    this.commitsModel = commitsModel;
    this.toolsModel = toolsModel;
    this.colorTheme = prefs.getTheme();
  }

  public void setColorTheme(ColorTheme colorTheme) {
    this.colorTheme = colorTheme;
    try {
      prefs.setTheme(colorTheme);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    resetStyles();
  }

  public void resetStyles() {
    getPrimaryWindow().getScene().getStylesheets().clear();
    getPrimaryWindow().getScene().getStylesheets().addAll(
        getClass().getResource("/styles/luke.css").toExternalForm(),
        getClass().getResource(colorTheme.resourceName()).toExternalForm()
    );
  }

  public String getStyleResourceName() {
    return colorTheme.resourceName();
  }

  public Preferences getPrefs() {
    return prefs;
  }

  public void showStatusMessage(String message) {
    statusMessage.setText(message);
  }

  public void showUnknownErrorMessage() {
    statusMessage.setText(MessageUtils.getLocalizedMessage("message.error.unknown"));
  }

  public void clearStatusMessage() {
    statusMessage.setText("");
  }

  public enum Tab {
    OVERVIEW(0), DOCUMENTS(1), SEARCH(2), ANALYZER(3), COMMITS(4);

    private int tabIdx;

    Tab(int tabIdx) {
      this.tabIdx = tabIdx;
    }

    int index() {
      return tabIdx;
    }
  }

  public enum ColorTheme {
    GRAY, CLASSIC, SANDSTONE, NAVY;

    String resourceName() {
      return String.format("/styles/theme_%s.css", name().toLowerCase());
    }
  }
}
