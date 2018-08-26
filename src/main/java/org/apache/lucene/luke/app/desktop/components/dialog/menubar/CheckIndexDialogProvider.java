package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class CheckIndexDialogProvider implements Provider<JDialog> {

  private final JFrame owner;

  @Inject
  public CheckIndexDialogProvider(JFrame owner) {
    this.owner = owner;
  }

  @Override
  public JDialog get() {
    JDialog dialog = new JDialog(owner, MessageUtils.getLocalizedMessage("checkidx.dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(600, 600));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(5,5, 5, 5));

    panel.add(controller());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(logs());

    return panel;
  }

  private JPanel controller() {
    JPanel panel = new JPanel(new GridLayout(3, 1));

    JPanel idxPath = new JPanel(new FlowLayout(FlowLayout.LEADING));
    idxPath.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.index_path")));
    JLabel idxPathLabel = new JLabel();
    idxPath.add(idxPathLabel);
    panel.add(idxPath);

    JPanel results = new JPanel(new GridLayout(2,1));
    results.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    results.add(new JLabel("checkidx.label.results"));
    JLabel resultsLabel = new JLabel("?");
    results.add(resultsLabel);
    panel.add(results);

    JPanel execButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton optmizeBtn = new JButton(MessageUtils.getLocalizedMessage("checkidx.button.check"), ImageUtils.createImageIcon("/img/icon_search_alt.png", 20, 20));
    execButtons.add(optmizeBtn);
    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    execButtons.add(closeBtn);
    panel.add(execButtons);

    return panel;
  }

  private JPanel logs() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));

    JPanel repair = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JButton repairBtn = new JButton(MessageUtils.getLocalizedMessage("checkidx.button.fix"), ImageUtils.createImageIcon("/img/icon_tool.png", 20, 20));
    repair.add(repairBtn);
    JTextArea warnArea = new JTextArea(MessageUtils.getLocalizedMessage("checkidx.label.warn"), 3, 30);
    warnArea.setLineWrap(true);
    warnArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    repair.add(warnArea);
    header.add(repair);

    JPanel note = new JPanel(new FlowLayout(FlowLayout.LEADING));
    note.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.note")));
    header.add(note);

    JPanel status = new JPanel(new FlowLayout(FlowLayout.LEADING));
    status.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.status")));
    JLabel statusLabel = new JLabel();
    status.add(statusLabel);
    header.add(status);

    panel.add(header, BorderLayout.PAGE_START);

    JTextArea logArea = new JTextArea();
    panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

    return panel;
  }
}
