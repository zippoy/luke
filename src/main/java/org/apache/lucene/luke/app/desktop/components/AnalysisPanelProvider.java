package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class AnalysisPanelProvider implements Provider<JPanel> {

  private final JPanel preset;

  private final JPanel custom;

  @Inject
  public AnalysisPanelProvider(@Named("analysis_preset") JPanel preset, @Named("analysis_custom") JPanel custom) {
    this.preset = preset;
    this.custom = custom;
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(330);
    panel.add(splitPane);

    return panel;
  }

  private JPanel createUpperPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(switcher(), BorderLayout.PAGE_START);
    panel.add(preset, BorderLayout.CENTER);
    //panel.add(custom, BorderLayout.CENTER);

    return panel;
  }

  private JPanel switcher() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    JRadioButton presetRB = new JRadioButton(MessageUtils.getLocalizedMessage("analysis.radio.preset"));
    presetRB.setActionCommand("preset");
    presetRB.setSelected(true);

    JRadioButton customRB = new JRadioButton(MessageUtils.getLocalizedMessage("analysis.radio.custom"));
    customRB.setActionCommand("custom");
    customRB.setSelected(false);

    ButtonGroup group = new ButtonGroup();
    group.add(presetRB);
    group.add(customRB);

    panel.add(presetRB);
    panel.add(customRB);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,3 ));

    JPanel analyzerName = new JPanel(new FlowLayout(FlowLayout.LEADING));
    analyzerName.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.label.selected_analyzer")));
    analyzerName.add(new JLabel("StandardAnalyzer"));
    panel.add(analyzerName);

    JPanel input = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JTextArea inputArea = new JTextArea(2, 50);
    input.add(new JScrollPane(inputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    JButton executeBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.test"), ImageUtils.createImageIcon("/img/icon_lightbulb_alt.png", 20, 20));
    input.add(executeBtn);
    JButton clearBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.clear"));
    input.add(clearBtn);
    panel.add(input);

    JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEADING));
    hint.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.hint.show_attributes")));
    panel.add(hint);

    String[][] data = new String[][]{};
    String[] columnName = new String[]{"Term", "Attributes"};
    JTable tokenTable = new JTable(data, columnName);
    tokenTable.setFillsViewportHeight(true);
    panel.add(new JScrollPane(tokenTable));

    return panel;
  }

}
