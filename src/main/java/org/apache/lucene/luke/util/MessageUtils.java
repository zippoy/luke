package org.apache.lucene.luke.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageUtils {

  private static ResourceBundle bundle = ResourceBundle.getBundle("fxml/messages", Locale.getDefault());

  public static ResourceBundle getBundle() {
    return bundle;
  }

  public static String getLocalizedMessage(String key) {
    return bundle.getString(key);
  }

  public static String getLocalizedMessage(String key, Object... args) {
    String pattern = bundle.getString(key);
    return MessageFormat.format(pattern, args);
  }

  private MessageUtils() {
  }
}
