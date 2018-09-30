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
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.util.TextAreaAppender;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import static org.apache.lucene.luke.app.desktop.util.ExceptionHandler.handle;

public class LukeMain {

  private static JFrame frame;

  public static JFrame getOwnerFrame() {
    return frame;
  }

  private static void createAndShowGUI() {
    Injector injector = DesktopModule.getIngector();

    // uncaught error handler
    MessageBroker messageBroker = injector.getInstance(MessageBroker.class);
    Thread.setDefaultUncaughtExceptionHandler((thread, cause) ->
        handle(cause, messageBroker)
    );

    // prepare log4j appender for Logs tab.
    JTextArea textArea = injector.getInstance(Key.get(JTextArea.class, Names.named("log_area")));
    TextAreaAppender.setTextArea(textArea);

    frame = injector.getInstance(JFrame.class);
    frame.setLocation(200, 100);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

    OpenIndexDialogFactory.showOpenIndexDialog();
  }

  public static void main(String[] args) throws Exception {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    javax.swing.SwingUtilities.invokeLater(LukeMain::createAndShowGUI);

  }
}
