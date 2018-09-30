package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.TokenAttributeDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.AddDocumentDialogFactory;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.CustomAnalyzerPanelProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.PresetAnalyzerPanelProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.AnalyzerPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.MLTPaneProvider;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.analysis.Analysis;
import org.apache.lucene.luke.models.analysis.AnalysisFactory;
import org.apache.lucene.luke.models.analysis.CustomAnalyzerConfig;

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
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class AnalysisPanelProvider implements Provider<JPanel> {

  private static final String TYPE_PRESET = "preset";

  private static final String TYPE_CUSTOM = "custom";

  private final ComponentOperatorRegistry operatorRegistry;

  private final TokenAttributeDialogFactory tokenAttrDialogFactory;

  private final MessageBroker messageBroker;

  private final JPanel mainPanel = new JPanel();

  private final JPanel preset;

  private final JPanel custom;

  private final JRadioButton presetRB = new JRadioButton();

  private final JRadioButton customRB  = new JRadioButton();

  private final JLabel analyzerNameLbl = new JLabel();

  private final JTextArea inputArea = new JTextArea();

  private final JTable tokensTable = new JTable();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private List<Analysis.Token> tokens;

  private Analysis analysisModel;

  class AnalysisPanelOperatorImpl implements AnalysisPanelOperator {

    @Override
    public void setAnalyzerByType(String analyzerType) {
      analysisModel.createAnalyzerFromClassName(analyzerType);
      analyzerNameLbl.setText(analysisModel.currentAnalyzer().getClass().getName());
      operatorRegistry.get(AnalyzerPaneProvider.AnalyzerTabOperator.class).ifPresent(operator ->
          operator.setAnalyzer(analysisModel.currentAnalyzer()));
      operatorRegistry.get(MLTPaneProvider.MLTTabOperator.class).ifPresent(operator ->
          operator.setAnalyzer(analysisModel.currentAnalyzer()));
      operatorRegistry.get(AddDocumentDialogFactory.AddDocumentDialogOperator.class).ifPresent(operator ->
          operator.setAnalyzer(analysisModel.currentAnalyzer()));
    }

    @Override
    public void setAnalyzerByCustomConfiguration(CustomAnalyzerConfig config) {
      analysisModel.buildCustomAnalyzer(config);
      analyzerNameLbl.setText(analysisModel.currentAnalyzer().getClass().getName());
      operatorRegistry.get(AnalyzerPaneProvider.AnalyzerTabOperator.class).ifPresent(operator ->
          operator.setAnalyzer(analysisModel.currentAnalyzer()));
      operatorRegistry.get(MLTPaneProvider.MLTTabOperator.class).ifPresent(operator ->
          operator.setAnalyzer(analysisModel.currentAnalyzer()));
      operatorRegistry.get(AddDocumentDialogFactory.AddDocumentDialogOperator.class).ifPresent(operator ->
          operator.setAnalyzer(analysisModel.currentAnalyzer()));
    }

    @Override
    public void addExternalJars(List<String> jarFiles) {
      analysisModel.addExternalJars(jarFiles);
      operatorRegistry.get(CustomAnalyzerPanelProvider.CustomAnalyzerPanelOperator.class).ifPresent(operator -> {
        operator.setAvailableCharFilterFactories(analysisModel.getAvailableCharFilterFactories());
        operator.setAvailableTokenizerFactories(analysisModel.getAvailableTokenizerFactories());
        operator.setAvailableTokenFilterFactories(analysisModel.getAvailableTokenFilterFactories());
      });
    }

    @Override
    public Analyzer getCurrentAnalyzer() {
      return analysisModel.currentAnalyzer();
    }
  }

  class ListenerFunctions {

    void toggleMainPanel(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase(TYPE_PRESET)) {
        mainPanel.remove(custom);
        mainPanel.add(preset, BorderLayout.CENTER);

        operatorRegistry.get(PresetAnalyzerPanelProvider.PresetAnalyzerPaneOperator.class).ifPresent(operator -> {
          operator.setPresetAnalyzers(analysisModel.getPresetAnalyzerTypes());
          operator.setSelectedAnalyzer(analysisModel.currentAnalyzer().getClass());
        });

      } else if (e.getActionCommand().equalsIgnoreCase(TYPE_CUSTOM)) {
        mainPanel.remove(preset);
        mainPanel.add(custom, BorderLayout.CENTER);

        operatorRegistry.get(CustomAnalyzerPanelProvider.CustomAnalyzerPanelOperator.class).ifPresent(operator -> {
          operator.setAvailableCharFilterFactories(analysisModel.getAvailableCharFilterFactories());
          operator.setAvailableTokenizerFactories(analysisModel.getAvailableTokenizerFactories());
          operator.setAvailableTokenFilterFactories(analysisModel.getAvailableTokenFilterFactories());
        });
      }
      mainPanel.setVisible(false);
      mainPanel.setVisible(true);
    }

    void executeAnalysis(ActionEvent e) {
      String text = inputArea.getText();
      if (Objects.isNull(text) || text.isEmpty()) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("analysis.message.empry_input"));
      }

      tokens = analysisModel.analyze(text);
      tokensTable.setModel(new TokenTableModel(tokens));
      tokensTable.setShowGrid(true);
      tokensTable.getColumnModel().getColumn(TokenTableModel.Column.TERM.getIndex()).setPreferredWidth(150);
      tokensTable.getColumnModel().getColumn(TokenTableModel.Column.ATTR.getIndex()).setPreferredWidth(1000);
    }

    void showAttributeValues(MouseEvent e) {
      if (e.getClickCount() != 2 || e.isConsumed()) {
        return;
      }
      int selectedIndex = tokensTable.rowAtPoint(e.getPoint());
      if (selectedIndex < 0 || selectedIndex >= tokensTable.getRowCount()) {
        return;
      }

      String term = tokens.get(selectedIndex).getTerm();
      List<Analysis.TokenAttribute> attributes = tokens.get(selectedIndex).getAttributes();
      new DialogOpener<>(tokenAttrDialogFactory).open("Token Attributes", 650, 400,
          factory -> {
            factory.setTerm(term);
            factory.setAttributes(attributes);
          });
    }

  }

  @Inject
  public AnalysisPanelProvider(AnalysisFactory analysisFactory,
                               ComponentOperatorRegistry operatorRegistry,
                               TokenAttributeDialogFactory tokenAttrDialogFactory,
                               MessageBroker messageBroker,
                               @Named("analysis_preset") JPanel preset,
                               @Named("analysis_custom") JPanel custom) {
    this.preset = preset;
    this.custom = custom;

    this.operatorRegistry = operatorRegistry;
    this.tokenAttrDialogFactory = tokenAttrDialogFactory;
    this.messageBroker = messageBroker;

    this.analysisModel = analysisFactory.newInstance();
    analysisModel.createAnalyzerFromClassName(StandardAnalyzer.class.getName());

    operatorRegistry.register(AnalysisPanelOperator.class, new AnalysisPanelOperatorImpl());

    operatorRegistry.get(PresetAnalyzerPanelProvider.PresetAnalyzerPaneOperator.class).ifPresent(operator -> {
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
    executeBtn.addActionListener(listeners::executeAnalysis);
    input.add(executeBtn);

    JButton clearBtn = new JButton(MessageUtils.getLocalizedMessage("analysis.button.clear"));
    clearBtn.setFont(new Font(clearBtn.getFont().getFontName(), Font.PLAIN, 15));
    clearBtn.setMargin(new Insets(5, 5, 5, 5));
    clearBtn.addActionListener(e -> inputArea.setText(""));
    input.add(clearBtn);

    inner1.add(input, BorderLayout.CENTER);

    JPanel inner2 = new JPanel(new BorderLayout());

    JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEADING));
    hint.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.hint.show_attributes")));
    inner2.add(hint, BorderLayout.PAGE_START);


    TableUtil.setupTable(tokensTable, ListSelectionModel.SINGLE_SELECTION, new TokenTableModel(), new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        listeners.showAttributeValues(e);
      }
    }, 150, 800);
    inner2.add(new JScrollPane(tokensTable), BorderLayout.CENTER);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,3 ));
    panel.add(inner1, BorderLayout.PAGE_START);
    panel.add(inner2, BorderLayout.CENTER);

    return panel;
  }

  public interface AnalysisPanelOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setAnalyzerByType(String analyzerType);
    void setAnalyzerByCustomConfiguration(CustomAnalyzerConfig config);
    void addExternalJars(List<String> jarFiles);
    Analyzer getCurrentAnalyzer();
  }

}

class TokenTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    TERM("Term", 0, String.class),
    ATTR("Attributes", 1, String.class);

    private String colName;
    private int index;
    private Class<?> type;

    Column(String colName, int index, Class<?> type) {
      this.colName = colName;
      this.index = index;
      this.type = type;
    }

    @Override
    public String getColName() {
      return colName;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public Class<?> getType() {
      return type;
    }
  }

  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  TokenTableModel() {
    this.data = new Object[0][colNames.length];
  }

  TokenTableModel(List<Analysis.Token> tokens) {
    this.data = new Object[tokens.size()][colNames.length];
    for (int i = 0; i < tokens.size(); i++) {
      Analysis.Token token = tokens.get(i);
      data[i][Column.TERM.getIndex()] = token.getTerm();
      List<String> attValues = token.getAttributes().stream()
          .flatMap(att -> att.getAttValues().entrySet().stream()
              .map(e -> e.getKey() + "=" + e.getValue()))
          .collect(Collectors.toList());
      data[i][Column.ATTR.getIndex()] = String.join(",", attValues);
    }
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public int getColumnCount() {
    return colNames.length;
  }

  @Override
  public String getColumnName(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).colName;
    }
    return "";
  }

  @Override
  public Class<?> getColumnClass(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).type;
    }
    return Object.class;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data[rowIndex][columnIndex];
  }

}