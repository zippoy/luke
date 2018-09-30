package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

public class StoredValueDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  private String field;

  private String value;

  public void setField(String field) {
    this.field = field;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public JDialog create(Window owner, String title, int width, int height) {
    if (Objects.isNull(field) || Objects.isNull(value)) {
      throw new IllegalStateException("field name and/or stored value is not set.");
    }

    dialog = new JDialog(owner, "Term Vector", Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("documents.stored.label.stored_value")));
    header.add(new JLabel(field));
    panel.add(header, BorderLayout.PAGE_START);

    JTextArea valueTA = new JTextArea(value);
    valueTA.setLineWrap(true);
    valueTA.setEditable(false);
    valueTA.setBackground(Color.white);
    JScrollPane scrollPane = new JScrollPane(valueTA);
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 5));

    JButton copyBtn = new JButton(MessageUtils.getLocalizedMessage("button.copy"),
        ImageUtils.createImageIcon("/img/icon_clipboard.png", 20, 20));
    copyBtn.setMargin(new Insets(3, 3, 3, 3));
    copyBtn.addActionListener(e -> {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection selection = new StringSelection(value);
      clipboard.setContents(selection, null);
    });
    footer.add(copyBtn);

    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    closeBtn.setMargin(new Insets(3, 3, 3, 3));
    closeBtn.addActionListener(e -> dialog.dispose());
    footer.add(closeBtn);
    panel.add(footer, BorderLayout.PAGE_END);

    return panel;
  }


}
