package org.apache.lucene.luke.app.desktop.util;

import org.apache.lucene.luke.models.LukeException;

import javax.swing.JLabel;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class URLLabel extends JLabel {

  private final URL link;

  public URLLabel(String text) {
    super(text);

    try {
      this.link = new URL(text);
    } catch (MalformedURLException e) {
      throw new LukeException(e.getMessage(), e);
    }

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        openUrl(link);
      }
    });
  }

  private void openUrl(URL link) {
    if(Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(link.toURI());
      }
      catch (IOException | URISyntaxException e) {
        throw new LukeException(e.getMessage(), e);
      }
    }
  }
}
