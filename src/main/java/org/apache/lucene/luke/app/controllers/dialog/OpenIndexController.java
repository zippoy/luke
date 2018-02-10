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

package org.apache.lucene.luke.app.controllers.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.util.DialogOpener;
import org.apache.lucene.luke.util.MessageUtils;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NativeUnixDirectory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class OpenIndexController implements DialogWindowController {

  @FXML
  private ChoiceBox<String> indexPath;

  private ObservableList<String> indexHistory;

  @FXML
  private Button browse;

  @FXML
  private CheckBox readOnly;

  @FXML
  private ChoiceBox<String> dirImpl;

  private ObservableList<String> dirImplList;

  @FXML
  private CheckBox noReader;

  @FXML
  private CheckBox useCompound;

  @FXML
  private ToggleGroup keepCommitsGroup;

  @FXML
  private RadioButton keepLastCommit;

  @FXML
  private RadioButton keepAllCommits;

  @FXML
  private Button ok;

  @FXML
  private Button cancel;

  @FXML
  private void initialize() throws Exception {
    indexHistory = FXCollections.observableArrayList();
    indexPath.setItems(indexHistory);
    browse.setOnAction(e -> showBrowseDialog());

    // load Directory sub types
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("org.apache.lucene.store"))
        .setScanners(new SubTypesScanner())
        .filterInputsBy(new FilterBuilder().include("org\\.apache\\.lucene\\.store.*"))
    );
    Set<Class<? extends FSDirectory>> clazzSet = reflections.getSubTypesOf(FSDirectory.class);
    dirImplList = FXCollections.observableArrayList();
    dirImplList.add(FSDirectory.class.getName());
    dirImplList.add(MMapDirectory.class.getName());
    dirImplList.addAll(clazzSet.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    dirImplList.remove(NativeUnixDirectory.class.getName());
    dirImpl.setItems(dirImplList);
    //dirImpl.setValue(FSDirectory.class.getName());

    readOnly.setOnAction(e -> setWriterConfigsDisable(readOnly.isSelected()));

    //keepLastCommit.setSelected(true);

    ok.setOnAction(e -> runnableWrapper(this::openIndex));
    cancel.setOnAction(e -> closeWindow(cancel));
  }

  private void setWriterConfigsDisable(boolean value) {
    useCompound.setDisable(value);
    keepLastCommit.setDisable(value);
    keepAllCommits.setDisable(value);
  }

  private void showBrowseDialog() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    File homeDir = new File(System.getProperty("user.home"));
    directoryChooser.setInitialDirectory(homeDir);
    Window window = browse.getScene().getWindow();
    File file = directoryChooser.showDialog(window);
    if (file != null) {
      indexHistory.add(file.getAbsolutePath());
      indexPath.setValue(file.getAbsolutePath());
    }
  }

  private void openIndex() throws Exception {
    String selectedPath = indexPath.getValue();
    String dirImplClazz = dirImpl.getValue();
    if (selectedPath == null || selectedPath.length() == 0) {
      String msg = MessageUtils.getLocalizedMessage("openindex.message.index_path_not_selected");
      showInvalidIndexSelectedDialog(msg);
      return;
    }
    if (noReader.isSelected()) {
      parent.onDirectoryOpen(selectedPath, dirImplClazz);
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("openindex.message.dirctory_opened"));
    } else {
      parent.onIndexOpen(selectedPath, dirImplClazz, readOnly.isSelected(), useCompound.isSelected(),
          keepCommitsGroup.getSelectedToggle().getUserData().equals("all"));
      parent.switchTab(LukeController.Tab.OVERVIEW);
      if (parent.isReadOnly()) {
        parent.showStatusMessage(MessageUtils.getLocalizedMessage("openindex.message.index_opened_ro"));
      } else if (!parent.hasDirectoryReader()) {
        parent.showStatusMessage(MessageUtils.getLocalizedMessage("openindex.message.index_opened_multi"));
      } else {
        parent.showStatusMessage(MessageUtils.getLocalizedMessage("openindex.message.index_opened"));
      }

    }
    addHistory(selectedPath);
    prefs.setIndexOpenerPrefs(readOnly.isSelected(), dirImpl.getValue(), noReader.isSelected(), useCompound.isSelected(), keepAllCommits.isSelected());
    closeWindow(ok);
  }

  private Stage warnDialog;

  private void showInvalidIndexSelectedDialog(String message) throws Exception {
    warnDialog = new DialogOpener<WarnController>(parent).show(
        warnDialog,
        "Invalid index path",
        "/fxml/dialog/warn.fxml",
        400, 200,
        (controller) -> controller.setContent(message)
    );
  }

  private LukeController parent;

  private Preferences prefs;

  public void setParent(LukeController parent) {
    this.parent = parent;
  }

  public void setPrefs(Preferences prefs) {
    this.prefs = prefs;
    setHistory(prefs.getHistory());
    readOnly.setSelected(prefs.isReadOnly());
    dirImpl.setValue(prefs.getDirImpl());
    noReader.setSelected(prefs.isNoReader());
    useCompound.setSelected(prefs.isUseCompound());
    keepAllCommits.setSelected(prefs.isKeepAllCommits());
    keepLastCommit.setSelected(!prefs.isKeepAllCommits());
  }

  private void setHistory(List<String> history) {
    this.indexHistory.setAll(history);
    if (indexHistory.size() > 0) {
      indexPath.setValue(indexHistory.get(0));
    }
  }

  private void addHistory(String indexPath) throws IOException {
    prefs.addHistory(indexPath);
    setHistory(prefs.getHistory());
  }

}
