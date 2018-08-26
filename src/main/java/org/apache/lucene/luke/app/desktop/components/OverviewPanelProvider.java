package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.listeners.OverviewPanelListeners;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.overview.Overview;
import org.apache.lucene.luke.models.overview.OverviewFactory;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

public class OverviewPanelProvider implements Provider<JPanel> {

  private static final int GRIDX_DESC = 0;
  private static final int GRIDX_VAL = 1;
  private static final double WEIGHTX_DESC = 0.1;
  private static final double WEIGHTX_VAL = 0.9;

  private final OverviewFactory overviewFactory;

  private final Components components;

  private final Observer observer;

  private final OverviewPanelListeners listeners;

  public static class Components {

    private JPanel panel;

    private JLabel indexPathLbl;

    private JLabel numFieldsLbl;

    private JLabel numDocsLbl;

    private JLabel numTermsLbl;

    private JLabel delOptLbl;

    private JLabel indexVerLbl;

    private JLabel indexFmtLbl;

    private JLabel dirImplLbl;

    private JLabel commitPointLbl;

    private JLabel commitUserDataLbl;

  }

  public class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      Overview overviewModel = overviewFactory.newInstance(state.getIndexReader(), state.getIndexPath());
      listeners.setOverviewModel(overviewModel);

      components.indexPathLbl.setText(overviewModel.getIndexPath());
      components.numFieldsLbl.setText(Integer.toString(overviewModel.getNumFields()));
      components.numDocsLbl.setText(Integer.toString(overviewModel.getNumDeletedDocs()));
      components.numTermsLbl.setText(Long.toString(overviewModel.getNumTerms()));
      String del = overviewModel.hasDeletions() ? String.format("Yes (%d)", overviewModel.getNumDeletedDocs()) : "No";
      String opt = overviewModel.isOptimized().map(b -> b ? "Yes" : "No").orElse("?");
      components.delOptLbl.setText(String.format("%s / %s", del, opt));
      components.indexVerLbl.setText(overviewModel.getIndexVersion().map(v -> Long.toString(v)).orElse("?"));
      components.indexFmtLbl.setText(overviewModel.getIndexFormat().orElse(""));
      components.dirImplLbl.setText(overviewModel.getDirImpl().orElse(""));
      components.commitPointLbl.setText(overviewModel.getCommitDescription().orElse("---"));
      components.commitUserDataLbl.setText(overviewModel.getCommitUserData().orElse("---"));
    }

    @Override
    public void closeIndex() {
      components.indexPathLbl.setText("");
      components.numFieldsLbl.setText("");
      components.numDocsLbl.setText("");
      components.numTermsLbl.setText("");
      components.delOptLbl.setText("");
      components.indexVerLbl.setText("");
      components.indexFmtLbl.setText("");
      components.dirImplLbl.setText("");
      components.commitPointLbl.setText("");
      components.commitUserDataLbl.setText("");
    }
  }

  @Inject
  public OverviewPanelProvider(OverviewFactory overviewFactory, IndexHandler indexHandler) {
    this.overviewFactory = overviewFactory;
    this.components = new Components();
    this.observer = new Observer();
    this.listeners = new OverviewPanelListeners();

    indexHandler.addObserver(this.observer);
  }

  @Override
  public JPanel get() {
    components.panel = new JPanel(new GridLayout(1, 1));
    components.panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(0.4);
    components.panel.add(splitPane);
    return components.panel;
  }

  private JPanel createUpperPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(2, 10, 2, 2);
    c.gridy = 0;

    c.gridx = GRIDX_DESC;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_path"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.indexPathLbl = new JLabel("?");
    panel.add(components.indexPathLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_fields"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.numFieldsLbl = new JLabel("?");
    panel.add(components.numFieldsLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_docs"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.numDocsLbl = new JLabel("?");
    panel.add(components.numDocsLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_terms"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.numTermsLbl = new JLabel("?");
    panel.add(components.numTermsLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.del_opt"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.delOptLbl = new JLabel("?");
    panel.add(components.delOptLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_version"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.indexVerLbl = new JLabel("?");
    panel.add(components.indexVerLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_format"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.indexFmtLbl = new JLabel("?");
    panel.add(components.indexFmtLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.dir_impl"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.dirImplLbl = new JLabel("?");
    panel.add(components.dirImplLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.commit_point"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.commitPointLbl = new JLabel("?");
    panel.add(components.commitPointLbl, c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.commit_userdata"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    components.commitUserDataLbl = new JLabel("?");
    panel.add(components.commitUserDataLbl, c);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("overview.label.select_fields"));
    label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    panel.add(label, BorderLayout.PAGE_START);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTermCountsPanel(), createTopTermsPanel());
    splitPane.setDividerLocation(0.4);
    splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(splitPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createTermCountsPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("overview.label.available_fields"));
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    panel.add(label, BorderLayout.PAGE_START);

    TableModel tableModel = new DefaultTableModel();
    TableColumnModel columnModel = new DefaultTableColumnModel();

    String[][] data = new String[][]{};
    String[] colNames = new String[]{"Name", "Term Count", "%"};
    JTable table = new JTable(data, colNames);
    table.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createTopTermsPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));

    JPanel selectedPanel = new JPanel(new BorderLayout(0, 10));
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
    innerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    innerPanel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.selected_field")));
    JTextField selectedField = new JTextField("");
    innerPanel.add(selectedField);
    JButton showTopTermsButton = new JButton(MessageUtils.getLocalizedMessage("overview.button.show_terms"));
    innerPanel.add(showTopTermsButton);
    innerPanel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_top_terms")));
    JSpinner spinner = new JSpinner();
    innerPanel.add(spinner);
    selectedPanel.add(innerPanel, BorderLayout.PAGE_START);

    JPanel termsPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("overview.label.top_terms"));
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    termsPanel.add(label, BorderLayout.PAGE_START);

    String[][] data = new String[][]{};
    String[] colNames = new String[]{"Rank", "Freq", "Text"};
    JTable table = new JTable(data, colNames);
    table.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(table);
    termsPanel.add(scrollPane, BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, selectedPanel, termsPanel);
    splitPane.setDividerLocation(0.3);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    panel.add(splitPane);

    return panel;
  }

}
