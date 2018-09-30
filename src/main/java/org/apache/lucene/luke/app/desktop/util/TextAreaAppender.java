package org.apache.lucene.luke.app.desktop.util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.JTextArea;

public class TextAreaAppender extends AppenderSkeleton {

  private static JTextArea textArea;

  public static void setTextArea(JTextArea ta) {
    if (textArea != null) {
      throw new IllegalStateException("TextArea already set.");
    }
    textArea = ta;
  }

  @Override
  protected void append(LoggingEvent event) {
    if (textArea == null) {
      throw new IllegalStateException();
    }

    String message = this.layout.format(event);
    textArea.append(message);

    if (layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      if (s != null) {
        for (int i = 0; i < s.length; i++) {
          textArea.append(s[i]);
          textArea.append(Layout.LINE_SEP);
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
