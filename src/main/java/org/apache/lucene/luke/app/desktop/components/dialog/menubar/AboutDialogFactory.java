package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.FontUtil;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.app.desktop.util.URLLabel;
import org.apache.lucene.luke.models.LukeException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;
import java.net.URISyntaxException;

public class AboutDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  @Override
  public JDialog create(Window owner, String title, int width, int height) {
    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    panel.add(header(), BorderLayout.PAGE_START);
    panel.add(center(), BorderLayout.CENTER);
    panel.add(footer(), BorderLayout.PAGE_END);

    return panel;
  }

  private JPanel header() {
    JPanel panel = new JPanel(new GridLayout(3, 1));

    JPanel logo = new JPanel(new FlowLayout(FlowLayout.CENTER));
    logo.add(new JLabel(ImageUtils.createImageIcon("/img/luke-logo.gif", 200, 40)));
    panel.add(logo);

    JPanel project = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JLabel projectLbl = new JLabel("Lucene Toolbox Project");
    projectLbl.setFont(new Font(projectLbl.getFont().getFontName(), Font.BOLD, 32));
    projectLbl.setForeground(Color.decode("#5aaa88"));
    project.add(projectLbl);
    panel.add(project);

    JPanel desc = new JPanel();
    desc.setLayout(new BoxLayout(desc, BoxLayout.PAGE_AXIS));

    JPanel subTitle = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    JLabel subTitleLbl = new JLabel("GUI client of the best Java search library Apache Lucene");
    subTitleLbl.setFont(new Font(subTitleLbl.getFont().getFontName(), Font.PLAIN, 20));
    subTitle.add(subTitleLbl);
    subTitle.add(new JLabel(ImageUtils.createImageIcon("/img/lucene-logo.gif", 100, 15)));
    desc.add(subTitle);

    JPanel link = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    JLabel linkLbl = FontUtil.toLinkText(new URLLabel("https://lucene.apache.org/"));
    link.add(linkLbl);
    desc.add(link);

    panel.add(desc);

    return panel;
  }

  private static final String LICENSE_NOTICE =
      "<p>Created by Andrzej Bialecki &lt;ab@getopt.org&gt; <br>" +
          "Further developed by: Dmitry Kan &lt;dmitry.lucene@gmail.com&gt;, Tomoko Uchida &lt;tomoko.uchida.1111@gmail.com&gt; <br>" +
          "Backed by pull-requests from our fantastic community.</p>" +
          "<p>[License]</p>" +
          "<p>Luke is distributed under <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache License Version 2.0</a> (http://www.apache.org/licenses/LICENSE-2.0) " +
          "and includes <a href=\"https://www.elegantthemes.com/blog/resources/elegant-icon-font\">The Elegant Icon Font</a> (https://www.elegantthemes.com/blog/resources/elegant-icon-font) " +
          "licensed under <a href=\"https://opensource.org/licenses/MIT\">MIT</a> (https://opensource.org/licenses/MIT)</p>";

  private static final HyperlinkListener hyperlinkListener = e -> {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      if (Desktop.isDesktopSupported()) {
        try {
          Desktop.getDesktop().browse(e.getURL().toURI());
        } catch (IOException | URISyntaxException ex) {
          throw new LukeException(ex.getMessage(), ex);
        }
      }
  };

  private JScrollPane center() {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setMargin(new Insets(5, 5, 5, 5));
    editorPane.setContentType("text/html");
    editorPane.setText(LICENSE_NOTICE);
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(hyperlinkListener);
    JScrollPane scrollPane = new JScrollPane(editorPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    return scrollPane;
  }

  private JPanel footer() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    closeBtn.setMargin(new Insets(5, 5, 5, 5));
    if (closeBtn.getActionListeners().length == 0) {
      closeBtn.addActionListener(e -> dialog.dispose());
    }
    panel.add(closeBtn);
    return panel;
  }

}
