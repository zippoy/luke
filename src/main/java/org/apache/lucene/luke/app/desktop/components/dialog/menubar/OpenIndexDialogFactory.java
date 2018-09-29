package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Injector;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.DesktopModule;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OpenIndexDialogFactory implements DialogOpener.DialogFactory {

  private static final Logger logger = LoggerFactory.getLogger(OpenIndexDialogFactory.class);

  private final JComboBox<String> idxPathCombo = new JComboBox<>();

  private final JButton browseBtn = new JButton();

  private final JCheckBox readOnlyCB = new JCheckBox();

  private final JComboBox<String> dirImplCombo = new JComboBox<>();

  private final JCheckBox noReaderCB = new JCheckBox();

  private final JCheckBox useCompoundCB = new JCheckBox();

  private final JRadioButton keepLastCommitRB = new JRadioButton();

  private final JRadioButton keepAllCommitsRB = new JRadioButton();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private JDialog dialog;

  private DirectoryHandler directoryHandler;

  private IndexHandler indexHandler;

  private Preferences prefs;

  class ListenerFunctions {

    void browseDirectory(ActionEvent e) {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int retVal = fc.showOpenDialog(dialog);
      if (retVal == JFileChooser.APPROVE_OPTION) {
        File dir = fc.getSelectedFile();
        idxPathCombo.insertItemAt(dir.getAbsolutePath(), 0);
        idxPathCombo.setSelectedIndex(0);
      }
    }

    void toggleReadOnly(ActionEvent e) {
      setWriterConfigEnabled(!isReadOnly());
    }

    private void setWriterConfigEnabled(boolean enable) {
      useCompoundCB.setEnabled(enable);
      keepLastCommitRB.setEnabled(enable);
      keepAllCommitsRB.setEnabled(enable);
    }

    void openIndexOrDirectory(ActionEvent e) {
      try {
        if (directoryHandler.directoryOpened()) {
          directoryHandler.close();
        }
        if (indexHandler.indexOpened()) {
          indexHandler.close();
        }

        String selectedPath = (String)idxPathCombo.getSelectedItem();
        String dirImplClazz = (String)dirImplCombo.getSelectedItem();
        if (selectedPath == null || selectedPath.length() == 0) {
          String msg = MessageUtils.getLocalizedMessage("openindex.message.index_path_not_selected");
          logger.error(msg);
        } else if (isNoReader()) {
          directoryHandler.open(selectedPath, dirImplClazz);
        } else {
          indexHandler.open(selectedPath, dirImplClazz, isReadOnly(), useCompound(), keepAllCommits());
        }
        addHistory(selectedPath);
        prefs.setIndexOpenerPrefs(
            isReadOnly(), dirImplClazz,
            isNoReader(), useCompound(), keepAllCommits());
        closeDialog();
      } catch (LukeException ex) {
        JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Invalid index path", JOptionPane.ERROR_MESSAGE);
      } catch (Throwable cause) {
        JOptionPane.showMessageDialog(dialog, MessageUtils.getLocalizedMessage("message.error.unknown"), "Unknown Error", JOptionPane.ERROR_MESSAGE);
        logger.error(cause.getMessage(), cause);
      }
    }

    private boolean isNoReader() {
      return noReaderCB.isSelected();
    }

    private boolean isReadOnly() {
      return readOnlyCB.isSelected();
    }

    private boolean useCompound() {
      return useCompoundCB.isSelected();
    }

    private boolean keepAllCommits() {
      return keepAllCommitsRB.isSelected();
    }

    private void closeDialog() {
      dialog.dispose();
    }

    private void addHistory(String indexPath) throws IOException {
      prefs.addHistory(indexPath);
    }

  }

  private void init(DirectoryHandler directoryHandler, IndexHandler indexHandler, Preferences prefs) {
    this.directoryHandler = directoryHandler;
    this.indexHandler = indexHandler;
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
    browseBtn.addActionListener(listeners::browseDirectory);
    idxPath.add(browseBtn);

    panel.add(idxPath);

    JPanel readOnly = new JPanel(new FlowLayout(FlowLayout.LEADING));
    readOnlyCB.setText(MessageUtils.getLocalizedMessage("openindex.checkbox.readonly"));
    readOnlyCB.setSelected(prefs.isReadOnly());
    readOnlyCB.addActionListener(listeners::toggleReadOnly);
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
    okBtn.addActionListener(listeners::openIndexOrDirectory);
    panel.add(okBtn);

    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    cancelBtn.addActionListener(e -> dialog.dispose());
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
