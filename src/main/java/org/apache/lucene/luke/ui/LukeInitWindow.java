package org.apache.lucene.luke.ui;

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

import java.io.File;
import java.net.URL;

import org.apache.lucene.luke.ui.LukeWindow.LukeMediator;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TextInput;

public class LukeInitWindow extends Dialog implements Bindable {
  @BXML
  TextInput indexPath;
  @BXML
  Checkbox ramDir;
  @BXML
  Checkbox ram;
  @BXML
  Checkbox readOnly;
  @BXML
  Checkbox keepCommits;
  @BXML
  Spinner tiiDiv;
  @BXML
  TextInput directory;
  private LukeMediator lukeMediator;

  public LukeInitWindow() {
    Action.getNamedActions().put("browse", new Action() {
      @Override
      public void perform(Component component) {
        openDialogue();
      }
    });
    Action.getNamedActions().put("openIndex", new Action() {
      @Override
      public void perform(Component component) {
        lukeMediator.getLukeWindow().openIndex(false, directory.getText(), readOnly.isSelected(), false, keepCommits.isSelected(), null,
            (Integer) tiiDiv.getSelectedItem());
      }
    });
  }

  public void initLukeMediator(LukeMediator lukeMediator) {
    this.lukeMediator = lukeMediator;
  }

  @Override
  public void initialize(Map<String,Object> namespace, URL location, Resources resources) {

  }

  private void openDialogue() {

    final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);

    fileBrowserSheet.open(this, new SheetCloseListener() {
      @Override
      public void sheetClosed(Sheet sheet) {
        if (sheet.getResult()) {
          Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();
          indexPath.setText(selectedFiles.get(0).getAbsolutePath());
        } else {
          Alert.alert(MessageType.INFO, "You didn't select anything.", LukeInitWindow.this);
        }
      }
    });
  }
}
