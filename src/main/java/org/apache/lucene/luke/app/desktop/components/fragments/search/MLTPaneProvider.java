package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.util.FontUtil;
import org.apache.lucene.luke.app.desktop.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.search.MLTConfig;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MLTPaneProvider implements Provider<JScrollPane> {

  private final Listeners listeners;

  private final JLabel analyzerLbl = new JLabel(StandardAnalyzer.class.getName());

  private final JFormattedTextField maxDocFreqFTF = new JFormattedTextField();

  private final JFormattedTextField minDocFreqFTF = new JFormattedTextField();

  private final JFormattedTextField minTermFreqFTF = new JFormattedTextField();

  private final JCheckBox loadAllCB = new JCheckBox();

  private final JTable fieldsTable = new JTable();

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  private MLTConfig config = new MLTConfig.Builder().build();

  class MLTTabOperatorImpl implements MLTTabOperator {

    @Override
    public void setAnalyzer(Analyzer analyzer) {
      analyzerLbl.setText(analyzer.getClass().getName());
    }

    @Override
    public void setFields(Collection<String> fields) {
      fieldsTable.setModel(new MLTFieldTableModel(fields));
      fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.LOAD.getIndex()).setMinWidth(50);
      fieldsTable.getColumnModel().getColumn(FieldsTableModel.Column.LOAD.getIndex()).setMaxWidth(50);
      fieldsTable.getModel().addTableModelListener(listeners.getFieldsTableModelListener());
    }

    @Override
    public MLTConfig getConfig() {
      List<String> fields = new ArrayList<>();
      for (int row = 0; row < fieldsTable.getRowCount(); row++) {
        boolean selected = (boolean)fieldsTable.getValueAt(row, MLTFieldTableModel.Column.SELECT.getIndex());
        if (selected) {
          fields.add((String)fieldsTable.getValueAt(row, MLTFieldTableModel.Column.FIELD.getIndex()));
        }
      }

      return new MLTConfig.Builder()
          .fields(fields)
          .maxDocFreq((int)maxDocFreqFTF.getValue())
          .minDocFreq((int)minDocFreqFTF.getValue())
          .minTermFreq((int)minTermFreqFTF.getValue())
          .build();
    }
  }

  class Listeners {

    ActionListener getLoadAllCBListener() {
      return (ActionEvent e) -> {
        for (int i = 0; i < fieldsTable.getModel().getRowCount(); i++) {
          if (loadAllCB.isSelected()) {
            fieldsTable.setValueAt(true, i, FieldsTableModel.Column.LOAD.getIndex());
          } else {
            fieldsTable.setValueAt(false, i, FieldsTableModel.Column.LOAD.getIndex());
          }
        }
      };
    }

    TableModelListener getFieldsTableModelListener() {
      return (TableModelEvent e) -> {
        int row = e.getFirstRow();
        int col = e.getColumn();
        if (col == FieldsTableModel.Column.LOAD.getIndex()) {
          boolean isLoad = (boolean)fieldsTable.getModel().getValueAt(row, col);
          if (!isLoad) {
            loadAllCB.setSelected(false);
          }
        }
      };
    }
  }

  @Inject
  public MLTPaneProvider(TabbedPaneProvider.TabSwitcherProxy tabSwitcher,
                         ComponentOperatorRegistry operatorRegistry) {
    this.listeners = new Listeners();
    this.tabSwitcher = tabSwitcher;

    operatorRegistry.register(MLTTabOperator.class, new MLTTabOperatorImpl());
  }

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(mltParams());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(analyzerNamePane());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(fieldsSettings());

    return new JScrollPane(panel);
  }

  private JPanel mltParams() {
    JPanel panel = new JPanel(new GridLayout(3, 1));

    JPanel maxDocFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    maxDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.max_doc_freq")));
    maxDocFreqFTF.setColumns(10);
    maxDocFreqFTF.setValue(config.getMaxDocFreq());
    maxDocFreq.add(maxDocFreqFTF);
    maxDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("label.int_required")));
    panel.add(maxDocFreq);

    JPanel minDocFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.min_doc_freq")));
    minDocFreqFTF.setColumns(5);
    minDocFreqFTF.setValue(config.getMinDocFreq());
    minDocFreq.add(minDocFreqFTF);

    minDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("label.int_required")));
    panel.add(minDocFreq);

    JPanel minTermFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minTermFreq.add(new JLabel(MessageUtils.getLocalizedMessage("serach_mlt.label.min_term_freq")));
    minTermFreqFTF.setColumns(5);
    minTermFreqFTF.setValue(config.getMinTermFreq());
    minTermFreq.add(minTermFreqFTF);
    minTermFreq.add(new JLabel(MessageUtils.getLocalizedMessage("label.int_required")));
    panel.add(minTermFreq);

    return panel;
  }

  private JPanel analyzerNamePane() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.analyzer")));

    panel.add(analyzerLbl);

    JLabel changeLbl = new JLabel(MessageUtils.getLocalizedMessage("search_mlt.hyperlink.change"));
    changeLbl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.ANALYZER);
      }
    });
    panel.add(FontUtil.toLinkText(changeLbl));

    return panel;
  }

  private JPanel fieldsSettings() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(500, 300));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel header = new JPanel(new GridLayout(2, 1));
    header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.description")));
    loadAllCB.setText(MessageUtils.getLocalizedMessage("search_mlt.checkbox.select_all"));
    loadAllCB.setSelected(true);
    loadAllCB.addActionListener(listeners.getLoadAllCBListener());
    header.add(loadAllCB);
    panel.add(header, BorderLayout.PAGE_START);

    TableUtil.setupTable(fieldsTable, ListSelectionModel.SINGLE_SELECTION, new MLTFieldTableModel(), null, 50);
    fieldsTable.setPreferredScrollableViewportSize(fieldsTable.getPreferredSize());
    panel.add(new JScrollPane(fieldsTable), BorderLayout.CENTER);

    return panel;
  }

  public interface MLTTabOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setAnalyzer(Analyzer analyzer);
    void setFields(Collection<String> fields);
    MLTConfig getConfig();
  }

}

class MLTFieldTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    SELECT("Select", 0, Boolean.class),
    FIELD("Field", 1, String.class);

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

  MLTFieldTableModel() {
    this.data = new Object[0][colNames.length];
  }

  MLTFieldTableModel(Collection<String> fields) {
    this.data = new Object[fields.size()][colNames.length];
    int i = 0;
    for (String field : fields) {
      data[i][Column.SELECT.getIndex()] = true;
      data[i][Column.FIELD.getIndex()] = field;
      i++;
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

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == Column.SELECT.getIndex()) {
      return true;
    }
    return false;
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }

}