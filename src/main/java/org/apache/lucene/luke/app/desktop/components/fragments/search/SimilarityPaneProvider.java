package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.util.StyleConstants;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.search.SimilarityConfig;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

public class SimilarityPaneProvider implements Provider<JScrollPane> {

  private final JCheckBox tfidfCB = new JCheckBox();

  private final JCheckBox discardOverlapsCB = new JCheckBox();

  private final JFormattedTextField k1FTF = new JFormattedTextField();

  private final JFormattedTextField bFTF = new JFormattedTextField();

  private final SimilarityConfig config = new SimilarityConfig.Builder().build();

  private final ListenerFunctions listeners = new ListenerFunctions();

  class ListenerFunctions {

    void toggleTfIdf(ActionEvent e) {
      if (tfidfCB.isSelected()) {
        k1FTF.setEnabled(false);
        k1FTF.setBackground(StyleConstants.DISABLED_COLOR);
        bFTF.setEnabled(false);
        bFTF.setBackground(StyleConstants.DISABLED_COLOR);
      } else {
        k1FTF.setEnabled(true);
        k1FTF.setBackground(Color.white);
        bFTF.setEnabled(true);
        bFTF.setBackground(Color.white);
      }
    }
  }

  class SimilarityTabOperatorImpl implements SimilarityTabOperator {

    @Override
    public SimilarityConfig getConfig() {
      float k1 = (float)k1FTF.getValue();
      float b = (float)bFTF.getValue();
      return new SimilarityConfig.Builder()
          .useClassicSimilarity(tfidfCB.isSelected())
          .discountOverlaps(discardOverlapsCB.isSelected())
          .k1(k1)
          .b(b)
          .build();
    }

  }

  @Inject
  public SimilarityPaneProvider(ComponentOperatorRegistry operatorRegistry) {
    operatorRegistry.register(SimilarityTabOperator.class, new SimilarityTabOperatorImpl());
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
    tfidfCB.addActionListener(listeners::toggleTfIdf);
    panel.add(tfidfCB);

    discardOverlapsCB.setText(MessageUtils.getLocalizedMessage("search_similarity.checkbox.discount_overlaps"));
    discardOverlapsCB.setSelected(config.isUseClassicSimilarity());
    panel.add(discardOverlapsCB);

    JLabel bm25Label = new JLabel(MessageUtils.getLocalizedMessage("search_similarity.label.bm25_params"));
    panel.add(bm25Label);

    JPanel bm25Params = new JPanel(new FlowLayout(FlowLayout.LEADING));
    bm25Params.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    JPanel k1Val = new JPanel(new FlowLayout(FlowLayout.LEADING));
    k1Val.add(new JLabel("k1: "));
    k1FTF.setColumns(5);
    k1FTF.setValue(config.getK1());
    k1Val.add(k1FTF);
    k1Val.add(new JLabel(MessageUtils.getLocalizedMessage("label.float_required")));
    bm25Params.add(k1Val);

    JPanel bVal = new JPanel(new FlowLayout(FlowLayout.LEADING));
    bVal.add(new JLabel("b: "));
    bFTF.setColumns(5);
    bFTF.setValue(config.getB());
    bVal.add(bFTF);
    bVal.add(new JLabel(MessageUtils.getLocalizedMessage("label.float_required")));
    bm25Params.add(bVal);

    panel.add(bm25Params);

    return panel;
  }

  public interface SimilarityTabOperator extends ComponentOperatorRegistry.ComponentOperator {
    SimilarityConfig getConfig();
  }
}
