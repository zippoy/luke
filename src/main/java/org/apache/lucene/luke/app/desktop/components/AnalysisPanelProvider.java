package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.PresetAnalyzerPaneProvider;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.analysis.Analysis;
import org.apache.lucene.luke.models.analysis.AnalysisFactory;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;


public class AnalysisPanelProvider implements Provider<JPanel> {

  private static final String TYPE_PRESET = "preset";

  private static final String TYPE_CUSTOM = "custom";

  private final JPanel mainPanel = new JPanel();

  private final JPanel preset;

  private final JPanel custom;

  private final JRadioButton presetRB = new JRadioButton();

  private final JRadioButton customRB  = new JRadioButton();

  private final JLabel analyzerNameLbl = new JLabel();

  private final JTextArea inputArea = new JTextArea();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private Analysis analysisModel;

  class AnalysisPanelOperatorImpl implements AnalysisPanelOperator {

    @Override
    public void setAnalyzerByType(String analyzerType) {
      analysisModel.createAnalyzerFromClassName(analyzerType);
      analyzerNameLbl.setText(analysisModel.currentAnalyzer().getClass().getName());
    }

  }

  class ListenerFunctions {

    void toggleMainPanel(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase(TYPE_PRESET)) {
        mainPanel.remove(custom);
        mainPanel.add(preset, BorderLayout.CENTER);
      } else if (e.getActionCommand().equalsIgnoreCase(TYPE_CUSTOM)) {
        mainPanel.remove(preset);
        mainPanel.add(custom, BorderLayout.CENTER);
      }
      mainPanel.setVisible(false);
      mainPanel.setVisible(true);
    }

  }

  @Inject
  public AnalysisPanelProvider(AnalysisFactory analysisFactory,
                               ComponentOperatorRegistry operatorRegistry,
                               @Named("analysis_preset") JPanel preset,
                               @Named("analysis_custom") JPanel custom) {
    this.preset = preset;
    this.custom = custom;

    this.analysisModel = analysisFactory.newInstance();
    analysisModel.createAnalyzerFromClassName(StandardAnalyzer.class.getName());

    operatorRegistry.register(AnalysisPanelOperator.class, new AnalysisPanelOperatorImpl());

    operatorRegistry.get(PresetAnalyzerPaneProvider.PresetAnalyzerPaneOperator.class).ifPresent(operator -> {
      operator.setPresetAnalyzers(analysisModel.getPresetAnalyzerTypes());
      operator.setSelectedAnalyzer(analysisModel.currentAnalyzer().getClass());
    });
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(320);
    panel.add(splitPane);

    return panel;
  }

  private JPanel createUpperPanel() {
    mainPanel.setLayout(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    mainPanel.add(switcher(), BorderLayout.PAGE_START);
    mainPanel.add(preset, BorderLayout.CENTER);

    return mainPanel;
  }

  private JPanel switcher() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    presetRB.setText(MessageUtils.getLocalizedMessage("analysis.radio.preset"));
    presetRB.setActionCommand(TYPE_PRESET);
    presetRB.addActionListener(listeners::toggleMainPanel);
    presetRB.setSelected(true);

    customRB.setText(MessageUtils.getLocalizedMessage("analysis.radio.custom"));
    customRB.setActionCommand(TYPE_CUSTOM);
    customRB.addActionListener(listeners::toggleMainPanel);
    customRB.setSelected(false);

    ButtonGroup group = new ButtonGroup();
    group.add(presetRB);
    group.add(customRB);

    panel.add(presetRB);
    panel.add(customRB);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel inner1 = new JPanel(new BorderLayout());

    JPanel analyzerName = new JPanel(new FlowLayout(FlowLayout.LEADING));
    analyzerName.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.label.selected_analyzer")));
    analyzerNameLbl.setText(analysisModel.currentAnalyzer().getClass().getName());
    analyzerName.add(analyzerNameLbl);
    inner1.add(analyzerName, BorderLayout.PAGE_START);

    JPanel input = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
    inputArea.setRows(3);
    inputArea.setColumns(50);
    inputArea.setLineWrap(true);
    inputArea.setWrapStyleWord(true);
    inputArea.setText(MessageUtils.getLocalizedMessage("analysis.textarea.prompt"));
    input.add(new JScrollPane(inputArea));

    JButton executeBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.test"), ImageUtils.createImageIcon("/img/icon_lightbulb_alt.png", 20, 20));
    executeBtn.setFont(new Font(executeBtn.getFont().getFontName(), Font.PLAIN, 15));
    executeBtn.setMargin(new Insets(3, 3, 3, 3));
    input.add(executeBtn);

    JButton clearBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.clear"));
    clearBtn.setFont(new Font(clearBtn.getFont().getFontName(), Font.PLAIN, 15));
    clearBtn.setMargin(new Insets(5, 5, 5, 5));
    input.add(clearBtn);

    inner1.add(input, BorderLayout.CENTER);

    JPanel inner2 = new JPanel(new BorderLayout());

    JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEADING));
    hint.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.hint.show_attributes")));
    inner2.add(hint, BorderLayout.PAGE_START);

    String[][] data = new String[][]{};
    String[] columnName = new String[]{"Term", "Attributes"};
    JTable tokenTable = new JTable(data, columnName);
    tokenTable.setFillsViewportHeight(true);
    inner2.add(new JScrollPane(tokenTable), BorderLayout.CENTER);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,3 ));
    panel.add(inner1, BorderLayout.PAGE_START);
    panel.add(inner2, BorderLayout.CENTER);

    return panel;
  }

  public interface AnalysisPanelOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setAnalyzerByType(String analyzerType);
  }

}
