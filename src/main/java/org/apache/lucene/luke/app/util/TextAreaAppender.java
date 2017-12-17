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
