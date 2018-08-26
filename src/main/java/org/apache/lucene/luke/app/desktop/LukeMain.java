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

package org.apache.lucene.luke.app.desktop;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class LukeMain {

  private static void createAndShowGUI() {
    Injector injector = DesktopModule.getIngector();
    JFrame frame = injector.getInstance(JFrame.class);

    frame.setLocation(200, 100);
    frame.setVisible(true);

    JDialog dialog = injector.getInstance(Key.get(JDialog.class, Names.named("menubar_openindex")));
    dialog.setVisible(true);
  }

  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(LukeMain::createAndShowGUI);
  }
}
