package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class SearchPanelProvider implements IndexObserver, Provider<JPanel> {

  private JScrollPane qparser;

  private JScrollPane analyzer;

  private JScrollPane similarity;

  private JScrollPane sort;

  private JScrollPane values;

  private JScrollPane mlt;

  @Inject
  public SearchPanelProvider(@Named("search_qparser") JScrollPane qparser,
                             @Named("search_analyzer") JScrollPane analyzer,
                             @Named("search_similarity") JScrollPane similarity,
                             @Named("search_sort") JScrollPane sort,
                             @Named("search_values") JScrollPane values,
                             @Named("search_mlt") JScrollPane mlt) {
    this.qparser = qparser;
    this.analyzer = analyzer;
    this.similarity = similarity;
    this.sort = sort;
    this.values = values;
    this.mlt = mlt;
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(350);
    panel.add(splitPane);

    return panel;
  }

  private JSplitPane createUpperPanel() {
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createQuerySettingsPane(), createQueryPane());
    splitPane.setDividerLocation(550);
    return splitPane;
  }

  private JPanel createQuerySettingsPane() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("search.label.settings"));
    panel.add(label, BorderLayout.PAGE_START);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Query Parser", qparser);
    tabbedPane.addTab("Analyzer", analyzer);
    tabbedPane.addTab("Similarity", similarity);
    tabbedPane.addTab("Sort", sort);
    tabbedPane.addTab("Field Values", values);
    tabbedPane.addTab("More Like This", mlt);
    panel.add(tabbedPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createQueryPane() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;

    JLabel labelQE = new JLabel(MessageUtils.getLocalizedMessage("search.label.expression"));
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.weightx = 0.5;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(labelQE, c);

    JPanel termQuery = new JPanel();
    JCheckBox termQueryBtn = new JCheckBox();
    termQuery.add(termQueryBtn);
    JLabel labelTQ = new JLabel(MessageUtils.getLocalizedMessage("search.checkbox.term"));
    termQuery.add(labelTQ);
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.2;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(termQuery, c);

    JTextArea textQE = new JTextArea();
    textQE.setRows(4);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 3;
    c.weightx = 0.0;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(textQE, c);

    JLabel labelPQ = new JLabel(MessageUtils.getLocalizedMessage("search.label.parsed"));
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 3;
    c.weightx = 0.0;
    c.insets = new Insets(8, 0, 2, 0);
    panel.add(labelPQ, c);

    JTextArea textPQ = new JTextArea();
    textPQ.setRows(4);
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 3;
    c.weightx = 0.0;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(textPQ, c);

    JButton parse = new JButton(MessageUtils.getLocalizedMessage("search.button.parse"),
        ImageUtils.createImageIcon("/img/icon_flowchart_alt.png", 20, 20));
    parse.setFont(new Font(parse.getFont().getFontName(), Font.PLAIN, 15));
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.weightx = 0.2;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(parse, c);

    JPanel rewrite = new JPanel(new FlowLayout());
    JCheckBox rewriteCB = new JCheckBox();
    rewrite.add(rewriteCB);
    JLabel rewriteLabel = new JLabel(MessageUtils.getLocalizedMessage("search.checkbox.rewrite"));
    rewrite.add(rewriteLabel);
    c.gridx = 1;
    c.gridy = 4;
    c.gridwidth = 2;
    c.weightx = 0.2;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(rewrite, c);

    JButton searchBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.search"),
        ImageUtils.createImageIcon("/img/icon_search2.png", 20, 20));
    searchBtn.setFont(new Font(searchBtn.getFont().getFontName(), Font.PLAIN, 15));
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.insets = new Insets(2, 0, 2, 0);
    panel.add(searchBtn, c);

    JButton mltBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.mlt"),
        ImageUtils.createImageIcon("/img/icon_heart_alt.png", 20, 20));
    mltBtn.setFont(new Font(mltBtn.getFont().getFontName(), Font.PLAIN, 15));
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.weightx = 0.3;
    c.insets = new Insets(8, 0, 0, 0);
    panel.add(mltBtn, c);

    JPanel docNo = new JPanel(new FlowLayout());
    JLabel docNoLabel = new JLabel("with doc #");
    docNo.add(docNoLabel);
    JTextField docNoField = new JTextField(3);
    docNo.add(docNoField);
    c.gridx = 1;
    c.gridy = 6;
    c.gridwidth = 2;
    c.weightx = 0.3;
    c.insets = new Insets(8, 0, 0, 0);
    panel.add(docNo, c);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(createSearchResultsHeaderPane(), BorderLayout.PAGE_START);
    panel.add(createSearchResultsTablePane(), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSearchResultsHeaderPane() {
    JPanel panel = new JPanel(new GridLayout(1, 2));

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("search.label.results"),
        ImageUtils.createImageIcon("/img/icon_table.png", 20, 20),
        JLabel.LEFT);
    panel.add(label);

    JPanel resultsInfo = new JPanel(new FlowLayout());

    JLabel totalLabel = new JLabel(MessageUtils.getLocalizedMessage("search.label.total"));
    resultsInfo.add(totalLabel);

    JLabel totalDocs = new JLabel("10");
    resultsInfo.add(totalDocs);

    JButton prevBtn = new JButton(ImageUtils.createImageIcon("/img/arrow_triangle-left.png", 20, 20));
    resultsInfo.add(prevBtn);

    JLabel start = new JLabel("");
    resultsInfo.add(start);

    resultsInfo.add(new JLabel(" ~ "));

    JLabel end = new JLabel("");
    resultsInfo.add(end);

    JButton nextBtn = new JButton(ImageUtils.createImageIcon("/img/arrow_triangle-right.png", 20, 20));
    resultsInfo.add(nextBtn);

    JSeparator sep = new JSeparator(JSeparator.VERTICAL);
    sep.setPreferredSize(new Dimension(5, 1));
    resultsInfo.add(sep);

    JButton delBtn = new JButton(MessageUtils.getLocalizedMessage("search.button.del_all"), ImageUtils.createImageIcon("/img/icon_trash.png", 20, 20));
    resultsInfo.add(delBtn);

    panel.add(resultsInfo, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSearchResultsTablePane() {
    JPanel panel = new JPanel(new GridLayout(1, 1));

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Doc ID", "Score", "Field Values"};
    JTable table = new JTable(data, columnNames);
    table.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane);

    return panel;
  }

  @Override
  public void openIndex(LukeState state) {

  }

  @Override
  public void closeIndex() {

  }
}
