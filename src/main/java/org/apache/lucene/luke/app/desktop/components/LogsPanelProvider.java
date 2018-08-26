package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class LogsPanelProvider implements Provider<JPanel> {

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("logs.label.see_also")));

    JLabel logPathLabel = new JLabel("luke.log");
    header.add(logPathLabel);

    panel.add(header, BorderLayout.PAGE_START);

    JTextArea textArea = new JTextArea();
    panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

    return panel;
  }

}
