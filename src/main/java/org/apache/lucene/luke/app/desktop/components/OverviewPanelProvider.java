package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class OverviewPanelProvider implements IndexObserver, Provider<JPanel> {

  private static final int GRIDX_DESC = 0;
  private static final int GRIDX_VAL = 1;
  private static final double WEIGHTX_DESC = 0.1;
  private static final double WEIGHTX_VAL = 0.9;

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(0.4);
    panel.add(splitPane);
    return panel;
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
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_fields"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_docs"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.num_terms"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.del_opt"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_version"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.index_format"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.dir_impl"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.commit_point"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

    c.gridx = GRIDX_DESC;
    c.gridy += 1;
    c.weightx = WEIGHTX_DESC;
    panel.add(new JLabel(MessageUtils.getLocalizedMessage("overview.label.commit_userdata"), JLabel.RIGHT), c);

    c.gridx = GRIDX_VAL;
    c.weightx = WEIGHTX_VAL;
    panel.add(new JLabel("?"), c);

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

  @Override
  public void openIndex(LukeState state) {

  }

  @Override
  public void closeIndex() {

  }
}
