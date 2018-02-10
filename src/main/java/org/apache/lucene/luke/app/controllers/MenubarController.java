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

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.luke.app.controllers.dialog.AboutController;
import org.apache.lucene.luke.app.controllers.dialog.CheckIndexController;
import org.apache.lucene.luke.app.controllers.dialog.OpenIndexController;
import org.apache.lucene.luke.app.controllers.dialog.OptimizeController;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.util.DialogOpener;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.util.MessageUtils;
import org.apache.lucene.util.Version;

import java.io.PrintStream;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class MenubarController implements ChildController {

  @FXML
  private MenuItem menuOpenIndex;

  @FXML
  private MenuItem menuReopenIndex;

  @FXML
  private MenuItem menuCloseIndex;

  @FXML
  private MenuItem menuExit;

  @FXML
  private MenuItem menuOptimizeIndex;

  @FXML
  private MenuItem menuCheckIndex;

  @FXML
  private MenuItem menuThemeGray;

  @FXML
  private MenuItem menuThemeClassic;

  @FXML
  private MenuItem menuThemeSandstone;

  @FXML
  private MenuItem menuThemeNavy;

  @FXML
  private MenuItem menuAbout;

  @FXML
  private void initialize() {
    menuOpenIndex.setOnAction(e -> runnableWrapper(this::showOpenIndexDialog));

    menuReopenIndex.setOnAction(e -> runnableWrapper(() -> {
      parent.onIndexReopen();
      parent.switchTab(LukeController.Tab.OVERVIEW);
    }));
    menuReopenIndex.setDisable(true);

    menuCloseIndex.setOnAction(e -> runnableWrapper(() -> {
      parent.onClose();
      parent.switchTab(LukeController.Tab.OVERVIEW);
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("menu.message.index_closed"));
    }));
    menuCloseIndex.setDisable(true);

    menuExit.setOnAction(e -> exit());

    menuOptimizeIndex.setOnAction(e -> runnableWrapper(this::showOptimizeDialog));
    menuOptimizeIndex.setDisable(true);

    menuCheckIndex.setOnAction(e -> runnableWrapper(this::showCheckIndexDialog));
    menuCheckIndex.setDisable(true);

    menuThemeGray.setOnAction(e -> changeTheme(LukeController.ColorTheme.GRAY));
    menuThemeClassic.setOnAction(e -> changeTheme(LukeController.ColorTheme.CLASSIC));
    menuThemeSandstone.setOnAction(e -> changeTheme(LukeController.ColorTheme.SANDSTONE));
    menuThemeNavy.setOnAction(e -> changeTheme(LukeController.ColorTheme.NAVY));

    menuAbout.setOnAction(e -> runnableWrapper(this::showAboutDialog));
  }

  private Stage openIndexDialog = null;

  public void showOpenIndexDialog() throws Exception {
    openIndexDialog = new DialogOpener<OpenIndexController>(parent).show(
        openIndexDialog,
        "Choose index directory path",
        "/fxml/dialog/openindex.fxml",
        600, 400,
        (controller) -> {
          controller.setParent(parent);
          controller.setPrefs(prefs);
        }
    );
  }

  private Stage optimizeDialog = null;

  private void showOptimizeDialog() throws Exception {
    optimizeDialog = new DialogOpener<OptimizeController>(parent).show(
        optimizeDialog,
        "Optimize index",
        "/fxml/dialog/optimize.fxml",
        600, 600,
        (controller) -> {
          controller.setParent(parent, this);
          controller.setIndexPath(parent.getIndexPath());
        }
    );
  }

  private Stage checkIndexDialog = null;

  private void showCheckIndexDialog() throws Exception {
    checkIndexDialog = new DialogOpener<CheckIndexController>(parent).show(
        checkIndexDialog,
        "Check index",
        "/fxml/dialog/checkindex.fxml",
        600, 600,
        (controller) -> {
          controller.setParent(parent, this);
          controller.setIndexPath(parent.getIndexPath());
        }
    );
  }

  private void changeTheme(LukeController.ColorTheme theme) {
    parent.setColorTheme(theme);
  }

  private Stage aboutDialog = null;

  private void showAboutDialog() throws Exception {
    String version = Version.LATEST.toString();
    aboutDialog = new DialogOpener<AboutController>(parent).show(
        aboutDialog,
        "About Luke v" + version,
        "/fxml/dialog/about.fxml",
        1000, 480,
        (controller) -> {},
        "/styles/about.css"
    );

  }

  private Preferences prefs;

  private LukeController parent;

  private IndexTools toolsModel;

  private void exit() {
    parent.onClose();
    ((Stage) parent.getPrimaryWindow()).close();
  }

  @Inject
  public MenubarController(Preferences prefs, IndexTools toolsModel) {
    this.prefs = prefs;
    this.toolsModel = toolsModel;
  }

  @Override
  public void onDirectoryOpen() {
    menuReopenIndex.setDisable(true);
    menuCloseIndex.setDisable(false);
    menuOptimizeIndex.setDisable(true);
    menuCheckIndex.setDisable(false);
  }

  @Override
  public void onIndexOpen() throws LukeException {
    menuReopenIndex.setDisable(false);
    menuCloseIndex.setDisable(false);
    if (!parent.isReadOnly() && parent.hasDirectoryReader()) {
      menuOptimizeIndex.setDisable(false);
    }
    if (parent.hasDirectoryReader()) {
      menuCheckIndex.setDisable(false);
    }
  }

  @Override
  public void onClose() {
    menuReopenIndex.setDisable(true);
    menuCloseIndex.setDisable(true);
    menuOptimizeIndex.setDisable(true);
    menuCheckIndex.setDisable(true);
  }

  @Override
  public void setParent(LukeController parent) {
    this.parent = parent;
  }

  public void optimize(boolean expunge, int maxNumSegments, PrintStream ps) throws LukeException {
    toolsModel.optimize(expunge, maxNumSegments, ps);
  }

  public CheckIndex.Status checkIndex(PrintStream ps) throws LukeException {
    return toolsModel.checkIndex(ps);
  }

  public void repairIndex(CheckIndex.Status st, PrintStream ps) throws LukeException {
    toolsModel.repairIndex(st, ps);
  }
}
