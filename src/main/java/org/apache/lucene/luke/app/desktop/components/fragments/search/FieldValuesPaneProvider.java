package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class FieldValuesPaneProvider implements Provider<JScrollPane> {

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(fieldsSettings());

    return new JScrollPane(panel);
  }

  private JPanel fieldsSettings() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel header = new JPanel(new GridLayout(1, 2));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_values.label.description")));
    JCheckBox loadAllCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_values.checkbox.load_all"));
    header.add(loadAllCB);
    panel.add(header, BorderLayout.PAGE_START);

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Load", "Field"};
    JTable fieldsTable = new JTable(data, columnNames);
    fieldsTable.setFillsViewportHeight(true);
    panel.add(new JScrollPane(fieldsTable), BorderLayout.CENTER);

    return panel;
  }

}
