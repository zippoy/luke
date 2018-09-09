package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;

public class IndexOptionsDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  @Override
  public JDialog create(JFrame owner, String title, int width, int height) {
    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    panel.add(indexOptions());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(tvOptions());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(dvOptions());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(pvOptions());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(footer());
    return panel;
  }

  private JPanel indexOptions() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel inner1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
    JCheckBox storedCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.stored"));
    inner1.add(storedCB);
    JCheckBox tokenizedCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.tokenized"));
    inner1.add(tokenizedCB);
    JCheckBox omitNormsCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.omit_norm"));
    inner1.add(omitNormsCB);
    panel.add(inner1);

    JPanel inner2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 1));
    JLabel idxOptLbl = new JLabel(MessageUtils.getLocalizedMessage("idx_options.label.index_options"));
    inner2.add(idxOptLbl);
    String[] idxOptList = Arrays.stream(IndexOptions.values()).map(IndexOptions::name).toArray(String[]::new);
    JComboBox<String> idxOptCombo = new JComboBox<>(idxOptList);
    idxOptCombo.setPreferredSize(new Dimension(300, idxOptCombo.getPreferredSize().height));
    inner2.add(idxOptCombo);
    panel.add(inner2);

    return panel;
  }

  private JPanel tvOptions() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel inner1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    JCheckBox storeTVCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.store_tv"));
    inner1.add(storeTVCB);
    panel.add(inner1);

    JPanel inner2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    inner2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    JCheckBox storeTVPosCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.store_tv_pos"));
    inner2.add(storeTVPosCB);
    JCheckBox storeTVOffCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.store_tv_off"));
    inner2.add(storeTVOffCB);
    JCheckBox storeTVPayCB = new JCheckBox(MessageUtils.getLocalizedMessage("idx_options.checkbox.store_tv_pay"));
    inner2.add(storeTVPayCB);
    panel.add(inner2);

    return panel;
  }

  private JPanel dvOptions() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    JLabel dvTypeLbl = new JLabel(MessageUtils.getLocalizedMessage("idx_options.label.dv_type"));
    panel.add(dvTypeLbl);
    String[] dvTypeList = Arrays.stream(DocValuesType.values()).map(DocValuesType::name).toArray(String[]::new);
    JComboBox<String> dvTypeCombo = new JComboBox<>(dvTypeList);
    panel.add(dvTypeCombo);
    return panel;
  }

  private JPanel pvOptions() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel inner1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    inner1.add(new JLabel(MessageUtils.getLocalizedMessage("idx_options.label.point_dims")));
    panel.add(inner1);

    JPanel inner2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    inner2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    inner2.add(new JLabel(MessageUtils.getLocalizedMessage("idx_options.label.point_dc")));
    JTextField dimCountTF = new JTextField(4);
    inner2.add(dimCountTF);
    inner2.add(new JLabel(MessageUtils.getLocalizedMessage("idx_options.label.point_nb")));
    JTextField dimNumBytesTF = new JTextField(4);
    inner2.add(dimNumBytesTF);
    panel.add(inner2);

    return panel;
  }

  private JPanel footer() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    panel.add(okBtn);
    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    cancelBtn.addActionListener(e -> dialog.dispose());
    panel.add(cancelBtn);

    return panel;
  }
}
