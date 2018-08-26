package org.apache.lucene.luke.app.desktop.listeners.dialog.menubar;

import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogProvider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class OpenDialogListeners {

  private static final Logger logger = LoggerFactory.getLogger(OpenDialogListeners.class);

  private final OpenIndexDialogProvider.Components components;

  private final DirectoryHandler directoryHandler;

  private final IndexHandler indexHandler;

  public OpenDialogListeners(OpenIndexDialogProvider.Components components, DirectoryHandler directoryHandler, IndexHandler indexHandler) {
    this.components = components;
    this.directoryHandler = directoryHandler;
    this.indexHandler = indexHandler;
  }

  public ActionListener getBrowseBtnListener() {
    return (ActionEvent e) -> {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int retVal = fc.showOpenDialog(components.getDialog());
      if (retVal == JFileChooser.APPROVE_OPTION) {
        File dir = fc.getSelectedFile();
        components.getIdxPathCB().insertItemAt(dir.getAbsolutePath(), 0);
        components.getIdxPathCB().setSelectedIndex(0);
      } else {
        System.out.println("cancelled");
      }
    };
  }

  public ActionListener getOkBtnListener() {
    return (ActionEvent e) -> {
      String selectedPath = (String)components.getIdxPathCB().getSelectedItem();
      String dirImplClazz = (String)components.getDirImplCB().getSelectedItem();
      if (selectedPath == null || selectedPath.length() == 0) {
        String msg = MessageUtils.getLocalizedMessage("openindex.message.index_path_not_selected");
        logger.error(msg);
      } else if (components.getNoReaderCB().isSelected()) {
        directoryHandler.open(selectedPath, dirImplClazz);
      } else {
        indexHandler.open(selectedPath, dirImplClazz, components.getReadOnlyCB().isSelected(),
            components.getUseCompoundCB().isSelected(),
            components.getKeepAllCommitsRB().isSelected());
      }
      components.getDialog().dispose();
    };
  }

  public ActionListener getCancelBtnListener() {
    return (ActionEvent e) -> components.getDialog().dispose();
  }

}
