package org.apache.lucene.luke.app.desktop.components.dialog.documents;

import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.documents.TermVectorEntry;

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
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TermVectorDialogFactory implements DialogOpener.DialogFactory {

  private JDialog dialog;

  private String field;

  List<TermVectorEntry> tvEntries;

  @Override
  public JDialog create(Window owner, String title, int width, int height) {
    if (Objects.isNull(field) || Objects.isNull(tvEntries)) {
      throw new IllegalStateException("field name and/or term vector is not set.");
    }

    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("documents.termvector.label.term_vector")));
    header.add(new JLabel(field));
    panel.add(header, BorderLayout.PAGE_START);

    JTable tvTable = new JTable();
    TableUtil.setupTable(tvTable, ListSelectionModel.SINGLE_SELECTION, new TermVectorTableModel(tvEntries), null, 100, 50, 100);
    JScrollPane scrollPane = new JScrollPane(tvTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 10));
    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    closeBtn.setMargin(new Insets(3, 3, 3, 3));
    closeBtn.addActionListener(e -> dialog.dispose());
    footer.add(closeBtn);
    panel.add(footer, BorderLayout.PAGE_END);

    return panel;
  }

  public void setField(String field) {
    this.field = field;
  }

  public void setTvEntries(List<TermVectorEntry> tvEntries) {
    this.tvEntries = tvEntries;
  }
}

class TermVectorTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {

    TERM("Term", 0, String.class),
    FREQ("Freq", 1, Long.class),
    POSITIONS("Positions", 2, String.class),
    OFFSETS("Offsets", 3, String.class);

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

  TermVectorTableModel() {
    this.data = new Object[0][colNames.length];
  }

  TermVectorTableModel(List<TermVectorEntry> tvEntries) {
    this.data = new Object[tvEntries.size()][colNames.length];

    for (int i = 0; i < tvEntries.size(); i++) {
      TermVectorEntry entry = tvEntries.get(i);

      String termText = entry.getTermText();
      long freq = tvEntries.get(i).getFreq();
      String positions = String.join(",",
          entry.getPositions().stream()
              .map(pos -> Integer.toString(pos.getPosition()))
              .collect(Collectors.toList()));
      String offsets = String.join(",",
          entry.getPositions().stream()
              .filter(pos -> pos.getStartOffset().isPresent() && pos.getEndOffset().isPresent())
              .map(pos -> String.format("%d-%d", pos.getStartOffset().orElse(-1), pos.getEndOffset().orElse(-1)))
              .collect(Collectors.toList())
      );

      data[i] = new Object[]{ termText, freq, positions, offsets };
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