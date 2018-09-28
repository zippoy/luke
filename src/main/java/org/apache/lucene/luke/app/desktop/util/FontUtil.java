package org.apache.lucene.luke.app.desktop.util;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

public class FontUtil {

  @SuppressWarnings("unchecked")
  public static JLabel toLinkText(JLabel label) {
    label.setForeground(StyleConstants.LINK_COLOR);
    Font font = label.getFont();
    Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) font.getAttributes();
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    label.setFont(font.deriveFont(attributes));
    return label;
  }

  private FontUtil() {}

}
