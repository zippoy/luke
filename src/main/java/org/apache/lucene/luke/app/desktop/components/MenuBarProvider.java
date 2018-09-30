package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.AboutDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.CheckIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OptimizeIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.util.Version;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MenuBarProvider implements Provider<JMenuBar> {

  private final Preferences prefs;

  private final DirectoryHandler directoryHandler;

  private final IndexHandler indexHandler;

  private final OptimizeIndexDialogFactory optimizeIndexDialogFactory;

  private final CheckIndexDialogFactory checkIndexDialogFactory;

  private final AboutDialogFactory aboutDialogFactory;

  private final JMenuItem openIndexMItem = new JMenuItem();

  private final JMenuItem reopenIndexMItem = new JMenuItem();

  private final JMenuItem closeIndexMItem = new JMenuItem();

  private final JMenuItem grayThemeMItem = new JMenuItem();

  private final JMenuItem classicThemeMItem = new JMenuItem();

  private final JMenuItem sandstoneThemeMItem = new JMenuItem();

  private final JMenuItem navyThemeMItem = new JMenuItem();

  private final JMenuItem exitMItem = new JMenuItem();

  private final JMenuItem optimizeIndexMItem = new JMenuItem();

  private final JMenuItem checkIndexMItem = new JMenuItem();

  private final JMenuItem aboutMItem = new JMenuItem();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private String indexPath;

  class ListenerFunctions {

    void showOpenIndexDialog(ActionEvent e){
      OpenIndexDialogFactory.showOpenIndexDialog();
    }

    void reopenIndex(ActionEvent e) {
      indexHandler.reOpen();
    }

    void closeIndex(ActionEvent e) {
      close();
    }

    void exit(ActionEvent e) {
      close();
      System.exit(0);
    }

    private void close() {
      directoryHandler.close();
      indexHandler.close();
    }

    void showOptimizeIndexDialog(ActionEvent e) {
      new DialogOpener<>(optimizeIndexDialogFactory).open("Optimize index", 600, 600,
          factory -> {});
    }

    void showCheckIndexDialog(ActionEvent e) {
      new DialogOpener<>(checkIndexDialogFactory).open("Check index", 600, 600,
          factory -> {});
    }

    void showAboutDialog(ActionEvent e) {
      final String title = "About Luke v" + Version.LATEST.toString();
      new DialogOpener<>(aboutDialogFactory).open(title, 800, 480,
          factory -> {});
    }
  }

  public class Observer implements IndexObserver, DirectoryObserver {

    @Override
    public void openDirectory(LukeState state) {
      reopenIndexMItem.setEnabled(false);
      closeIndexMItem.setEnabled(false);
      optimizeIndexMItem.setEnabled(false);
      checkIndexMItem.setEnabled(true);
      indexPath = "";
    }

    @Override
    public void closeDirectory() {
      close();
    }

    @Override
    public void openIndex(LukeState state) {
      reopenIndexMItem.setEnabled(true);
      closeIndexMItem.setEnabled(true);
      if (!state.readOnly() && state.hasDirectoryReader()) {
        optimizeIndexMItem.setEnabled(true);
      }
      if (state.hasDirectoryReader()) {
        checkIndexMItem.setEnabled(true);
      }
      indexPath = state.getIndexPath();
    }

    @Override
    public void closeIndex() {
      close();
    }

    private void close() {
      reopenIndexMItem.setEnabled(false);
      closeIndexMItem.setEnabled(false);
      optimizeIndexMItem.setEnabled(false);
      checkIndexMItem.setEnabled(false);
      indexPath = "";
    }

    private Observer() {}
  }

  @Inject
  public MenuBarProvider(Preferences prefs, DirectoryHandler directoryHandler, IndexHandler indexHandler,
                         OptimizeIndexDialogFactory optimizeIndexDialogFactory,
                         CheckIndexDialogFactory checkIndexDialogFactory,
                         AboutDialogFactory aboutDialogFactory) {
    this.prefs = prefs;
    this.directoryHandler = directoryHandler;
    this.indexHandler = indexHandler;
    this.optimizeIndexDialogFactory = optimizeIndexDialogFactory;
    this.checkIndexDialogFactory = checkIndexDialogFactory;
    this.aboutDialogFactory = aboutDialogFactory;

    Observer observer = new Observer();
    directoryHandler.addObserver(observer);
    indexHandler.addObserver(observer);
  }

  public JMenuBar get() {
    JMenuBar menuBar = new JMenuBar();

    menuBar.add(createFileMenu());
    menuBar.add(createToolsMenu());
    menuBar.add(createHelpMenu());

    return menuBar;
  }

  private JMenu createFileMenu() {
    JMenu fileMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.file"));

    openIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.open_index"));
    openIndexMItem.addActionListener(listeners::showOpenIndexDialog);
    fileMenu.add(openIndexMItem);

    reopenIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.reopen_index"));
    reopenIndexMItem.setEnabled(false);
    reopenIndexMItem.addActionListener(listeners::reopenIndex);
    fileMenu.add(reopenIndexMItem);

    closeIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.close_index"));
    closeIndexMItem.setEnabled(false);
    closeIndexMItem.addActionListener(listeners::closeIndex);
    fileMenu.add(closeIndexMItem);

    fileMenu.addSeparator();

    JMenu settingsMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.settings"));
    JMenu themeMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.color"));
    grayThemeMItem.setText(MessageUtils.getLocalizedMessage("menu.item.theme_gray"));
    themeMenu.add(grayThemeMItem);
    classicThemeMItem.setText(MessageUtils.getLocalizedMessage("menu.item.theme_classic"));
    themeMenu.add(classicThemeMItem);
    sandstoneThemeMItem.setText(MessageUtils.getLocalizedMessage("menu.item.theme_sandstone"));
    themeMenu.add(sandstoneThemeMItem);
    navyThemeMItem.setText(MessageUtils.getLocalizedMessage("menu.item.theme_navy"));
    themeMenu.add(navyThemeMItem);
    settingsMenu.add(themeMenu);
    fileMenu.add(settingsMenu);

    fileMenu.addSeparator();

    exitMItem.setText(MessageUtils.getLocalizedMessage("menu.item.exit"));
    exitMItem.addActionListener(listeners::exit);
    fileMenu.add(exitMItem);

    return fileMenu;
  }

  private JMenu createToolsMenu() {
    JMenu toolsMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.tools"));
    optimizeIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.optimize"));
    optimizeIndexMItem.setEnabled(false);
    optimizeIndexMItem.addActionListener(listeners::showOptimizeIndexDialog);
    toolsMenu.add(optimizeIndexMItem);
    checkIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.check_index"));
    checkIndexMItem.setEnabled(false);
    checkIndexMItem.addActionListener(listeners::showCheckIndexDialog);
    toolsMenu.add(checkIndexMItem);
    return toolsMenu;
  }

  private JMenu createHelpMenu() {
    JMenu helpMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.help"));
    aboutMItem.setText(MessageUtils.getLocalizedMessage("menu.item.about"));
    aboutMItem.addActionListener(listeners::showAboutDialog);
    helpMenu.add(aboutMItem);
    return helpMenu;
  }

}
