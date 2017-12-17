package org.apache.lucene.luke.app.controllers.dto;

public class TokenAttValue {
  private String attClass;
  private String name;
  private String value;

  public static TokenAttValue of(String attClass, String name, String value) {
    TokenAttValue attValue = new TokenAttValue();
    attValue.attClass = attClass;
    attValue.name = name;
    attValue.value = value;
    return attValue;
  }

  private TokenAttValue() {
  }

  public String getAttClass() {
    return attClass;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
