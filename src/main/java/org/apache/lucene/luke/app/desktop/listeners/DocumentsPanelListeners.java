package org.apache.lucene.luke.app.desktop.listeners;

import org.apache.lucene.luke.app.desktop.components.DocumentsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;
import org.apache.lucene.luke.models.documents.Documents;

import java.awt.event.ActionListener;

public class DocumentsPanelListeners {

  private final DocumentsPanelProvider.Controller controller;

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  private Documents documentsModel;

  public DocumentsPanelListeners(DocumentsPanelProvider.Controller controller, TabbedPaneProvider.TabSwitcherProxy tabSwitcher) {
    this.controller = controller;
    this.tabSwitcher = tabSwitcher;
  }

  public void setDocumentsModel(Documents documentsModel) {
    this.documentsModel = documentsModel;
  }

}
