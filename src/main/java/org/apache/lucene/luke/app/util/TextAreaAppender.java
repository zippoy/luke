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

package org.apache.lucene.luke.app.util;

import javafx.scene.control.TextArea;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class TextAreaAppender extends AppenderSkeleton {

  public static TextArea textArea;

  public TextAreaAppender() {
  }

  @Override
  protected void append(LoggingEvent event) {
    if (textArea == null) {
      throw new IllegalStateException();
    }

    String message = this.layout.format(event);
    textArea.selectEnd();
    textArea.appendText(message);

    if (layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      if (s != null) {
        for (int i = 0; i < s.length; i++) {
          textArea.appendText(s[i]);
          textArea.appendText(Layout.LINE_SEP);
        }
      }
    }
  }

  @Override
  public void close() {

  }

  @Override
  public boolean requiresLayout() {
    return true;
  }
}
