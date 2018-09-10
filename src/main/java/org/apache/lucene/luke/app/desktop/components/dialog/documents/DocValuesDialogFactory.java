package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.listeners.dialog.documents.DocValuesDialogListeners;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.documents.DocValues;
import org.apache.lucene.luke.util.BytesRefUtils;
import org.apache.lucene.util.NumericUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DocValuesDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  private String field;

  private DocValues docValues;

  private final JComboBox<String> decodersCB = new JComboBox<>();

  private final JList<String> valueList = new JList<>();

  private final DocValuesDialogListeners listeners;

  public class Controller {

    public Decoder getSelectedDecoder() {
      String decoderLabel = (String) decodersCB.getSelectedItem();
      return Decoder.fromLabel(decoderLabel);
    }

    public void changeDecoder(Decoder decoder) {
      if (docValues.getNumericValues().isEmpty()) {
        return;
      }

      DefaultListModel<String> values = new DefaultListModel<>();
      switch (decoder) {
        case LONG:
          docValues.getNumericValues().stream()
              .map(String::valueOf)
              .forEach(values::addElement);
          break;
        case FLOAT:
          docValues.getNumericValues().stream()
              .mapToInt(Long::intValue)
              .mapToObj(NumericUtils::sortableIntToFloat)
              .map(String::valueOf)
              .forEach(values::addElement);
          break;
        case DOUBLE:
          docValues.getNumericValues().stream()
              .map(NumericUtils::sortableLongToDouble)
              .map(String::valueOf)
              .forEach(values::addElement);
          break;
      }

      valueList.setModel(values);
    }

    public List<String> selectedValues() {
      return valueList.getSelectedValuesList();
    }

    public List<String> getAllVlues() {
      List<String> values = new ArrayList<>();
      for (int i = 0; i < valueList.getModel().getSize(); i++) {
        values.add(valueList.getModel().getElementAt(i));
      }
      return values;
    }
  }

  public DocValuesDialogFactory() {
    this.listeners = new DocValuesDialogListeners(new Controller());
  }

  @Override
  public JDialog create(JFrame owner, String title, int width, int height) {
    if (Objects.isNull(field) || Objects.isNull(docValues)) {
      throw new IllegalStateException("field name and/or doc values is not set.");
    }

    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  public void setValue(String field, DocValues docValues) {
    this.field = field;
    this.docValues = docValues;

    DefaultListModel<String> values = new DefaultListModel<>();
    if (docValues.getValues().size() > 0) {
      decodersCB.setEnabled(false);
      docValues.getValues().stream()
          .map(BytesRefUtils::decode)
          .forEach(values::addElement);
    } else if (docValues.getNumericValues().size() > 0) {
      decodersCB.setEnabled(true);
      docValues.getNumericValues().stream()
          .map(String::valueOf)
          .forEach(values::addElement);
    }

    valueList.setModel(values);
  }

  private JPanel content() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    panel.add(headerPanel(), BorderLayout.PAGE_START);
    JScrollPane scrollPane = new JScrollPane(valueList());
    panel.add(scrollPane, BorderLayout.CENTER);
    panel.add(footerPanel(), BorderLayout.PAGE_END);
    return panel;
  }

  private JPanel headerPanel() {
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));

    JPanel fieldHeader = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
    fieldHeader.add(new JLabel(MessageUtils.getLocalizedMessage("documents.docvalues.label.doc_values")));
    fieldHeader.add(new JLabel(field));
    header.add(fieldHeader);

    JPanel typeHeader = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
    typeHeader.add(new JLabel(MessageUtils.getLocalizedMessage("documents.docvalues.label.type")));
    typeHeader.add(new JLabel(docValues.getDvType().toString()));
    header.add(typeHeader);

    JPanel decodeHeader = new JPanel(new FlowLayout(FlowLayout.TRAILING, 3, 3));
    decodeHeader.add(new JLabel("decoded as"));
    String[] decoders = Arrays.stream(Decoder.values()).map(Decoder::toString).toArray(String[]::new);
    decodersCB.setModel(new DefaultComboBoxModel<>(decoders));
    decodersCB.setSelectedItem(Decoder.LONG.toString());
    decodersCB.addActionListener(listeners.getDecoderCBListener());
    decodeHeader.add(decodersCB);
    if (docValues.getValues().size() > 0) {
      decodeHeader.setEnabled(false);
    }
    header.add(decodeHeader);

    return header;
  }

  private JList<String> valueList() {
    valueList.setVisibleRowCount(5);
    valueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    valueList.setLayoutOrientation(JList.VERTICAL);

    DefaultListModel<String> values = new DefaultListModel<>();
    if (docValues.getValues().size() > 0) {
      docValues.getValues().stream()
          .map(BytesRefUtils::decode)
          .forEach(values::addElement);
    } else {
      docValues.getNumericValues().stream()
          .map(String::valueOf)
          .forEach(values::addElement);
    }
    valueList.setModel(values);

    return valueList;
  }

  private JPanel footerPanel() {
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 5));

    JButton copyBtn = new JButton(MessageUtils.getLocalizedMessage("button.copy"),
        ImageUtils.createImageIcon("/img/icon_clipboard.png", 20, 20));
    copyBtn.addActionListener(listeners.getCopyBtnListener());
    footer.add(copyBtn);

    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    closeBtn.addActionListener(e -> dialog.dispose());
    footer.add(closeBtn);

    return footer;
  }

  public enum Decoder {

    LONG("long"), FLOAT("float"), DOUBLE("double");

    private final String label;

    Decoder(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }

    public static Decoder fromLabel(String label) {
      for (Decoder d : values()) {
        if (d.label.equalsIgnoreCase(label)) {
          return d;
        }
      }
      throw new IllegalArgumentException("No such decoder: " + label);
    }
  }

}
