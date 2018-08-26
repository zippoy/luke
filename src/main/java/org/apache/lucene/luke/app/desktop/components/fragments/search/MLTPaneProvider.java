package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class MLTPaneProvider implements Provider<JScrollPane> {

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(mltParams());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(analyzerNamePane());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(fieldsSettings());

    return new JScrollPane(panel);
  }

  private JPanel mltParams() {
    JPanel panel = new JPanel(new GridLayout(3, 1));

    JPanel maxDocFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    maxDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.max_doc_freq")));
    JTextField maxDocFreqTF = new JTextField(10);
    maxDocFreq.add(maxDocFreqTF);
    panel.add(maxDocFreq);

    JPanel minDocFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.min_doc_freq")));
    JTextField minDocFreqTF = new JTextField(5);
    minDocFreq.add(minDocFreqTF);
    panel.add(minDocFreq);

    JPanel minTermFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minTermFreq.add(new JLabel(MessageUtils.getLocalizedMessage("serach_mlt.label.min_term_freq")));
    JTextField minTermFreqTF = new JTextField(5);
    minTermFreq.add(minTermFreqTF);
    panel.add(minTermFreq);

    return panel;
  }

  private JPanel analyzerNamePane() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.analyzer")));

    JLabel analyzerLabel = new JLabel("StandardAnalyzer");
    panel.add(analyzerLabel);

    JLabel changeLabel = new JLabel(MessageUtils.getLocalizedMessage("search_mlt.hyperlink.change"));
    panel.add(changeLabel);

    return panel;
  }

  private JPanel fieldsSettings() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel header = new JPanel(new GridLayout(2, 1));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.description")));
    JCheckBox loadAllCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_mlt.checkbox.select_all"));
    header.add(loadAllCB);
    panel.add(header, BorderLayout.PAGE_START);

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Select", "Field"};
    JTable fieldsTable = new JTable(data, columnNames);
    fieldsTable.setFillsViewportHeight(true);
    panel.add(new JScrollPane(fieldsTable), BorderLayout.CENTER);

    return panel;
  }

}
