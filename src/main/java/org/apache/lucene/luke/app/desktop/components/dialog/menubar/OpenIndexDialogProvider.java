package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.listeners.dialog.menubar.OpenDialogListeners;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OpenIndexDialogProvider implements Provider<JDialog> {

  private final JFrame owner;

  private final Preferences preferences;

  private final Components components;

  private final OpenDialogListeners listeners;

  public static class Components {

    private JDialog dialog;

    private JComboBox<String> idxPathCB;

    private JButton browseBtn;

    private JCheckBox readOnlyCB;

    private JComboBox<String> dirImplCB;

    private JCheckBox noReaderCB;

    private JCheckBox useCompoundCB;

    private JRadioButton keepLastCommitRB;

    private JRadioButton keepAllCommitsRB;

    public JDialog getDialog() {
      return dialog;
    }

    public JComboBox<String> getIdxPathCB() {
      return idxPathCB;
    }

    public JButton getBrowseBtn() {
      return browseBtn;
    }

    public JCheckBox getReadOnlyCB() {
      return readOnlyCB;
    }

    public JComboBox<String> getDirImplCB() {
      return dirImplCB;
    }

    public JCheckBox getNoReaderCB() {
      return noReaderCB;
    }

    public JCheckBox getUseCompoundCB() {
      return useCompoundCB;
    }

    public JRadioButton getKeepLastCommitRB() {
      return keepLastCommitRB;
    }

    public JRadioButton getKeepAllCommitsRB() {
      return keepAllCommitsRB;
    }
  }

  @Inject
  public OpenIndexDialogProvider(JFrame owner, DirectoryHandler directoryHandler, IndexHandler indexHandler, Preferences preferences) {
    this.owner = owner;
    this.components = new Components();
    this.listeners = new OpenDialogListeners(components, directoryHandler, indexHandler);
    this.preferences = preferences;
  }

  @Override
  public JDialog get() {
    components.dialog = new JDialog(owner, MessageUtils.getLocalizedMessage("openindex.dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
    components.dialog.add(content());
    components.dialog.setSize(new Dimension(600, 420));
    components.dialog.setLocationRelativeTo(owner);
    return components.dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10,10, 10, 10));

    panel.add(basicSettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(expertSettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(buttons());

    return panel;
  }

  private JPanel basicSettings() {
    JPanel panel = new JPanel(new GridLayout(2, 1));

    JPanel idxPath = new JPanel(new FlowLayout(FlowLayout.LEADING));
    idxPath.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.index_path")));

    String[] history = new String[preferences.getHistory().size()];
    components.idxPathCB = new JComboBox<>(preferences.getHistory().toArray(history));
    components.idxPathCB.setPreferredSize(new Dimension(400, 35));
    idxPath.add(components.idxPathCB);

    components.browseBtn = new JButton(MessageUtils.getLocalizedMessage("button.browse"), ImageUtils.createImageIcon("/img/icon_folder-open_alt.png", 20, 20));
    components.browseBtn.setPreferredSize(new Dimension(80, 35));
    components.browseBtn.addActionListener(listeners.getBrowseBtnListener());
    idxPath.add(components.browseBtn);

    panel.add(idxPath);

    JPanel readOnly = new JPanel(new FlowLayout(FlowLayout.LEADING));
    components.readOnlyCB = new JCheckBox(MessageUtils.getLocalizedMessage("openindex.checkbox.readonly"));
    readOnly.add(components.readOnlyCB);
    JLabel roIconLB = new JLabel(ImageUtils.createImageIcon("/img/icon_lock.png", 12, 12));
    readOnly.add(roIconLB);
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
    components.dirImplCB = new JComboBox<>(supportedDirImpls());
    components.dirImplCB.setPreferredSize(new Dimension(350, 30));
    dirImpl.add(components.dirImplCB);
    panel.add(dirImpl);

    JPanel noReader = new JPanel(new FlowLayout(FlowLayout.LEADING));
    components.noReaderCB = new JCheckBox(MessageUtils.getLocalizedMessage("openindex.checkbox.no_reader"));
    noReader.add(components.noReaderCB);
    JLabel noReaderIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_cone.png", 12, 12));
    noReader.add(noReaderIcon);
    panel.add(noReader);

    JPanel iwConfig = new JPanel(new FlowLayout(FlowLayout.LEADING));
    iwConfig.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.iw_config")));
    panel.add(iwConfig);

    JPanel compound = new JPanel(new FlowLayout(FlowLayout.LEADING));
    compound.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    components.useCompoundCB = new JCheckBox(MessageUtils.getLocalizedMessage("openindex.checkbox.use_compound"));
    compound.add(components.useCompoundCB);
    panel.add(compound);

    JPanel keepCommits = new JPanel(new FlowLayout(FlowLayout.LEADING));
    keepCommits.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    components.keepLastCommitRB = new JRadioButton(MessageUtils.getLocalizedMessage("openindex.radio.keep_only_last_commit"));
    components.keepLastCommitRB.setSelected(true);
    keepCommits.add(components.keepLastCommitRB);
    components.keepAllCommitsRB = new JRadioButton(MessageUtils.getLocalizedMessage("openindex.radio.keep_all_commits"));
    components.keepAllCommitsRB.setSelected(false);
    keepCommits.add(components.keepAllCommitsRB);

    ButtonGroup group = new ButtonGroup();
    group.add(components.keepLastCommitRB);
    group.add(components.keepAllCommitsRB);

    panel.add(keepCommits);

    return panel;
  }

  private String[] supportedDirImpls() {
    // supports FS-based built-in implementations
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("org.apache.lucene.store"))
        .setScanners(new SubTypesScanner())
        .filterInputsBy(new FilterBuilder().include("org\\.apache\\.lucene\\.store.*"))
    );
    Set<Class<? extends FSDirectory>> clazzSet = reflections.getSubTypesOf(FSDirectory.class);

    List<String> clazzNames = new ArrayList<>();
    clazzNames.add(FSDirectory.class.getName());
    clazzNames.add(MMapDirectory.class.getName());
    clazzNames.addAll(clazzSet.stream().map(Class::getName).collect(Collectors.toList()));

    String[] result = new String[clazzNames.size()];
    return clazzNames.toArray(result);
  }

  private JPanel buttons() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 10, 20));

    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    okBtn.addActionListener(listeners.getOkBtnListener());
    panel.add(okBtn);

    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    cancelBtn.addActionListener(listeners.getCancelBtnListener());
    panel.add(cancelBtn);

    return panel;
  }

}
