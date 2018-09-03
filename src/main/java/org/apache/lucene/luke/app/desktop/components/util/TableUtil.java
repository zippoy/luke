package org.apache.lucene.luke.app.desktop.components.util;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.event.MouseListener;

public class TableUtil {

  public static void setupTable(JTable table, int selectionModel, TableModel model, MouseListener mouseListener) {
    table.setFillsViewportHeight(true);
    table.setFont(StyleConstants.TABLE_FONT_DEFAULT);
    table.setRowHeight(StyleConstants.TABLE_ROW_HEIGHT_DEFAULT);
    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setGridColor(Color.lightGray);
    table.getColumnModel().setColumnMargin(StyleConstants.TABLE_COLUMN_MARGIN_DEFAULT);
    table.setRowMargin(StyleConstants.TABLE_ROW_MARGIN_DEFAULT);
    table.setSelectionMode(selectionModel);
    if (model != null) {
      table.setModel(model);
    }
    if (mouseListener != null) {
      table.addMouseListener(mouseListener);
    }
  }

  private TableUtil() {}

}
