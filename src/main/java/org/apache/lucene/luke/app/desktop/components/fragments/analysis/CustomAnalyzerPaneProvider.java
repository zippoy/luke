package org.apache.lucene.luke.app.desktop.components.fragments.analysis;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;


public class CustomAnalyzerPaneProvider implements Provider<JPanel> {

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(createCustomAnalyzerHeader(), BorderLayout.PAGE_START);
    panel.add(createCustomAnalyzerChain(), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createCustomAnalyzerHeader() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.label.config_dir")));
    JTextField confDirTF = new JTextField(30);
    panel.add(confDirTF);
    JButton confDirBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.browse"),
        ImageUtils.createImageIcon("/img/icon_folder-open_alt.png", 20, 20));
    panel.add(confDirBtn);
    JButton buildBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.build_analyzser"),
        ImageUtils.createImageIcon("/img/icon_puzzle.png", 20, 20));
    panel.add(buildBtn);
    JButton loadJarBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.hyperlink.load_jars"));
    panel.add(loadJarBtn);

    return panel;
  }

  private JPanel createCustomAnalyzerChain() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,3));

    panel.add(createCharFilterConfig());
    panel.add(createTokenizerConfig());
    panel.add(createTokenFilterConfig());

    return panel;
  }

  private JPanel createCharFilterConfig() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.black));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.charfilters"));
    label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    panel.add(label, c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    JList<String> selectedList = new JList<>(new String[]{});
    selectedList.setVisibleRowCount(1);
    JScrollPane selectedPanel = new JScrollPane(selectedList);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    panel.add(selectedPanel, c);

    JButton editBtn = new JButton(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"),
        ImageUtils.createImageIcon("/img/icon_pencil.png", 20, 20));
    c.gridx = 7;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(editBtn, c);

    JLabel addLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.add"),
        ImageUtils.createImageIcon("/img/icon_plus.png", 20,20),
        JLabel.LEFT);
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(addLabel, c);

    JComboBox<String> factoryList = new JComboBox<>();
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    panel.add(factoryList, c);

    return panel;
  }

  private JPanel createTokenizerConfig() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.black));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.tokenizer"));
    label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    panel.add(label, c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    JTextField selectedTF = new JTextField(15);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    panel.add(selectedTF, c);

    JButton editBtn = new JButton(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"),
        ImageUtils.createImageIcon("/img/icon_pencil.png", 20, 20));
    c.gridx = 7;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(editBtn, c);

    JLabel setLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.set"),
        ImageUtils.createImageIcon("/img/icon_pushpin_alt.png", 20,20),
        JLabel.LEFT);
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(setLabel, c);

    JComboBox<String> factoryList = new JComboBox<>();
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    panel.add(factoryList, c);

    return panel;
  }

  private JPanel createTokenFilterConfig() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.black));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.charfilters"));
    label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    panel.add(label, c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    JList<String> selectedList = new JList<>(new String[]{});
    selectedList.setVisibleRowCount(1);
    JScrollPane selectedPanel = new JScrollPane(selectedList);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    panel.add(selectedPanel, c);

    JButton editBtn = new JButton(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"),
        ImageUtils.createImageIcon("/img/icon_pencil.png", 20, 20));
    c.gridx = 7;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(editBtn, c);

    JLabel addLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.add"),
        ImageUtils.createImageIcon("/img/icon_plus.png", 20,20),
        JLabel.LEFT);
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    panel.add(addLabel, c);

    JComboBox<String> factoryList = new JComboBox<>();
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    panel.add(factoryList, c);

    return panel;
  }
}
