package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class OptimizeIndexDialogProvider implements Provider<JDialog> {

  private final JFrame owner;

  @Inject
  public OptimizeIndexDialogProvider(JFrame owner) {
    this.owner = owner;
  }

  @Override
  public JDialog get() {
    JDialog dialog = new JDialog(owner, MessageUtils.getLocalizedMessage("optimize.dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(600, 600));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(5,5, 5, 5));

    panel.add(controller());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(logs());

    return panel;
  }

  private JPanel controller() {
    JPanel panel = new JPanel(new GridLayout(4, 1));

    JPanel idxPath = new JPanel(new FlowLayout(FlowLayout.LEADING));
    idxPath.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.index_path")));
    JLabel idxPathLabel = new JLabel();
    idxPath.add(idxPathLabel);
    panel.add(idxPath);

    JPanel expunge = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JCheckBox expungeCB = new JCheckBox(MessageUtils.getLocalizedMessage("optimize.checkbox.expunge"));
    expunge.add(expungeCB);
    panel.add(expunge);

    JPanel maxSegs = new JPanel(new FlowLayout(FlowLayout.LEADING));
    maxSegs.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.max_segments")));
    JSpinner maxSegSpinner = new JSpinner();
    maxSegSpinner.setPreferredSize(new Dimension(50, 30));
    maxSegs.add(maxSegSpinner);
    panel.add(maxSegs);

    JPanel execButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton optmizeBtn = new JButton(MessageUtils.getLocalizedMessage("optimize.button.optimize"), ImageUtils.createImageIcon("/img/icon_balance.png", 20, 20));
    execButtons.add(optmizeBtn);
    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    execButtons.add(closeBtn);
    panel.add(execButtons);

    return panel;
  }

  private JPanel logs() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel header = new JPanel(new GridLayout(2,1));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.note")));
    JPanel status = new JPanel(new FlowLayout(FlowLayout.LEADING));
    status.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.status")));
    JLabel statusLabel = new JLabel();
    status.add(statusLabel);
    header.add(status);
    panel.add(header, BorderLayout.PAGE_START);

    JTextArea logArea = new JTextArea();
    panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

    return panel;
  }
}
