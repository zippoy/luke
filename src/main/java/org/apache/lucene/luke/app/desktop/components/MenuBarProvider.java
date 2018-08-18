package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;

public class MenuBarProvider implements IndexObserver, DirectoryObserver, Provider<JMenuBar> {

  private final Preferences prefs;

  private final DirectoryHandler directoryHandler;

  private final IndexHandler indexHandler;

  @Inject
  public MenuBarProvider(Preferences prefs, DirectoryHandler directoryHandler, IndexHandler indexHandler) {
    this.prefs = prefs;
    this.directoryHandler = directoryHandler;
    this.indexHandler = indexHandler;
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

    JMenuItem openIndex = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.open_index"));
    fileMenu.add(openIndex);

    JMenuItem reopenIndex = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.reopen_index"));
    fileMenu.add(reopenIndex);

    JMenuItem closeIndex = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.close_index"));
    fileMenu.add(closeIndex);

    fileMenu.addSeparator();

    JMenu settingsMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.settings"));
    JMenu themeMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.color"));
    JMenuItem grayTheme = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.theme_gray"));
    themeMenu.add(grayTheme);
    JMenuItem classicTheme = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.theme_classic"));
    themeMenu.add(classicTheme);
    JMenuItem sandstoneTheme = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.theme_sandstone"));
    themeMenu.add(sandstoneTheme);
    JMenuItem navyTheme = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.theme_navy"));
    themeMenu.add(navyTheme);
    settingsMenu.add(themeMenu);
    fileMenu.add(settingsMenu);

    fileMenu.addSeparator();

    JMenuItem exit = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.exit"));
    fileMenu.add(exit);

    return fileMenu;
  }

  private JMenu createToolsMenu() {
    JMenu toolsMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.tools"));
    JMenuItem optimizeIndex = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.optimize"));
    toolsMenu.add(optimizeIndex);
    JMenuItem checkIndex = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.check_index"));
    toolsMenu.add(checkIndex);
    return toolsMenu;
  }

  private JMenu createHelpMenu() {
    JMenu helpMenu = new JMenu(MessageUtils.getLocalizedMessage("menu.help"));
    JMenuItem about = new JMenuItem(MessageUtils.getLocalizedMessage("menu.item.about"));
    helpMenu.add(about);
    return helpMenu;
  }

  @Override
  public void openDirectory(LukeState state) {

  }

  @Override
  public void closeDirectory() {

  }

  @Override
  public void openIndex(LukeState state) {

  }

  @Override
  public void closeIndex() {

  }

}
