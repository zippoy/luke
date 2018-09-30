package org.apache.lucene.luke.app.desktop.util;

import javax.swing.*;
import java.awt.*;

public class ImageUtils {

  public static ImageIcon createImageIcon(String path, int width, int height) {
    return createImageIcon(path, "", width, height);
  }

  public static ImageIcon createImageIcon(String path, String description, int width, int height) {
    java.net.URL imgURL = ImageUtils.class.getResource(path);
    if (imgURL != null) {
      ImageIcon originalIcon = new ImageIcon(imgURL, description);
      ImageIcon icon = new ImageIcon(originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
      return icon;
    } else {
      return null;
    }
  }

  public static ImageIcon createImageIcon(String path) {
    return createImageIcon(path, "");
  }

  public static ImageIcon createImageIcon(String path, String description) {
    java.net.URL imgURL = ImageUtils.class.getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL, description);
    } else {
      return null;
    }
  }

  private ImageUtils() {}
}
