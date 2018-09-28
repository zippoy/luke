package org.apache.lucene.luke.app.desktop.util;

import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TableUtil {

  public static void setupTable(JTable table, int selectionModel, TableModel model, MouseListener mouseListener,
                                int... colWidth) {
    table.setFillsViewportHeight(true);
    table.setFont(StyleConstants.FONT_MONOSPACE_LARGE);
    table.setRowHeight(StyleConstants.TABLE_ROW_HEIGHT_DEFAULT);
    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setGridColor(Color.lightGray);
    table.getColumnModel().setColumnMargin(StyleConstants.TABLE_COLUMN_MARGIN_DEFAULT);
    table.setRowMargin(StyleConstants.TABLE_ROW_MARGIN_DEFAULT);
    table.setSelectionMode(selectionModel);
    if (model != null) {
      table.setModel(model);
    } else {
      table.setModel(new DefaultTableModel());
    }
    if (mouseListener != null) {
      table.removeMouseListener(mouseListener);
      table.addMouseListener(mouseListener);
    }
    for (int i = 0; i < colWidth.length; i++) {
      table.getColumnModel().getColumn(i).setMinWidth(colWidth[i]);
      table.getColumnModel().getColumn(i).setMaxWidth(colWidth[i]);
    }
  }

  public static void setEnabled(JTable table, boolean enabled) {
    table.setEnabled(enabled);
    if (enabled) {
      table.setRowSelectionAllowed(true);
      table.setForeground(Color.black);
      table.setBackground(Color.white);
    } else {
      table.setRowSelectionAllowed(false);
      table.setForeground(Color.gray);
      table.setBackground(Color.lightGray);
    }
  }

  public static <T extends TableColumnInfo> String[] columnNames(T[] columns) {
    return columnMap(columns).entrySet().stream().map(e -> e.getValue().getColName()).toArray(String[]::new);
  }

  public static <T extends TableColumnInfo> TreeMap<Integer, T> columnMap(T[] columns) {
    return Arrays.stream(columns).collect(Collectors.toMap(T::getIndex, UnaryOperator.identity(), (e1, e2) -> e1, TreeMap::new));
  }

  private TableUtil() {}

}
