package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.util.Version;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;


public class LukeWindowProvider implements Provider<JFrame> {

  private static final String WINDOW_TITLE = MessageUtils.getLocalizedMessage("window.title") + " - v" + Version.LATEST.toString();

  private final MessageBroker messageBroker;

  private final JMenuBar menuBar;

  private final JTabbedPane tabbedPane;

  private final JLabel messageLbl = new JLabel();

  private final JLabel multiIcon = new JLabel();

  private final JLabel readOnlyIcon = new JLabel();

  private final JLabel noReaderIcon = new JLabel();

  public class Observer implements IndexObserver, DirectoryObserver {

    @Override
    public void openDirectory(LukeState state) {
      multiIcon.setVisible(false);
      readOnlyIcon.setVisible(false);
      noReaderIcon.setVisible(true);

      messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("message.directory_opened"));
    }

    @Override
    public void closeDirectory() {
      multiIcon.setVisible(false);
      readOnlyIcon.setVisible(false);
      noReaderIcon.setVisible(false);

      messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("message.directory_closed"));
    }

    @Override
    public void openIndex(LukeState state) {
      multiIcon.setVisible(!state.hasDirectoryReader());
      readOnlyIcon.setVisible(state.readOnly());
      noReaderIcon.setVisible(false);

      if (state.readOnly()) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("message.index_opened_ro"));
      } else if (!state.hasDirectoryReader()) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("message.index_opened_multi"));
      } else {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("message.index_opened"));
      }
    }

    @Override
    public void closeIndex() {
      multiIcon.setVisible(false);
      readOnlyIcon.setVisible(false);
      noReaderIcon.setVisible(false);

      messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("message.index_closed"));
    }

    private Observer() {}
  }

  public class MessageReceiverImpl implements MessageBroker.MessageReceiver {

    @Override
    public void showStatusMessage(String message) {
      messageLbl.setText(message);
    }

    @Override
    public void showUnknownErrorMessage() {
      messageLbl.setText(MessageUtils.getLocalizedMessage("message.error.unknown"));
    }

    @Override
    public void clearStatusMessage() {
      messageLbl.setText("");
    }

    private MessageReceiverImpl() {}

  }

  @Inject
  public LukeWindowProvider(JMenuBar menuBar,
                            @Named("main") JTabbedPane tabbedPane,
                            DirectoryHandler directoryHandler,
                            IndexHandler indexHandler,
                            MessageBroker messageBroker) {
    this.menuBar = menuBar;
    this.tabbedPane = tabbedPane;
    this.messageBroker = messageBroker;

    Observer observer = new Observer();
    directoryHandler.addObserver(observer);
    indexHandler.addObserver(observer);

    messageBroker.registerReceiver(new MessageReceiverImpl());
  }

  @Override
  public JFrame get() {
    JFrame frame = new JFrame(WINDOW_TITLE);
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

    tabbedPane.setEnabledAt(TabbedPaneProvider.Tab.OVERVIEW.index(), false);
    tabbedPane.setEnabledAt(TabbedPaneProvider.Tab.DOCUMENTS.index(), false);
    tabbedPane.setEnabledAt(TabbedPaneProvider.Tab.SEARCH.index(), false);
    tabbedPane.setEnabledAt(TabbedPaneProvider.Tab.COMMITS.index(), false);

    panel.add(tabbedPane);

    return panel;
  }

  private JPanel createMessagePanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));

    JPanel innerPanel = new JPanel(new GridBagLayout());
    innerPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    msgPanel.add(messageLbl);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.8;
    innerPanel.add(msgPanel, c);

    JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    multiIcon.setIcon(ImageUtils.createImageIcon("/img/icon_grid-2x2.png", "multi reader", 16, 16));
    multiIcon.setToolTipText(MessageUtils.getLocalizedMessage("tooltip.multi_reader"));
    multiIcon.setVisible(false);
    iconPanel.add(multiIcon);


    readOnlyIcon.setIcon(ImageUtils.createImageIcon("/img/icon_lock.png", "read only", 16, 16));
    readOnlyIcon.setToolTipText(MessageUtils.getLocalizedMessage("tooltip.read_only"));
    readOnlyIcon.setVisible(false);
    iconPanel.add(readOnlyIcon);

    noReaderIcon.setIcon(ImageUtils.createImageIcon("/img/icon_cone.png", "no reader", 16, 16));
    noReaderIcon.setToolTipText(MessageUtils.getLocalizedMessage("tooltip.no_reader"));
    noReaderIcon.setVisible(false);
    iconPanel.add(noReaderIcon);

    JLabel luceneIcon = new JLabel(ImageUtils.createImageIcon("/img/lucene.gif", "lucene", 16, 16));
    iconPanel.add(luceneIcon);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.2;
    innerPanel.add(iconPanel);
    panel.add(innerPanel);

    return panel;
  }

}
