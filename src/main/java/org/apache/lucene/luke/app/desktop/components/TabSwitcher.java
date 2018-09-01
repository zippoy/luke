package org.apache.lucene.luke.app.desktop.components;

public class TabSwitcher {

  private TabbedPaneProvider.Controller controller;

  public void setController(TabbedPaneProvider.Controller controller) {
    this.controller = controller;
  }

  public void switchTab(TabbedPaneProvider.Tab tab) {
    if (controller == null) {
      throw new IllegalStateException();
    }
    controller.switchTab(tab);
  }

}
