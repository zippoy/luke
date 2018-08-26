package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;


public class DocumentsPanelProvider implements IndexObserver, Provider<JPanel> {

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
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(browseTermsPanel(), c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(browseDocsByTermPanel(), c);

    return panel;
  }

  private JPanel browseTermsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("documents.label.browse_terms"));
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(3, 3, 3, 3);
    c.weightx = 0.5;
    panel.add(label, c);

    JComboBox<String> fields = new JComboBox<>(new String[]{});
    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(3, 3, 3, 3);
    c.weightx = 0.5;
    panel.add(fields, c);

    JButton firstTerm = new JButton(MessageUtils.getLocalizedMessage("documents.button.first_term"));
    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(3, 3, 3, 3);
    c.weightx = 0.5;
    panel.add(firstTerm, c);

    JTextField term = new JTextField(10);
    c.gridx = 1;
    c.gridy = 2;
    c.insets = new Insets(3, 3,3, 3);
    c.weightx = 0.3;
    panel.add(term, c);

    JButton nextTerm = new JButton(MessageUtils.getLocalizedMessage("documents.button.next"));
    c.gridx = 2;
    c.gridy = 2;
    c.insets = new Insets(3, 3, 3, 3);
    c.weightx = 0.2;
    panel.add(nextTerm, c);

    return panel;
  }

  private JPanel browseDocsByTermPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("documents.label.browse_doc_by_term"));
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.gridwidth = 2;
    c.insets = new Insets(3, 3, 3, 3);
    panel.add(label, c);

    JTextField term = new JTextField(10);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = new Insets(3, 3, 3, 3);
    panel.add(term, c);

    JButton firstDoc = new JButton(MessageUtils.getLocalizedMessage("documents.button.first_termdoc"));
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = new Insets(3, 3, 3, 1);
    panel.add(firstDoc, c);

    JTextField termDocIdx = new JTextField();
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.5;
    c.gridwidth = 1;
    c.insets = new Insets(3, 1, 3, 1);
    panel.add(termDocIdx, c);

    JButton nextDoc = new JButton(MessageUtils.getLocalizedMessage("documents.button.next"));
    c.gridx = 2;
    c.gridy = 2;
    c.weightx = 0.2;
    c.gridwidth = 1;
    c.insets = new Insets(3, 1, 3, 3);
    panel.add(nextDoc, c);

    JLabel termDocsNum = new JLabel("in ? docs");
    c.gridx = 3;
    c.gridy = 2;
    c.weightx = 0.3;
    c.gridwidth = 1;
    c.insets = new Insets(3, 3, 3, 3);
    panel.add(termDocsNum, c);

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Position", "Offsets", "Payload"};
    JTable posTable = new JTable(data, columnNames);
    posTable.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(posTable);
    scrollPane.setMinimumSize(new Dimension(100, 100));
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 4;
    c.insets = new Insets(3, 3, 3, 3);
    panel.add(scrollPane, c);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel browseDocsBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    JLabel label = new JLabel(
        MessageUtils.getLocalizedMessage("documents.label.doc_num"),
        ImageUtils.createImageIcon("/img/icon_document_alt.png", 20, 20),
        JLabel.LEFT);
    browseDocsBar.add(label);
    JSpinner spinner = new JSpinner();
    spinner.setPreferredSize(new Dimension(100, 30));
    browseDocsBar.add(spinner);
    JLabel maxDocsLabel = new JLabel("in ? docs");
    browseDocsBar.add(maxDocsLabel);
    JButton mltSearchBtn = new JButton(MessageUtils.getLocalizedMessage("documents.hyperlink.mlt"));
    browseDocsBar.add(mltSearchBtn);
    JButton addDocBtn = new JButton(
        MessageUtils.getLocalizedMessage("documents.button.add"),
        ImageUtils.createImageIcon("/img/icon_plus-box.png", 20, 20));
    browseDocsBar.add(addDocBtn);

    panel.add(browseDocsBar, BorderLayout.PAGE_START);

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Field", "Flags", "Norm", "Value"};
    JTable documentTable = new JTable(data, columnNames);
    documentTable.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(documentTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  @Override
  public void openIndex(LukeState state) {

  }

  @Override
  public void closeIndex() {

  }
}
