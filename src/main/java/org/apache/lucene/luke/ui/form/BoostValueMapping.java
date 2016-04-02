package org.apache.lucene.luke.ui.form;

import org.apache.pivot.wtk.TextInput;

public class BoostValueMapping implements TextInput.TextBindMapping {
  @Override
  public String toString(Object value) {
    return (!(value instanceof Float) || Float.isNaN((Float) value)) ? null : String.valueOf(value);
  }

  @Override
  public Object valueOf(String s) {
    return Float.parseFloat(s);
  }

}
