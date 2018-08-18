package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class AnalyzerPaneProvider implements Provider<JScrollPane> {

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(analyzerNamePane());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(analysisChanePane());

    return new JScrollPane(panel);
  }

  private JPanel analyzerNamePane() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_analyzer.label.name")));

    JLabel analyzerLabel = new JLabel("StandardAnalyzer");
    panel.add(analyzerLabel);

    JLabel changeLabel = new JLabel(MessageUtils.getLocalizedMessage("search_analyzer.hyperlink.change"));
    panel.add(changeLabel);

    return panel;
  }

  private JPanel analysisChanePane() {
    JPanel panel = new JPanel(new GridLayout(7, 1));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_analyzer.label.chain")));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_analyzer.label.charfilters")));

    panel.add(new JList<>(new String[]{}));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_analyzer.label.tokenizer")));

    panel.add(new JTextField("StandardTokenizerFactory"));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_analyzer.label.tokenfilters")));

    panel.add(new JList<>(new String[]{}));

    return panel;
  }

}
