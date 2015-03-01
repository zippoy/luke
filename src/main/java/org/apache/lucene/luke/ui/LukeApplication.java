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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Theme;

public class LukeApplication implements Application {
  public static final String LANGUAGE_KEY = "language";

  private static final String DEFAULT_LANG = "en";

  LukeWindow lukeWindow = null;

  @Override
  public void startup(Display display, Map<String,String> properties) throws Exception {
    try {

      String language = properties.get(LANGUAGE_KEY);
      Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
      Resources resources = null;
      try {
        resources = new Resources(getClass().getName(), locale);
      } catch (MissingResourceException e) {
        // if resource can't find, use default locale (en)
        locale = new Locale(DEFAULT_LANG);
        resources = new Resources(getClass().getName(), locale);
        // if not reset default locale, same exception will occur
        // when BXMLSerializer.readObject() is called with 3rd option localize=true...
        Locale.setDefault(locale);
      }

      Theme theme = Theme.getTheme();
      Font font = theme.getFont();

      // Search for a font that can support the sample string
      String sampleResource = (String) resources.get("sampleText");
      if (font.canDisplayUpTo(sampleResource) != -1) {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

        for (int i = 0; i < fonts.length; i++) {
          if (fonts[i].canDisplayUpTo(sampleResource) == -1) {
            theme.setFont(fonts[i].deriveFont(Font.PLAIN, 12));
            break;
          }
        }
      }

      BXMLSerializer bxmlSerializer = new BXMLSerializer();

      lukeWindow = (LukeWindow) bxmlSerializer.readObject(LukeApplication.class, "LukeWindow.bxml", true);

      lukeWindow.openLukeWindow(display);
    } catch (Exception e) {
      if (lukeWindow != null) {
        e.printStackTrace();
        Alert.alert(MessageType.ERROR, e.getMessage(), lukeWindow);
      } else {
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean shutdown(boolean optional) {
    if (lukeWindow != null) {
      lukeWindow.close();
    }

    return false;
  }

  @Override
  public void suspend() {}

  @Override
  public void resume() {}

  public static void main(String[] args) {
    DesktopApplicationContext.main(LukeApplication.class, args);
  }
}
