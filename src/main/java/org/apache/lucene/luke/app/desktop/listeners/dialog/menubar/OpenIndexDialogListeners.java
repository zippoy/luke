package org.apache.lucene.luke.app.desktop.listeners.dialog.menubar;

import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.desktop.Preferences;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogProvider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.LukeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class OpenIndexDialogListeners {

  private static final Logger logger = LoggerFactory.getLogger(OpenIndexDialogListeners.class);

  private final OpenIndexDialogProvider.Controller controller;

  private final DirectoryHandler directoryHandler;

  private final IndexHandler indexHandler;

  private final Preferences prefs;

  public OpenIndexDialogListeners(OpenIndexDialogProvider.Controller controller,
                                  DirectoryHandler directoryHandler, IndexHandler indexHandler,
                                  Preferences preferences) {
    this.controller = controller;
    this.directoryHandler = directoryHandler;
    this.indexHandler = indexHandler;
    this.prefs = preferences;
  }

  public ActionListener getBrowseBtnListener() {
    return (ActionEvent e) -> {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int retVal = fc.showOpenDialog(controller.getDialog());
      if (retVal == JFileChooser.APPROVE_OPTION) {
        File dir = fc.getSelectedFile();
        controller.addIndexPath(dir.getAbsolutePath());
      }
    };
  }

  public ActionListener getReadOnlyCBListener() {
    return (ActionEvent e) ->
      controller.setWriterConfigEnabled(!controller.isReadOnly());
  }

  public ActionListener getOkBtnListener() {
    return (ActionEvent e) -> {
      try {
        if (directoryHandler.directoryOpened()) {
          directoryHandler.close();
        }
        if (indexHandler.indexOpened()) {
          indexHandler.close();
        }

        String selectedPath = controller.getSelectedIndexPath();
        String dirImplClazz = controller.getSelectedDirImpl();
        if (selectedPath == null || selectedPath.length() == 0) {
          String msg = MessageUtils.getLocalizedMessage("openindex.message.index_path_not_selected");
          logger.error(msg);
        } else if (controller.isNoReader()) {
          directoryHandler.open(selectedPath, dirImplClazz);
        } else {
          indexHandler.open(selectedPath, dirImplClazz, controller.isReadOnly(),
              controller.useCompound(), controller.keepAllCommits());
        }
        addHistory(selectedPath);
        prefs.setIndexOpenerPrefs(
            controller.isReadOnly(), controller.getSelectedDirImpl(),
            controller.isNoReader(), controller.useCompound(), controller.keepAllCommits());
        closeDialog();
      } catch (LukeException ex) {
        JOptionPane.showMessageDialog(controller.getDialog(), ex.getMessage(), "Invalid index path", JOptionPane.ERROR_MESSAGE);
      } catch (Throwable cause) {
        JOptionPane.showMessageDialog(controller.getDialog(), MessageUtils.getLocalizedMessage("message.error.unknown"), "Unknown Error", JOptionPane.ERROR_MESSAGE);
        logger.error(cause.getMessage(), cause);
      }
    };
  }

  public ActionListener getCancelBtnListener() {
    return (ActionEvent e) -> closeDialog();
  }

  private void closeDialog() {
    controller.getDialog().dispose();
  }

  private void addHistory(String indexPath) throws IOException {
    prefs.addHistory(indexPath);
  }

}
