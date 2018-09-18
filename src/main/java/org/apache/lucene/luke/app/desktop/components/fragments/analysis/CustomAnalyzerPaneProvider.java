package org.apache.lucene.luke.app.desktop.components.fragments.analysis;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.components.util.FontUtil;
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
import javax.swing.JSeparator;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;


public class CustomAnalyzerPaneProvider implements Provider<JPanel> {

  private final JTextField confDirTF = new JTextField();

  private final JButton confDirBtn = new JButton();

  private final JButton buildBtn = new JButton();

  private final JLabel loadJarLbl = new JLabel();

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
    confDirTF.setColumns(30);
    panel.add(confDirTF);
    confDirBtn.setText(MessageUtils.getLocalizedMessage("analysis.button.browse"));
    confDirBtn.setIcon(ImageUtils.createImageIcon("/img/icon_folder-open_alt.png", 20, 20));
    confDirBtn.setFont(new Font(confDirBtn.getFont().getFontName(), Font.PLAIN, 15));
    confDirBtn.setMargin(new Insets(2, 2, 2, 2));
    panel.add(confDirBtn);
    buildBtn.setText(MessageUtils.getLocalizedMessage("analysis.button.build_analyzser"));
    buildBtn.setIcon(ImageUtils.createImageIcon("/img/icon_puzzle.png", 20, 20));
    buildBtn.setFont(new Font(buildBtn.getFont().getFontName(), Font.PLAIN, 15));
    buildBtn.setMargin(new Insets(2, 2, 2, 2));
    panel.add(buildBtn);
    loadJarLbl.setText(MessageUtils.getLocalizedMessage("analysis.hyperlink.load_jars"));
    panel.add(FontUtil.toLinkText(loadJarLbl));

    return panel;
  }

  private JPanel createCustomAnalyzerChain() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    //panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,3));

    panel.add(createCustomChainConfig());
    //panel.add(createCharFilterConfig());
    //panel.add(createTokenizerConfig());
    //panel.add(createTokenFilterConfig());

    return panel;
  }

  private JPanel createCustomChainConfig() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.black));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    GridBagConstraints sepc = new GridBagConstraints();
    sepc.fill = GridBagConstraints.HORIZONTAL;
    sepc.weightx = 1.0;
    sepc.gridwidth = GridBagConstraints.REMAINDER;

    // char filters
    JLabel cfLbl = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.charfilters"));
    cfLbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(cfLbl, c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    JList<String> selectedCfList = new JList<>(new String[]{});
    selectedCfList.setVisibleRowCount(1);
    JScrollPane selectedPanel = new JScrollPane(selectedCfList);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(selectedPanel, c);

    JButton cfEditBtn = new JButton(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"),
        ImageUtils.createImageIcon("/img/icon_pencil.png", 15, 15));
    cfEditBtn.setFont(new Font(cfEditBtn.getFont().getFontName(), Font.PLAIN, 15));
    cfEditBtn.setMargin(new Insets(2, 4, 2, 4));
    c.fill = GridBagConstraints.NONE;
    c.gridx = 7;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(cfEditBtn, c);

    JLabel cfAddLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.add"),
        ImageUtils.createImageIcon("/img/icon_plus.png", 20,20),
        JLabel.LEFT);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(cfAddLabel, c);

    JComboBox<String> cfFactoryList = new JComboBox<>();
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(cfFactoryList, c);

    // separator
    sepc.gridx = 0;
    sepc.gridy = 3;
    c.anchor = GridBagConstraints.LINE_START;
    panel.add(new JSeparator(JSeparator.HORIZONTAL), sepc);

    // tokenizer
    JLabel tokLabel = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.tokenizer"));
    tokLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tokLabel, c);

    c.gridx = 1;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    JTextField selectedTokTF = new JTextField(15);
    c.gridx = 2;
    c.gridy = 4;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(selectedTokTF, c);

    JButton tokEditBtn = new JButton(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"),
        ImageUtils.createImageIcon("/img/icon_pencil.png", 15, 15));
    tokEditBtn.setFont(new Font(tokEditBtn.getFont().getFontName(), Font.PLAIN, 15));
    tokEditBtn.setMargin(new Insets(2, 4, 2, 4));
    c.fill = GridBagConstraints.NONE;
    c.gridx = 7;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tokEditBtn, c);

    JLabel setTokLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.set"),
        ImageUtils.createImageIcon("/img/icon_pushpin_alt.png", 20,20),
        JLabel.LEFT);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(setTokLabel, c);

    JComboBox<String> tokFactoryList = new JComboBox<>();
    c.gridx = 2;
    c.gridy = 6;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(tokFactoryList, c);

    // separator
    sepc.gridx = 0;
    sepc.gridy = 7;
    c.anchor = GridBagConstraints.LINE_START;
    panel.add(new JSeparator(JSeparator.HORIZONTAL), sepc);

    // token filters
    JLabel tfLbl = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.tokenfilters"));
    tfLbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    c.gridx = 0;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tfLbl, c);

    c.gridx = 1;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    JList<String> selectedList = new JList<>(new String[]{});
    selectedList.setVisibleRowCount(1);
    JScrollPane selectedTfPanel = new JScrollPane(selectedList);
    c.gridx = 2;
    c.gridy = 8;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(selectedTfPanel, c);

    JButton tfEditBtn = new JButton(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"),
        ImageUtils.createImageIcon("/img/icon_pencil.png", 15, 15));
    tfEditBtn.setFont(new Font(tfEditBtn.getFont().getFontName(), Font.PLAIN, 15));
    tfEditBtn.setMargin(new Insets(2, 4, 2, 4));
    c.fill = GridBagConstraints.NONE;
    c.gridx = 7;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tfEditBtn, c);

    JLabel tfAddLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.add"),
        ImageUtils.createImageIcon("/img/icon_plus.png", 20,20),
        JLabel.LEFT);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 10;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(tfAddLabel, c);

    JComboBox<String> tfFactoryList = new JComboBox<>();
    c.gridx = 2;
    c.gridy = 10;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(tfFactoryList, c);

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
