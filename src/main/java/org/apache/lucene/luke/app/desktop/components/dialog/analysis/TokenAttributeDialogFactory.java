package org.apache.lucene.luke.app.desktop.components.dialog.analysis;

import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.analysis.Analysis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenAttributeDialogFactory implements DialogOpener.DialogFactory {

  private final JTable attributesTable = new JTable();

  private JDialog dialog;

  private String term;

  private List<Analysis.TokenAttribute> attributes;

  public void setTerm(String term) {
    this.term = term;
  }

  public void setAttributes(List<Analysis.TokenAttribute> attributes) {
    this.attributes = attributes;
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
    header.add(new JLabel("All token attributes for:"));
    header.add(new JLabel(term));
    panel.add(header, BorderLayout.PAGE_START);

    List<TokenAttValue> attrValues = attributes.stream()
        .flatMap(att -> att.getAttValues().entrySet().stream().map(e -> TokenAttValue.of(att.getAttClass(), e.getKey(), e.getValue())))
        .collect(Collectors.toList());
    TableUtil.setupTable(attributesTable, ListSelectionModel.SINGLE_SELECTION, new AttributeTableModel(attrValues), null);
    panel.add(new JScrollPane(attributesTable), BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    okBtn.addActionListener(e -> dialog.dispose());
    footer.add(okBtn);
    panel.add(footer, BorderLayout.PAGE_END);

    return panel;
  }

}

class AttributeTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {

    ATTR("Attribute", 0, String.class),
    NAME("Name", 1, String.class),
    VALUE("Value", 2, String.class);

    private String colName;
    private int index;
    private Class<?> type;

    Column(String colName, int index, Class<?> type) {
      this.colName = colName;
      this.index = index;
      this.type = type;
    }

    @Override
    public String getColName() {
      return colName;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public Class<?> getType() {
      return type;
    }
  }

  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  AttributeTableModel(List<TokenAttValue> attrValues) {
    this.data = new Object[attrValues.size()][colNames.length];
    for (int i = 0; i < attrValues.size(); i++) {
      TokenAttValue attrValue = attrValues.get(i);
      data[i][Column.ATTR.getIndex()] = attrValue.getAttClass();
      data[i][Column.NAME.getIndex()] = attrValue.getName();
      data[i][Column.VALUE.getIndex()] = attrValue.getValue();
    }
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public int getColumnCount() {
    return colNames.length;
  }

  @Override
  public String getColumnName(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).colName;
    }
    return "";
  }

  @Override
  public Class<?> getColumnClass(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).type;
    }
    return Object.class;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data[rowIndex][columnIndex];
  }
}

class TokenAttValue {
  private String attClass;
  private String name;
  private String value;

  public static TokenAttValue of(String attClass, String name, String value) {
    TokenAttValue attValue = new TokenAttValue();
    attValue.attClass = attClass;
    attValue.name = name;
    attValue.value = value;
    return attValue;
  }

  private TokenAttValue() {
  }

  public String getAttClass() {
    return attClass;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
