package org.apache.lucene.luke.app.desktop.components.util;

import com.google.inject.Injector;
import org.apache.lucene.luke.app.desktop.DesktopModule;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.util.function.Consumer;

public class DialogOpener<T extends DialogOpener.DialogFactory> {

  private final T factory;

  public DialogOpener(T factory) {
    this.factory = factory;
  }

  public void open(String title, int width, int height, Consumer<? super T> initializer,
                      String... styleSheets) {
    initializer.accept(factory);
    JDialog dialog = factory.create(getOwner(), title, width, height);
    dialog.setVisible(true);
  }

  public interface DialogFactory {
    JDialog create(JFrame owner, String title, int width, int height);
  }

  private static JFrame getOwner() {
    Injector injector = DesktopModule.getIngector();
    return injector.getInstance(JFrame.class);
  }
}
