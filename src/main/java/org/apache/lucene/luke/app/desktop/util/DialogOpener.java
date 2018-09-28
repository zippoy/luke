package org.apache.lucene.luke.app.desktop.util;

import org.apache.lucene.luke.app.desktop.LukeMain;
import javax.swing.JDialog;
import java.awt.Window;
import java.util.function.Consumer;

public class DialogOpener<T extends DialogOpener.DialogFactory> {

  private final T factory;

  public DialogOpener(T factory) {
    this.factory = factory;
  }

  public void open(String title, int width, int height, Consumer<? super T> initializer,
                   String... styleSheets) {
    open(LukeMain.getOwnerFrame(), title, width, height, initializer, styleSheets);
  }

  public void open(Window owner, String title, int width, int height, Consumer<? super T> initializer,
                   String... styleSheets) {
    initializer.accept(factory);
    JDialog dialog = factory.create(owner, title, width, height);
    dialog.setVisible(true);
  }

  public interface DialogFactory {
    JDialog create(Window owner, String title, int width, int height);
  }

}
