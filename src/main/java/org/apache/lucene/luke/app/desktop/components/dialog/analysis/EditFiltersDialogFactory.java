package org.apache.lucene.luke.app.desktop.components.dialog.analysis;

import com.google.inject.Inject;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.CustomAnalyzerPanelProvider;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.lang.Callable;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EditFiltersDialogFactory implements DialogOpener.DialogFactory {

  private final ComponentOperatorRegistry operatorRegistry;

  private final EditParamsDialogFactory editParamsDialogFactory;

  private final JLabel targetLbl = new JLabel();

  private final JTable filtersTable = new JTable();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private final FiltersTableMouseListener tableListener = new FiltersTableMouseListener();

  private JDialog dialog;

  private List<String> selectedFilters;

  private Callable callback;

  private EditFiltersMode mode;

  class ListenerFunctions {

    void showEditParamsDialog(MouseEvent e) {
      if (e.getClickCount() != 2 || e.isConsumed()) {
        return;
      }
      int selectedIndex = filtersTable.rowAtPoint(e.getPoint());
      if (selectedIndex < 0 || selectedIndex >= selectedFilters.size()) {
        return;
      }

      switch (mode) {
        case CHARFILTER:
          showEditParamsCharFilterDialog(selectedIndex);
          break;
        case TOKENFILTER:
          showEditParamsTokenFilterDialog(selectedIndex);
          break;
        default:
      }
    }

    private void showEditParamsCharFilterDialog(int selectedIndex) {
      int targetIndex = filtersTable.getSelectedRow();
      String selectedItem = (String)filtersTable.getValueAt(selectedIndex, FiltersTableModel.Column.TYPE.getIndex());
      Map<String, String> params = operatorRegistry.get(CustomAnalyzerPanelProvider.CustomAnalyzerPanelOperator.class).map(operator -> operator.getCharFilterParams(targetIndex)).orElse(Collections.emptyMap());
      new DialogOpener<>(editParamsDialogFactory).open(dialog, MessageUtils.getLocalizedMessage("analysis.dialog.title.char_filter_params"), 400, 300,
          factory -> {
            factory.setMode(EditParamsDialogFactory.EditParamsMode.CHARFILTER);
            factory.setTargetIndex(targetIndex);
            factory.setTarget(selectedItem);
            factory.setParams(params);
          });
    }

    private void showEditParamsTokenFilterDialog(int selectedIndex) {
      int targetIndex = filtersTable.getSelectedRow();
      String selectedItem = (String)filtersTable.getValueAt(selectedIndex, FiltersTableModel.Column.TYPE.getIndex());
      Map<String, String> params = operatorRegistry.get(CustomAnalyzerPanelProvider.CustomAnalyzerPanelOperator.class).map(operator -> operator.getTokenFilterParams(targetIndex)).orElse(Collections.emptyMap());
      new DialogOpener<>(editParamsDialogFactory).open(dialog, MessageUtils.getLocalizedMessage("analysis.dialog.title.char_filter_params"), 400, 300,
          factory -> {
            factory.setMode(EditParamsDialogFactory.EditParamsMode.TOKENFILTER);
            factory.setTargetIndex(targetIndex);
            factory.setTarget(selectedItem);
            factory.setParams(params);
          });
    }
  }

  class FiltersTableMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      listeners.showEditParamsDialog(e);
    }
  }

  @Inject
  public EditFiltersDialogFactory(ComponentOperatorRegistry operatorRegistry,
                                  EditParamsDialogFactory editParamsDialogFactory) {
    this.operatorRegistry = operatorRegistry;
    this.editParamsDialogFactory = editParamsDialogFactory;
  }

  public void setSelectedFilters(List<String> selectedFilters) {
    this.selectedFilters = selectedFilters;
  }

  public void setCallback(Callable callback) {
    this.callback = callback;
  }

  public void setMode(EditFiltersMode mode) {
    this.mode = mode;
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
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.dialog.hint.edit_param")));
    header.add(targetLbl);
    panel.add(header, BorderLayout.PAGE_START);

    TableUtil.setupTable(filtersTable, ListSelectionModel.SINGLE_SELECTION, new FiltersTableModel(selectedFilters), tableListener, 50, 50);
    filtersTable.setShowGrid(true);
    filtersTable.getColumnModel().getColumn(FiltersTableModel.Column.TYPE.getIndex()).setCellRenderer(new TypeCellRenderer());
    panel.add(new JScrollPane(filtersTable), BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 5));
    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    okBtn.addActionListener(e -> {
      List<Integer> deletedIndexes = new ArrayList<>();
      for (int i = 0; i < filtersTable.getRowCount(); i++) {
        boolean deleted = (boolean)filtersTable.getValueAt(i, FiltersTableModel.Column.DELETE.getIndex());
        if (deleted) {
          deletedIndexes.add(i);
        }
      }
      operatorRegistry.get(CustomAnalyzerPanelProvider.CustomAnalyzerPanelOperator.class).ifPresent(operator -> {
        switch (mode) {
          case CHARFILTER:
            operator.updateCharFilters(deletedIndexes);
            break;
          case TOKENFILTER:
            operator.updateTokenFilters(deletedIndexes);
            break;
        }
      });
      callback.call();
      dialog.dispose();
    });
    footer.add(okBtn);
    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    cancelBtn.addActionListener(e -> dialog.dispose());
    footer.add(cancelBtn);
    panel.add(footer, BorderLayout.PAGE_END);

    return panel;
  }

  public enum EditFiltersMode {
    CHARFILTER, TOKENFILTER
  }
}

class FiltersTableModel extends AbstractTableModel  {

  enum Column implements TableColumnInfo {
    DELETE("Delete", 0, Boolean.class),
    ORDER("Order", 1, Integer.class),
    TYPE("Factory class", 2, String.class);

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

  FiltersTableModel() {
    this.data = new Object[0][colNames.length];
  }

  FiltersTableModel(List<String> selectedFilters) {
    this.data = new Object[selectedFilters.size()][colNames.length];
    for (int i = 0; i < selectedFilters.size(); i++) {
      data[i][Column.DELETE.getIndex()] = false;
      data[i][Column.ORDER.getIndex()] = i + 1;
      data[i][Column.TYPE.getIndex()] = selectedFilters.get(i);
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
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == Column.DELETE.getIndex();
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
  }
    @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data[rowIndex][columnIndex];
  }
}

class TypeCellRenderer implements TableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String[] tmp = ((String)value).split("\\.");
    String type = tmp[tmp.length - 1];
    return new JLabel(type);
  }

}


