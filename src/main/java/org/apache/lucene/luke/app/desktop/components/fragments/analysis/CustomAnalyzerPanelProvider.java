package org.apache.lucene.luke.app.desktop.components.fragments.analysis;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.components.AnalysisPanelProvider;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.EditFiltersDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.EditParamsDialogFactory;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.FontUtil;
import org.apache.lucene.luke.app.desktop.util.ListUtil;
import org.apache.lucene.luke.app.desktop.util.lang.Callable;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.analysis.CustomAnalyzerConfig;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CustomAnalyzerPanelProvider implements Provider<JPanel> {

  private final ComponentOperatorRegistry operatorRegistry;

  private final EditParamsDialogFactory editParamsDialogFactory;

  private final EditFiltersDialogFactory editFiltersDialogFactory;

  private final MessageBroker messageBroker;

  private final JTextField confDirTF = new JTextField();

  private final JFileChooser fileChooser = new JFileChooser();

  private final JButton confDirBtn = new JButton();

  private final JButton buildBtn = new JButton();

  private final JLabel loadJarLbl = new JLabel();

  private final JList<String> selectedCfList = new JList<>(new String[]{});

  private final JButton cfEditBtn = new JButton();

  private final JComboBox<String> cfFactoryCombo = new JComboBox<>();

  private final JTextField selectedTokTF = new JTextField();

  private final JButton tokEditBtn = new JButton();

  private final JComboBox<String> tokFactoryCombo = new JComboBox<>();

  private final JList<String> selectedTfList = new JList<>(new String[]{});

  private final JButton tfEditBtn = new JButton();

  private final JComboBox<String> tfFactoryCombo = new JComboBox<>();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private final List<Map<String, String>> cfParamsList = new ArrayList<>();

  private final Map<String, String> tokParams = new HashMap<>();

  private final List<Map<String, String>> tfParamsList = new ArrayList<>();

  private JPanel containerPanel;

  class ListenerFunctions {

    void chooseConfigDir(ActionEvent e) {
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int ret = fileChooser.showOpenDialog(containerPanel);
      if (ret == JFileChooser.APPROVE_OPTION) {
        File dir = fileChooser.getSelectedFile();
        confDirTF.setText(dir.getAbsolutePath());
      } else {
        // do nothing
      }
    }

    void loadExternalJars(MouseEvent e) {
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setMultiSelectionEnabled(true);

      int ret = fileChooser.showOpenDialog(containerPanel);
      if (ret == JFileChooser.APPROVE_OPTION) {
        File[] files = fileChooser.getSelectedFiles();
        operatorRegistry.get(AnalysisPanelProvider.AnalysisPanelOperator.class).ifPresent(operator -> {
            operator.addExternalJars(Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.toList()));
        });
        messageBroker.showStatusMessage("External jars were added.");
      }
    }


    void buildAnalyzer(ActionEvent e) {
      List<String> charFilterFactories = ListUtil.getAllItems(selectedCfList);
      assert charFilterFactories.size() == cfParamsList.size();

      List<String> tokenFilterFactories = ListUtil.getAllItems(selectedTfList);
      assert tokenFilterFactories.size() == tfParamsList.size();

      CustomAnalyzerConfig.Builder builder =
          new CustomAnalyzerConfig.Builder(selectedTokTF.getText(), tokParams).configDir(confDirTF.getText());
      IntStream.range(0, charFilterFactories.size()).forEach(i ->
          builder.addCharFilterConfig(charFilterFactories.get(i), cfParamsList.get(i)));
      IntStream.range(0, tokenFilterFactories.size()).forEach(i ->
          builder.addTokenFilterConfig(tokenFilterFactories.get(i), tfParamsList.get(i)));
      CustomAnalyzerConfig config = builder.build();

      operatorRegistry.get(AnalysisPanelProvider.AnalysisPanelOperator.class).ifPresent(operator -> {
        operator.setAnalyzerByCustomConfiguration(config);
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("analysis.message.build_success"));
        buildBtn.setEnabled(false);
      });

    }

    void addCharFilter(ActionEvent e) {
      if (Objects.isNull(cfFactoryCombo.getSelectedItem()) || cfFactoryCombo.getSelectedItem() == "") {
        return;
      }

      int targetIndex = selectedCfList.getModel().getSize();
      String selectedItem = (String)cfFactoryCombo.getSelectedItem();
      List<String> updatedList = ListUtil.getAllItems(selectedCfList);
      updatedList.add(selectedItem);
      cfParamsList.add(new HashMap<>());

      assert selectedCfList.getModel().getSize() == cfParamsList.size();

      showEditParamsDialog(MessageUtils.getLocalizedMessage("analysis.dialog.title.char_filter_params"),
          EditParamsDialogFactory.EditParamsMode.CHARFILTER, targetIndex, selectedItem, cfParamsList.get(cfParamsList.size()-1),
          () -> {
            selectedCfList.setModel(new DefaultComboBoxModel<>(updatedList.toArray(new String[0])));
            cfFactoryCombo.setSelectedItem("");
            cfEditBtn.setEnabled(true);
            buildBtn.setEnabled(true);
          });
    }

    void setTokenizer(ActionEvent e) {
      if (Objects.isNull(tokFactoryCombo.getSelectedItem()) || tokFactoryCombo.getSelectedItem() == "") {
        return;
      }

      String selectedItem = (String)tokFactoryCombo.getSelectedItem();
      showEditParamsDialog(MessageUtils.getLocalizedMessage("analysis.dialog.title.tokenizer_params"),
          EditParamsDialogFactory.EditParamsMode.TOKENIZER,-1, selectedItem, Collections.emptyMap(),
          () -> {
            selectedTokTF.setText(selectedItem);
            tokFactoryCombo.setSelectedItem("");
            buildBtn.setEnabled(true);
          });
    }

    void addTokenFilter(ActionEvent e) {
      if (Objects.isNull(tfFactoryCombo.getSelectedItem()) || tfFactoryCombo.getSelectedItem() == "") {
        return;
      }

      int targetIndex = selectedTfList.getModel().getSize();
      String selectedItem = (String)tfFactoryCombo.getSelectedItem();
      List<String> updatedList = ListUtil.getAllItems(selectedTfList);
      updatedList.add(selectedItem);
      tfParamsList.add(new HashMap<>());

      assert selectedTfList.getModel().getSize() == tfParamsList.size();

      showEditParamsDialog(MessageUtils.getLocalizedMessage("analysis.dialog.title.token_filter_params"),
          EditParamsDialogFactory.EditParamsMode.TOKENFILTER, targetIndex, selectedItem, tfParamsList.get(tfParamsList.size()-1),
          () -> {
            selectedTfList.setModel(new DefaultComboBoxModel<>(updatedList.toArray(new String[updatedList.size()])));
            tfFactoryCombo.setSelectedItem("");
            tfEditBtn.setEnabled(true);
            buildBtn.setEnabled(true);
          });
    }

    private void showEditParamsDialog(String title, EditParamsDialogFactory.EditParamsMode mode, int targetIndex, String selectedItem, Map<String, String> params, Callable callback) {
      new DialogOpener<>(editParamsDialogFactory).open(title, 400, 300,
          (factory) -> {
            factory.setMode(mode);
            factory.setTargetIndex(targetIndex);
            factory.setTarget(selectedItem);
            factory.setParams(params);
            factory.setCallback(callback);
          });
    }

    void editCharFilters(ActionEvent e) {
      List<String> filters = ListUtil.getAllItems(selectedCfList);
      showEditFiltersDialog(EditFiltersDialogFactory.EditFiltersMode.CHARFILTER, filters,
          () -> {
            cfEditBtn.setEnabled(selectedCfList.getModel().getSize() > 0);
            buildBtn.setEnabled(true);
          });
    }

    void editTokenizer(ActionEvent e) {
      String selectedItem = selectedTokTF.getText();
      showEditParamsDialog(MessageUtils.getLocalizedMessage("analysis.dialog.title.tokenizer_params"),
          EditParamsDialogFactory.EditParamsMode.TOKENIZER,-1, selectedItem, tokParams, () -> {
            buildBtn.setEnabled(true);
          });
    }

    void editTokenFilters(ActionEvent e) {
      List<String> filters = ListUtil.getAllItems(selectedTfList);
      showEditFiltersDialog(EditFiltersDialogFactory.EditFiltersMode.TOKENFILTER, filters,
          () -> {
            tfEditBtn.setEnabled(selectedTfList.getModel().getSize() > 0);
            buildBtn.setEnabled(true);
          });
    }

    private void showEditFiltersDialog(EditFiltersDialogFactory.EditFiltersMode mode, List<String> selectedFilters, Callable callback) {
      String title = (mode == EditFiltersDialogFactory.EditFiltersMode.CHARFILTER) ?
          MessageUtils.getLocalizedMessage("analysis.dialog.title.selected_char_filter") :
          MessageUtils.getLocalizedMessage("analysis.dialog.title.selected_token_filter");
      new DialogOpener<>(editFiltersDialogFactory).open(title, 400, 300,
          (factory) -> {
            factory.setMode(mode);
            factory.setSelectedFilters(selectedFilters);
            factory.setCallback(callback);
          });
    }

  }

  class CustomAnalyzerPanelOperatorImpl implements CustomAnalyzerPanelOperator {

    @Override
    public void setAvailableCharFilterFactories(Collection<Class<? extends CharFilterFactory>> charFilters) {
      String[] charFilterNames = new String[charFilters.size() + 1];
      charFilterNames[0] = "";
      System.arraycopy(charFilters.stream().map(Class::getName).toArray(String[]::new), 0, charFilterNames, 1, charFilters.size());
      cfFactoryCombo.setModel(new DefaultComboBoxModel<>(charFilterNames));
    }

    @Override
    public void setAvailableTokenizerFactories(Collection<Class<? extends TokenizerFactory>> tokenizers) {
      String[] tokenizerNames = new String[tokenizers.size() + 1];
      tokenizerNames[0] = "";
      System.arraycopy(tokenizers.stream().map(Class::getName).toArray(String[]::new), 0, tokenizerNames, 1, tokenizers.size());
      tokFactoryCombo.setModel(new DefaultComboBoxModel<>(tokenizerNames));
    }

    @Override
    public void setAvailableTokenFilterFactories(Collection<Class<? extends TokenFilterFactory>> tokenFilters) {
      String[] tokenFilterNames = new String[tokenFilters.size() + 1];
      tokenFilterNames[0] = "";
      System.arraycopy(tokenFilters.stream().map(Class::getName).toArray(String[]::new), 0, tokenFilterNames, 1, tokenFilters.size());
      tfFactoryCombo.setModel(new DefaultComboBoxModel<>(tokenFilterNames));
    }

    @Override
    public void updateCharFilters(List<Integer> deletedIndexes) {
      // update filters
      List<String> filters = ListUtil.getAllItems(selectedCfList);
      String[] updatedFilters = IntStream.range(0, filters.size())
          .filter(i -> !deletedIndexes.contains(i))
          .mapToObj(filters::get)
          .toArray(String[]::new);
      selectedCfList.setModel(new DefaultComboBoxModel<>(updatedFilters));
      // update parameters map for each filter
      List<Map<String, String>> updatedParamList = IntStream.range(0, cfParamsList.size())
          .filter(i -> !deletedIndexes.contains(i))
          .mapToObj(cfParamsList::get)
          .collect(Collectors.toList());
      cfParamsList.clear();
      cfParamsList.addAll(updatedParamList);
      assert selectedCfList.getModel().getSize() == cfParamsList.size();
    }

    @Override
    public void updateTokenFilters(List<Integer> deletedIndexes) {
      // update filters
      List<String> filters = ListUtil.getAllItems(selectedTfList);
      String[] updatedFilters = IntStream.range(0, filters.size())
          .filter(i -> !deletedIndexes.contains(i))
          .mapToObj(filters::get)
          .toArray(String[]::new);
      selectedTfList.setModel(new DefaultComboBoxModel<>(updatedFilters));
      // update parameters map for each filter
      List<Map<String, String>> updatedParamList = IntStream.range(0, tfParamsList.size())
          .filter(i -> !deletedIndexes.contains(i))
          .mapToObj(tfParamsList::get)
          .collect(Collectors.toList());
      tfParamsList.clear();
      tfParamsList.addAll(updatedParamList);
      assert selectedTfList.getModel().getSize() == tfParamsList.size();
    }

    @Override
    public Map<String, String> getCharFilterParams(int index) {
      if (index < 0 || index > cfParamsList.size()) {
        throw new IllegalArgumentException();
      }
      return ImmutableMap.copyOf(cfParamsList.get(index));
    }

    @Override
    public void updateCharFilterParams(int index, Map<String, String> updatedParams) {
      if (index < 0 || index > cfParamsList.size()) {
        throw new IllegalArgumentException();
      }
      if (index == cfParamsList.size()) {
        cfParamsList.add(new HashMap<>());
      }
      cfParamsList.get(index).clear();
      cfParamsList.get(index).putAll(updatedParams);
    }

    @Override
    public void updateTokenizerParams(Map<String, String> updatedParams) {
      tokParams.clear();
      tokParams.putAll(updatedParams);
    }

    @Override
    public Map<String, String> getTokenFilterParams(int index) {
      if (index < 0 || index > tfParamsList.size()) {
        throw new IllegalArgumentException();
      }
      return ImmutableMap.copyOf(tfParamsList.get(index));
    }

    @Override
    public void updateTokenFilterParams(int index, Map<String, String> updatedParams) {
      if (index < 0 || index > tfParamsList.size()) {
        throw new IllegalArgumentException();
      }
      if (index == tfParamsList.size()) {
        tfParamsList.add(new HashMap<>());
      }
      tfParamsList.get(index).clear();
      tfParamsList.get(index).putAll(updatedParams);
    }
  }

  @Inject
  public CustomAnalyzerPanelProvider(ComponentOperatorRegistry operatorRegistry,
                                     EditParamsDialogFactory editParamsDialogFactory,
                                     EditFiltersDialogFactory editFiltersDialogFactory,
                                     MessageBroker messageBroker) {
    this.operatorRegistry = operatorRegistry;
    this.editParamsDialogFactory = editParamsDialogFactory;
    this.editFiltersDialogFactory = editFiltersDialogFactory;
    this.messageBroker = messageBroker;

    operatorRegistry.register(CustomAnalyzerPanelOperator.class, new CustomAnalyzerPanelOperatorImpl());

    cfFactoryCombo.addActionListener(listeners::addCharFilter);
    tokFactoryCombo.addActionListener(listeners::setTokenizer);
    tfFactoryCombo.addActionListener(listeners::addTokenFilter);
  }

  @Override
  public JPanel get() {
    if (containerPanel == null) {
      containerPanel = new JPanel();
      containerPanel.setLayout(new BorderLayout());
      containerPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

      containerPanel.add(createCustomAnalyzerHeader(), BorderLayout.PAGE_START);
      containerPanel.add(createCustomAnalyzerChain(), BorderLayout.CENTER);
    }

    return containerPanel;
  }

  private JPanel createCustomAnalyzerHeader() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.label.config_dir")));
    confDirTF.setColumns(30);
    panel.add(confDirTF);
    confDirBtn.setText(MessageUtils.getLocalizedMessage("analysis.button.browse"));
    confDirBtn.setIcon(ImageUtils.createImageIcon("/img/icon_folder-open_alt.png", 20, 20));
    confDirBtn.setFont(new Font(confDirBtn.getFont().getFontName(), Font.PLAIN, 15));
    confDirBtn.setMargin(new Insets(3, 3, 3, 3));
    confDirBtn.addActionListener(listeners::chooseConfigDir);
    panel.add(confDirBtn);
    buildBtn.setText(MessageUtils.getLocalizedMessage("analysis.button.build_analyzser"));
    buildBtn.setIcon(ImageUtils.createImageIcon("/img/icon_puzzle.png", 20, 20));
    buildBtn.setFont(new Font(buildBtn.getFont().getFontName(), Font.PLAIN, 15));
    buildBtn.setMargin(new Insets(3, 3, 3, 3));
    buildBtn.addActionListener(listeners::buildAnalyzer);
    panel.add(buildBtn);
    loadJarLbl.setText(MessageUtils.getLocalizedMessage("analysis.hyperlink.load_jars"));
    loadJarLbl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        listeners.loadExternalJars(e);
      }
    });
    panel.add(FontUtil.toLinkText(loadJarLbl));

    return panel;
  }

  private JPanel createCustomAnalyzerChain() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,3));

    panel.add(createCustomChainConfig());

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
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(cfLbl, c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    selectedCfList.setVisibleRowCount(1);
    selectedCfList.setFont(new Font(selectedCfList.getFont().getFontName(), Font.PLAIN, 15));
    JScrollPane selectedPanel = new JScrollPane(selectedCfList);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(selectedPanel, c);

    cfEditBtn.setText(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"));
    cfEditBtn.setIcon(ImageUtils.createImageIcon("/img/icon_pencil.png", 15, 15));
    cfEditBtn.setMargin(new Insets(2, 4, 2, 4));
    cfEditBtn.setEnabled(false);
    cfEditBtn.addActionListener(listeners::editCharFilters);
    c.fill = GridBagConstraints.NONE;
    c.gridx = 7;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(cfEditBtn, c);

    JLabel cfAddLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.add"),
        ImageUtils.createImageIcon("/img/icon_plus.png", 15,15),
        JLabel.LEFT);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(cfAddLabel, c);

    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(cfFactoryCombo, c);

    // separator
    sepc.gridx = 0;
    sepc.gridy = 3;
    sepc.anchor = GridBagConstraints.LINE_START;
    panel.add(new JSeparator(JSeparator.HORIZONTAL), sepc);

    // tokenizer
    JLabel tokLabel = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.tokenizer"));
    tokLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tokLabel, c);

    c.gridx = 1;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    selectedTokTF.setColumns(15);
    selectedTokTF.setFont(new Font(selectedTokTF.getFont().getFontName(), Font.PLAIN, 15));
    selectedTokTF.setText(StandardTokenizerFactory.class.getName());
    selectedTokTF.setEditable(false);
    c.gridx = 2;
    c.gridy = 4;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(selectedTokTF, c);

    tokEditBtn.setText(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"));
    tokEditBtn.setIcon(ImageUtils.createImageIcon("/img/icon_pencil.png", 15, 15));
    tokEditBtn.setMargin(new Insets(2, 4, 2, 4));
    tokEditBtn.addActionListener(listeners::editTokenizer);
    c.fill = GridBagConstraints.NONE;
    c.gridx = 7;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tokEditBtn, c);

    JLabel setTokLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.set"),
        ImageUtils.createImageIcon("/img/icon_pushpin_alt.png", 15,15),
        JLabel.LEFT);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(setTokLabel, c);

    c.gridx = 2;
    c.gridy = 6;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(tokFactoryCombo, c);

    // separator
    sepc.gridx = 0;
    sepc.gridy = 7;
    sepc.anchor = GridBagConstraints.LINE_START;
    panel.add(new JSeparator(JSeparator.HORIZONTAL), sepc);

    // token filters
    JLabel tfLbl = new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.tokenfilters"));
    tfLbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    c.gridx = 0;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tfLbl, c);

    c.gridx = 1;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("analysis_custom.label.selected")), c);

    selectedTfList.setVisibleRowCount(1);
    selectedTfList.setFont(new Font(selectedTfList.getFont().getFontName(), Font.PLAIN, 15));
    JScrollPane selectedTfPanel = new JScrollPane(selectedTfList);
    c.gridx = 2;
    c.gridy = 8;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(selectedTfPanel, c);

    tfEditBtn.setText(MessageUtils.getLocalizedMessage("analysis_custom.label.edit"));
    tfEditBtn.setIcon(ImageUtils.createImageIcon("/img/icon_pencil.png", 15, 15));
    tfEditBtn.setMargin(new Insets(2, 4, 2, 4));
    tfEditBtn.setEnabled(false);
    tfEditBtn.addActionListener(listeners::editTokenFilters);
    c.fill = GridBagConstraints.NONE;
    c.gridx = 7;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(tfEditBtn, c);

    JLabel tfAddLabel = new JLabel(
        MessageUtils.getLocalizedMessage("analysis_custom.label.add"),
        ImageUtils.createImageIcon("/img/icon_plus.png", 15,15),
        JLabel.LEFT);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 10;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weightx = 0.1;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(tfAddLabel, c);

    c.gridx = 2;
    c.gridy = 10;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(tfFactoryCombo, c);

    return panel;
  }

  public interface CustomAnalyzerPanelOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setAvailableCharFilterFactories(Collection<Class<? extends CharFilterFactory>> charFilters);
    void setAvailableTokenizerFactories(Collection<Class<? extends TokenizerFactory>> tokenizers);
    void setAvailableTokenFilterFactories(Collection<Class<? extends TokenFilterFactory>> tokenFilters);

    void updateCharFilters(List<Integer> deletedIndexes);
    void updateTokenFilters(List<Integer> deletedIndexes);

    Map<String, String> getCharFilterParams(int index);
    void updateCharFilterParams(int index, Map<String, String> updatedParams);
    void updateTokenizerParams(Map<String, String> updatedParams);
    Map<String, String> getTokenFilterParams(int index);
    void updateTokenFilterParams(int index, Map<String, String> updatedParams);
  }
}
