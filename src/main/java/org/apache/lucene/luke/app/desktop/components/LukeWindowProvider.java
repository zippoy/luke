package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;


public class LukeWindowProvider implements IndexObserver, DirectoryObserver, Provider<JFrame> {

  private final JMenuBar menuBar;

  private final JPanel overviewPanel;

  private final JPanel documentsPanel;

  private final JPanel searchPanel;

  private final JPanel analysisPanel;

  private final JPanel commitsPanel;

  private final JPanel logsPanel;

  @Inject
  public LukeWindowProvider(JMenuBar menuBar,
                            @Named("overview") JPanel overviewPanel,
                            @Named("documents") JPanel documentsPanel,
                            @Named("search") JPanel searchPanel,
                            @Named("analysis") JPanel analysisPanel,
                            @Named("commits") JPanel commitsPanel,
                            @Named("logs") JPanel logsPanel) {
    this.menuBar = menuBar;
    this.overviewPanel = overviewPanel;
    this.documentsPanel = documentsPanel;
    this.searchPanel = searchPanel;
    this.analysisPanel = analysisPanel;
    this.commitsPanel = commitsPanel;
    this.logsPanel = logsPanel;
  }

  public JFrame get() {
    JFrame frame = new JFrame(MessageUtils.getLocalizedMessage("window.title"));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    frame.setJMenuBar(menuBar);
    frame.add(createMainPanel(), BorderLayout.CENTER);
    frame.add(createMessagePanel(), BorderLayout.PAGE_END);

    frame.setPreferredSize(new Dimension(900, 680));
    frame.pack();

    return frame;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));

    JTabbedPane tabbedPane = new JTabbedPane();

    tabbedPane.addTab("Overview", ImageUtils.createImageIcon("/img/icon_house_alt.png", 20, 20), overviewPanel);
    tabbedPane.addTab("Documents", ImageUtils.createImageIcon("/img/icon_documents_alt.png", 20, 20), documentsPanel);
    tabbedPane.addTab("Search", ImageUtils.createImageIcon("/img/icon_search.png", 20, 20), searchPanel);
    tabbedPane.addTab("Analysis", ImageUtils.createImageIcon("/img/icon_pencil-edit_alt.png", 20, 20), analysisPanel);
    tabbedPane.addTab("Commits", ImageUtils.createImageIcon("/img/icon_drive.png", 20, 20), commitsPanel);
    tabbedPane.addTab("Logs", ImageUtils.createImageIcon("/img/icon_document.png", 20, 20), logsPanel);

    panel.add(tabbedPane);

    return panel;
  }

  private JPanel createMessagePanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));

    JPanel innerPanel = new JPanel(new GridLayout(1, 2));
    innerPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel message = new JLabel("status message");
    msgPanel.add(message);
    innerPanel.add(msgPanel);

    JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JLabel multiIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_grid-2x2.png", "multi reader", 16, 16));
    JLabel roIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_lock.png", "read only", 16, 16));
    JLabel noReaderIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_cone.png", "no reader", 16, 16));
    JLabel luceneIcon = new JLabel(ImageUtils.createImageIcon("/img/lucene.gif", "lucene", 16, 16));
    iconPanel.add(multiIcon);
    iconPanel.add(roIcon);
    iconPanel.add(noReaderIcon);
    iconPanel.add(luceneIcon);
    innerPanel.add(iconPanel);

    panel.add(innerPanel);
    return panel;
  }

  @Override
  public void openDirectory(LukeState state) {

  }

  @Override
  public void closeDirectory() {

  }

  @Override
  public void openIndex(LukeState state) {

  }

  @Override
  public void closeIndex() {

  }
}
