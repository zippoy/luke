package org.apache.lucene.luke.app.desktop.listeners;

import org.apache.lucene.luke.app.desktop.LukeMain;
import org.apache.lucene.luke.app.desktop.components.MenuBarProvider;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogProvider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBarListeners {

  private final MenuBarProvider.Controller controller;

  public MenuBarListeners(MenuBarProvider.Controller controller) {
    this.controller = controller;
  }

  public ActionListener getOpenIndexMItemListener() {
    return (ActionEvent e) -> OpenIndexDialogFactory.showOpenIndexDialog();
  }

  public ActionListener getReopenIndexMItemListener() {
    return (ActionEvent e) -> controller.reopen();
  }

  public ActionListener getCloseIndexMItemListener() {
    return (ActionEvent e) -> controller.close();
  }

  public ActionListener getExitMItemListener() {
    return (ActionEvent e) -> controller.exit();
  }

}
