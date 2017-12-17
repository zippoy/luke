package org.apache.lucene.luke.app.util;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class IntegerTextFormatter extends TextFormatter<Integer> {

  public IntegerTextFormatter(StringConverter<Integer> valueConverter, Integer defaultValue) {
    super(valueConverter, defaultValue,
        (change -> {
          if (change.isAdded()) {
            try {
              // only numbers allowed
              Integer.parseInt(change.getText());
              return change;
            } catch (NumberFormatException e) {
              return null;
            }
          }
          return change;
        }));
  }
}
