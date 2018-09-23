package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.DesktopModule;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.listeners.dialog.menubar.OpenIndexDialogListeners;
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
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OpenIndexDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  private Controller controller = new Controller();

  private OpenIndexDialogListeners listeners;

  private Preferences prefs;

  private final JComboBox<String> idxPathCombo = new JComboBox<>();

  private final JButton browseBtn = new JButton();

  private final JCheckBox readOnlyCB = new JCheckBox();

  private final JComboBox<String> dirImplCombo = new JComboBox<>();

  private final JCheckBox noReaderCB = new JCheckBox();

  private final JCheckBox useCompoundCB = new JCheckBox();

  private final JRadioButton keepLastCommitRB = new JRadioButton();

  private final JRadioButton keepAllCommitsRB = new JRadioButton();

  public class Controller {

    public JDialog getDialog() {
      return dialog;
    }

    public void addIndexPath(String path) {
      idxPathCombo.insertItemAt(path, 0);
      idxPathCombo.setSelectedIndex(0);
    }

    public String getSelectedIndexPath() {
      return (String) idxPathCombo.getSelectedItem();
    }

    public String getSelectedDirImpl() {
      return (String) dirImplCombo.getSelectedItem();
    }

    public boolean isNoReader() {
      return noReaderCB.isSelected();
    }

    public boolean isReadOnly() {
      return readOnlyCB.isSelected();
    }

    public boolean useCompound() {
      return useCompoundCB.isSelected();
    }

    public boolean keepAllCommits() {
      return keepAllCommitsRB.isSelected();
    }

    public void setWriterConfigEnabled(boolean enable) {
      useCompoundCB.setEnabled(enable);
      keepLastCommitRB.setEnabled(enable);
      keepAllCommitsRB.setEnabled(enable);
    }

    private Controller() {}

  }

  public void init(DirectoryHandler directoryHandler, IndexHandler indexHandler, Preferences prefs) {
    this.listeners = new OpenIndexDialogListeners(controller, directoryHandler, indexHandler, prefs);
    this.prefs = prefs;
  }

  @Override
  public JDialog create(Window owner, String title, int width, int height) {
    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
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

    for (String path : prefs.getHistory()) {
      idxPathCombo.addItem(path);
    }
    idxPathCombo.setPreferredSize(new Dimension(360, 35));
    idxPath.add(idxPathCombo);

    browseBtn.setText(MessageUtils.getLocalizedMessage("button.browse"));
    browseBtn.setIcon(ImageUtils.createImageIcon("/img/icon_folder-open_alt.png", 20, 20));
    browseBtn.setPreferredSize(new Dimension(120, 35));
    browseBtn.addActionListener(listeners.getBrowseBtnListener());
    idxPath.add(browseBtn);

    panel.add(idxPath);

    JPanel readOnly = new JPanel(new FlowLayout(FlowLayout.LEADING));
    readOnlyCB.setText(MessageUtils.getLocalizedMessage("openindex.checkbox.readonly"));
    readOnlyCB.setSelected(prefs.isReadOnly());
    readOnlyCB.addActionListener(listeners.getReadOnlyCBListener());
    readOnly.add(readOnlyCB);
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
    for (String clazzName : supportedDirImpls()) {
      dirImplCombo.addItem(clazzName);
    }
    dirImplCombo.setPreferredSize(new Dimension(350, 30));
    dirImplCombo.setSelectedItem(prefs.getDirImpl());
    dirImpl.add(dirImplCombo);
    panel.add(dirImpl);

    JPanel noReader = new JPanel(new FlowLayout(FlowLayout.LEADING));
    noReaderCB.setText(MessageUtils.getLocalizedMessage("openindex.checkbox.no_reader"));
    noReaderCB.setSelected(prefs.isNoReader());
    noReader.add(noReaderCB);
    JLabel noReaderIcon = new JLabel(ImageUtils.createImageIcon("/img/icon_cone.png", 12, 12));
    noReader.add(noReaderIcon);
    panel.add(noReader);

    JPanel iwConfig = new JPanel(new FlowLayout(FlowLayout.LEADING));
    iwConfig.add(new JLabel(MessageUtils.getLocalizedMessage("openindex.label.iw_config")));
    panel.add(iwConfig);

    JPanel compound = new JPanel(new FlowLayout(FlowLayout.LEADING));
    compound.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    useCompoundCB.setText(MessageUtils.getLocalizedMessage("openindex.checkbox.use_compound"));
    useCompoundCB.setSelected(prefs.isUseCompound());
    compound.add(useCompoundCB);
    panel.add(compound);

    JPanel keepCommits = new JPanel(new FlowLayout(FlowLayout.LEADING));
    keepCommits.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    keepLastCommitRB.setText(MessageUtils.getLocalizedMessage("openindex.radio.keep_only_last_commit"));
    keepLastCommitRB.setSelected(!prefs.isKeepAllCommits());
    keepCommits.add(keepLastCommitRB);
    keepAllCommitsRB.setText(MessageUtils.getLocalizedMessage("openindex.radio.keep_all_commits"));
    keepAllCommitsRB.setSelected(prefs.isKeepAllCommits());
    keepCommits.add(keepAllCommitsRB);

    ButtonGroup group = new ButtonGroup();
    group.add(keepLastCommitRB);
    group.add(keepAllCommitsRB);

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

  public static void showOpenIndexDialog() {
    Injector injector = DesktopModule.getIngector();
    DirectoryHandler directoryHandler = injector.getInstance(DirectoryHandler.class);
    IndexHandler indexHandler = injector.getInstance(IndexHandler.class);
    Preferences prefs = injector.getInstance(Preferences.class);

    OpenIndexDialogFactory openIndexDialogFactory = new OpenIndexDialogFactory();
    new DialogOpener<>(openIndexDialogFactory).open(MessageUtils.getLocalizedMessage("openindex.dialog.title"), 600, 420,
        (factory) -> {
          factory.init(directoryHandler, indexHandler, prefs);
        });
  }

}
