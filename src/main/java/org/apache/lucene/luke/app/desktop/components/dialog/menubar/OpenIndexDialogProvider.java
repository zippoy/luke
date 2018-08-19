package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class OpenIndexDialogProvider implements Provider<JDialog> {

  private final JFrame owner;

  @Inject
  public OpenIndexDialogProvider(JFrame owner) {
    this.owner = owner;
  }

  @Override
  public JDialog get() {
    JDialog dialog = new JDialog(owner, MessageUtils.getLocalizedMessage("openindex.dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(600, 420));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(5,5, 5, 5));

    panel.add(basicSettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(expertSettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(execButtons());

    return panel;
  }

  private JPanel basicSettings() {
    JPanel panel = new JPanel(new GridLayout(2, 1));

    JPanel idxPath = new JPanel(new FlowLayout(FlowLayout.LEADING));
    idxPath.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.index_path")));
    JComboBox<String> history = new JComboBox<>();
    history.setPreferredSize(new Dimension(400, 30));
    idxPath.add(history);
    JButton browseBtn = new JButton(MessageUtils.getLocalizedMessage("button.browse"), ImageUtils.createImageIcon("/img/icon_folder-open_alt.png", 20, 20));
    idxPath.add(browseBtn);
    panel.add(idxPath);

    JPanel readOnly = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JCheckBox roCB = new JCheckBox(MessageUtils.getLocalizedMessage("openindex.checkbox.readonly"));
    readOnly.add(roCB);
    JLabel roIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_lock.png", 12, 12));
    readOnly.add(roIcon);
    panel.add(readOnly);

    return panel;
  }

  private JPanel expertSettings() {
    JPanel panel = new JPanel(new GridLayout(6, 1));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.expert")));
    panel.add(header);

    JPanel dirImpl = new JPanel(new FlowLayout(FlowLayout.LEADING));
    dirImpl.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.dir_impl")));
    JComboBox<String> dirImplCB = new JComboBox<>();
    dirImplCB.setPreferredSize(new Dimension(350, 30));
    dirImpl.add(dirImplCB);
    panel.add(dirImpl);

    JPanel noReader = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JCheckBox noReaderCB = new JCheckBox(MessageUtils.getLocalizedMessage("openindex.checkbox.no_reader"));
    noReader.add(noReaderCB);
    JLabel noReaderIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_cone.png", 12, 12));
    noReader.add(noReaderIcon);
    panel.add(noReader);

    JPanel iwConfig = new JPanel(new FlowLayout(FlowLayout.LEADING));
    iwConfig.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.iw_config")));
    panel.add(iwConfig);

    JPanel compound = new JPanel(new FlowLayout(FlowLayout.LEADING));
    compound.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JCheckBox compoundCB = new JCheckBox(MessageUtils.getLocalizedMessage("openindex.checkbox.use_compound"));
    compound.add(compoundCB);
    panel.add(compound);

    JPanel keepCommits = new JPanel(new FlowLayout(FlowLayout.LEADING));
    keepCommits.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JRadioButton keepLastRB = new JRadioButton(MessageUtils.getLocalizedMessage("openindex.radio.keep_only_last_commit"));
    keepLastRB.setActionCommand("keepLastCommit");
    keepLastRB.setSelected(true);
    keepCommits.add(keepLastRB);
    JRadioButton keepAllRB = new JRadioButton(MessageUtils.getLocalizedMessage("openindex.radio.keep_all_commits"));
    keepAllRB.setActionCommand("keepAllCommits");
    keepAllRB.setSelected(false);
    keepCommits.add(keepAllRB);

    ButtonGroup group = new ButtonGroup();
    group.add(keepLastRB);
    group.add(keepAllRB);

    panel.add(keepCommits);

    return panel;
  }

  private JPanel execButtons() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 10, 20));

    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    panel.add(okBtn);

    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    panel.add(cancelBtn);

    return panel;
  }
}
