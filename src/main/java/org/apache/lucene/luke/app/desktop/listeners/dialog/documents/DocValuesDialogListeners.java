package org.apache.lucene.luke.app.desktop.listeners.dialog.documents;

import org.apache.lucene.luke.app.desktop.components.dialog.documents.DocValuesDialogFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DocValuesDialogListeners {

  private final DocValuesDialogFactory.Controller controller;

  public DocValuesDialogListeners(DocValuesDialogFactory.Controller controller) {
    this.controller = controller;
  }

  public ActionListener getDecoderCBListener() {
    return (ActionEvent e) -> {
      DocValuesDialogFactory.Decoder decoder = controller.getSelectedDecoder();
      controller.changeDecoder(decoder);
    };
  }

  public ActionListener getCopyBtnListener() {
    return (ActionEvent e) -> {
      List<String> values = controller.selectedValues();
      if (values.isEmpty()) {
        values = controller.getAllVlues();
      }

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection selection = new StringSelection(String.join("\n", values));
      clipboard.setContents(selection, null);
    };
  }
}
