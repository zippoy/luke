package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.components.util.StyleConstants;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimilarityPaneProvider implements Provider<JScrollPane> {

  private final JCheckBox tfidfCB = new JCheckBox();

  private final JCheckBox discardOverlapsCB = new JCheckBox();

  private final JTextField k1TF = new JTextField();

  private final JTextField bTF = new JTextField();

  private final Listeners listeners;

  class Listeners {

    ActionListener getTfIdfCBListener() {
      return (ActionEvent e) -> {
        if (tfidfCB.isSelected()) {
          k1TF.setEnabled(false);
          k1TF.setBackground(StyleConstants.DISABLED_COLOR);
          bTF.setEnabled(false);
          bTF.setBackground(StyleConstants.DISABLED_COLOR);
        } else {
          k1TF.setEnabled(true);
          k1TF.setBackground(Color.white);
          bTF.setEnabled(true);
          bTF.setBackground(Color.white);
        }
      };
    }

  }

  public SimilarityPaneProvider() {
    this.listeners = new Listeners();
    UIManager.put("inactiveBackground", new ColorUIResource(Color.lightGray));
  }

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    panel.add(similaritySettings());

    return new JScrollPane(panel);
  }

  private JPanel similaritySettings() {
    JPanel panel = new JPanel(new GridLayout(4, 1));
    panel.setMaximumSize(new Dimension(700, 220));

    tfidfCB.setText(MessageUtils.getLocalizedMessage("search_similarity.checkbox.use_classic"));
    tfidfCB.addActionListener(listeners.getTfIdfCBListener());
    panel.add(tfidfCB);

    discardOverlapsCB.setText(MessageUtils.getLocalizedMessage("search_similarity.checkbox.discount_overlaps"));
    panel.add(discardOverlapsCB);

    JLabel bm25Label = new JLabel(MessageUtils.getLocalizedMessage("search_similarity.label.bm25_params"));
    panel.add(bm25Label);

    JPanel bm25Params = new JPanel(new FlowLayout(FlowLayout.LEADING));
    bm25Params.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    JPanel k1Val = new JPanel(new FlowLayout(FlowLayout.LEADING));
    k1Val.add(new JLabel("k1: "));
    k1TF.setColumns(5);
    k1Val.add(k1TF);
    k1Val.add(new JLabel(MessageUtils.getLocalizedMessage("label.float_required")));
    bm25Params.add(k1Val);

    JPanel bVal = new JPanel(new FlowLayout(FlowLayout.LEADING));
    bVal.add(new JLabel("b: "));
    bTF.setColumns(5);
    bVal.add(bTF);
    bVal.add(new JLabel(MessageUtils.getLocalizedMessage("label.float_required")));
    bm25Params.add(bVal);

    panel.add(bm25Params);

    return panel;
  }
}
