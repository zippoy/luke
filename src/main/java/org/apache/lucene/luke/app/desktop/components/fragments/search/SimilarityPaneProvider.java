package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class SimilarityPaneProvider implements Provider<JScrollPane> {

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(similaritySettings());

    return new JScrollPane(panel);
  }

  private JPanel similaritySettings() {
    JPanel panel = new JPanel(new GridLayout(4, 1));
    panel.setMaximumSize(new Dimension(500, 180));

    JCheckBox tfidfCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_similarity.checkbox.use_classic"));
    panel.add(tfidfCB);

    JCheckBox discardOverlapsCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_similarity.checkbox.discount_overlaps"));
    panel.add(discardOverlapsCB);

    JLabel bm25Label = new JLabel(MessageUtils.getLocalizedMessage("search_similarity.label.bm25_params"));
    panel.add(bm25Label);

    JPanel bm25Params = new JPanel(new FlowLayout(FlowLayout.LEADING));
    bm25Params.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    bm25Params.add(new JLabel("k1"));
    JTextField k1TF = new JTextField(5);
    bm25Params.add(k1TF);
    bm25Params.add(new JLabel("b"));
    JTextField bTF = new JTextField(5);
    bm25Params.add(bTF);
    panel.add(bm25Params);

    return panel;
  }
}
