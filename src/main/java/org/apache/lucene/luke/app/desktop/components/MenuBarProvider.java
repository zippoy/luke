package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.listeners.MenuBarListeners;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;

public class MenuBarProvider implements Provider<JMenuBar> {

  private final Preferences prefs;

  private final DirectoryHandler directoryHandler;

  private final IndexHandler indexHandler;

  private final MenuBarListeners listeners;

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

  public class Controller {

    public void reopen() {
      indexHandler.reOpen();
    }

    public void close() {
      directoryHandler.close();
      indexHandler.close();
    }

    public void exit() {
      close();
      System.exit(0);
    }

    private Controller() {}
  }

  public class Observer implements IndexObserver, DirectoryObserver {

    @Override
    public void openDirectory(LukeState state) {
      reopenIndexMItem.setEnabled(false);
      closeIndexMItem.setEnabled(false);
      optimizeIndexMItem.setEnabled(false);
      checkIndexMItem.setEnabled(false);
    }

    @Override
    public void closeDirectory() {
      reopenIndexMItem.setEnabled(false);
      closeIndexMItem.setEnabled(false);
      optimizeIndexMItem.setEnabled(false);
      checkIndexMItem.setEnabled(false);
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
    }

    @Override
    public void closeIndex() {
      reopenIndexMItem.setEnabled(false);
      closeIndexMItem.setEnabled(false);
      optimizeIndexMItem.setEnabled(false);
      checkIndexMItem.setEnabled(false);
    }

    private Observer() {}
  }

  @Inject
  public MenuBarProvider(Preferences prefs, DirectoryHandler directoryHandler, IndexHandler indexHandler) {
    this.prefs = prefs;
    this.directoryHandler = directoryHandler;
    this.indexHandler = indexHandler;
    this.listeners = new MenuBarListeners(new Controller());

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
    openIndexMItem.addActionListener(listeners.getOpenIndexMItemListener());
    fileMenu.add(openIndexMItem);

    reopenIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.reopen_index"));
    reopenIndexMItem.setEnabled(false);
    reopenIndexMItem.addActionListener(listeners.getReopenIndexMItemListener());
    fileMenu.add(reopenIndexMItem);

    closeIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.close_index"));
    closeIndexMItem.setEnabled(false);
    closeIndexMItem.addActionListener(listeners.getCloseIndexMItemListener());
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
    exitMItem.addActionListener(listeners.getExitMItemListener());
    fileMenu.add(exitMItem);

    return fileMenu;
  }

  private JMenu createToolsMenu() {
    JMenu toolsMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.tools"));
    optimizeIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.optimize"));
    optimizeIndexMItem.setEnabled(false);
    toolsMenu.add(optimizeIndexMItem);
    checkIndexMItem.setText(MessageUtils.getLocalizedMessage("menu.item.check_index"));
    checkIndexMItem.setEnabled(false);
    toolsMenu.add(checkIndexMItem);
    return toolsMenu;
  }

  private JMenu createHelpMenu() {
    JMenu helpMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.help"));
    aboutMItem.setText(MessageUtils.getLocalizedMessage("menu.item.about"));
    helpMenu.add(aboutMItem);
    return helpMenu;
  }

}
