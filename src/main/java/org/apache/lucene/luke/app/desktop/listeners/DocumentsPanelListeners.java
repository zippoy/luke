package org.apache.lucene.luke.app.desktop.listeners;

import org.apache.lucene.luke.app.desktop.components.DocumentsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DocumentsPanelListeners {

  private final DocumentsPanelProvider.Controller controller;

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  public DocumentsPanelListeners(DocumentsPanelProvider.Controller controller, TabbedPaneProvider.TabSwitcherProxy tabSwitcher) {
    this.controller = controller;
    this.tabSwitcher = tabSwitcher;
  }

  public ActionListener getFieldsCBListener() {
    return (ActionEvent e) -> controller.showFirstTerm();
  }

  public ActionListener getFirstTermBtnListener() {
    return (ActionEvent e) -> controller.showFirstTerm();
  }

  public ActionListener getTermTFListener() {
    return (ActionEvent e) -> controller.seekNextTerm();
  }

  public ActionListener getNextTermBtnListener() {
    return (ActionEvent e) -> controller.showNextTerm();
  }

  public ActionListener getFirstTermDocBtnListener() {
    return (ActionEvent e) -> controller.showFirstTermDoc();
  }

  public ActionListener getNextTermDocBtnListener() {
    return (ActionEvent e) -> controller.showNextTermDoc();
  }

  public ActionListener getMltSearchBtnListener() {
    return (ActionEvent e) -> {
      // TODO
      tabSwitcher.switchTab(TabbedPaneProvider.Tab.SEARCH);
    };
  }

  public ActionListener getAddDocBtn() {
    return (ActionEvent e) -> {
      // TODO
      System.out.println("show add doc dialog");
    };
  }

  public ChangeListener getDocNumSpnrListener() {
    return (ChangeEvent e) -> controller.showCurrentDoc();
  }

}
