package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;

public class LogsPanelProvider implements Provider<JPanel> {

  private static final String LOG_FILE = System.getProperty("user.home") + File.separator + ".luke.d" + File.separator + "luke.log";

  private final JTextArea logTextArea;

  @Inject
  public LogsPanelProvider(@Named("log_area") JTextArea logTextArea) {
    this.logTextArea = logTextArea;
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("logs.label.see_also")));

    JLabel logPathLabel = new JLabel(LOG_FILE);
    header.add(logPathLabel);

    panel.add(header, BorderLayout.PAGE_START);

    panel.add(new JScrollPane(logTextArea), BorderLayout.CENTER);

    return panel;
  }

}
