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
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditParamsDialogFactory implements DialogOpener.DialogFactory {

  private final ComponentOperatorRegistry operatorRegistry;

  //private final JLabel targetLbl = new JLabel();

  private final JTable paramsTable = new JTable();

  //private final JButton okBtn = new JButton();

  //private final JButton cancelBtn = new JButton();

  private JDialog dialog;

  private EditParamsMode mode;

  private String target;

  private int targetIndex;

  private Map<String, String> params = new HashMap<>();

  private Callable callback;

  @Inject
  public EditParamsDialogFactory(ComponentOperatorRegistry operatorRegistry) {
    this.operatorRegistry = operatorRegistry;
  }

  public void setMode(EditParamsMode mode) {
    this.mode = mode;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setTargetIndex(int targetIndex) {
    this.targetIndex = targetIndex;
  }

  public void setParams(Map<String, String> params) {
    this.params.putAll(params);
  }

  public void setCallback(Callable callback) {
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
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
    header.add(new JLabel("Parameters for:"));
    String[] tmp = target.split("\\.");
    JLabel targetLbl = new JLabel(tmp[tmp.length - 1]);
    header.add(targetLbl);
    panel.add(header, BorderLayout.PAGE_START);

    TableUtil.setupTable(paramsTable, ListSelectionModel.SINGLE_SELECTION, new ParamsTableModel(params), null, 50, 150);
    paramsTable.setShowGrid(true);
    panel.add(new JScrollPane(paramsTable), BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 5));
    JButton okBtn = new JButton(MessageUtils.getLocalizedMessage("button.ok"));
    okBtn.addActionListener(e -> {
      Map<String, String> params = new HashMap<>();
      for (int i = 0; i < paramsTable.getRowCount(); i++) {
        boolean deleted = (boolean)paramsTable.getValueAt(i, ParamsTableModel.Column.DELETE.getIndex());
        String name = (String)paramsTable.getValueAt(i, ParamsTableModel.Column.NAME.getIndex());
        String value = (String)paramsTable.getValueAt(i, ParamsTableModel.Column.VALUE.getIndex());
        if (deleted || Objects.isNull(name) || name.equals("") || Objects.isNull(value) || value.equals("")) {
          continue;
        }
        params.put(name, value);
      }
      updateTargetParams(params);
      callback.call();
      this.params.clear();
      dialog.dispose();
    });
    footer.add(okBtn);
    JButton cancelBtn = new JButton(MessageUtils.getLocalizedMessage("button.cancel"));
    cancelBtn.addActionListener(e -> {
      this.params.clear();
      dialog.dispose();
    });
    footer.add(cancelBtn);
    panel.add(footer, BorderLayout.PAGE_END);

    return panel;
  }

  private void updateTargetParams(Map<String, String> params) {
    operatorRegistry.get(CustomAnalyzerPanelProvider.CustomAnalyzerPanelOperator.class).ifPresent(operator -> {
      switch (mode) {
        case CHARFILTER:
          operator.updateCharFilterParams(targetIndex, params);
          break;
        case TOKENIZER:
          operator.updateTokenizerParams(params);
          break;
        case TOKENFILTER:
          operator.updateTokenFilterParams(targetIndex, params);
          break;
      }
    });
  }

  public enum EditParamsMode {
    CHARFILTER, TOKENIZER, TOKENFILTER;
  }
}

class ParamsTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    DELETE("Delete", 0, Boolean.class),
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

  private static final int PARAM_SIZE = 20;
  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  ParamsTableModel(Map<String, String> params) {
    this.data = new Object[PARAM_SIZE][colNames.length];
    List<String> keys = new ArrayList<>(params.keySet());
    for (int i = 0; i < keys.size(); i++) {
      data[i][Column.NAME.getIndex()] = keys.get(i);
      data[i][Column.VALUE.getIndex()] = params.get(keys.get(i));
    }
    for (int i = 0; i < data.length; i++) {
      data[i][Column.DELETE.getIndex()] = false;
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
    return true;
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
