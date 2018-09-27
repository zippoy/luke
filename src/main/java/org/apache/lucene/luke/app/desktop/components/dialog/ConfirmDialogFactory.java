package org.apache.lucene.luke.app.desktop.components.dialog;

import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

public class ConfirmDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  private String message;

  private Callback callback;

  public void setMessage(String message) {
    this.message = message;
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override
  public JDialog create(Window owner, String title, int width, int height) {
    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(ImageUtils.createImageIcon("/img/icon_error-oct_alt.png", 30, 30)));
    panel.add(header, BorderLayout.PAGE_START);

    JPanel center = new JPanel(new GridLayout(1, 1));
    center.setBorder(BorderFactory.createLineBorder(Color.gray, 3));
    center.add(new JLabel(message, JLabel.CENTER));
    panel.add(center, BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    okBtn.addActionListener(e -> {
      callback.execute();
      dialog.dispose();
    });
    footer.add(okBtn);
    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    closeBtn.addActionListener(e -> dialog.dispose());
    footer.add(closeBtn);
    panel.add(footer, BorderLayout.PAGE_END);

    return panel;
  }

  @FunctionalInterface
  public interface Callback {
    void execute();
  }
}
