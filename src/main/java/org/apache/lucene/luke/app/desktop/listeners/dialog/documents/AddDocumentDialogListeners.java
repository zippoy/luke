package org.apache.lucene.luke.app.desktop.listeners.dialog.documents;

import org.apache.lucene.luke.app.desktop.components.dialog.documents.AddDocumentDialogFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddDocumentDialogListeners {

  private final AddDocumentDialogFactory.Controller controller;


  public AddDocumentDialogListeners(AddDocumentDialogFactory.Controller controller) {
    this.controller = controller;
  }

  public ActionListener getAddBtnListener() {
    return (ActionEvent e) -> {
      // TODO
      System.out.println("Adding document");
    };
  }

}
