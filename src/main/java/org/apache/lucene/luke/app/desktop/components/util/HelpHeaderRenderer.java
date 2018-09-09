package org.apache.lucene.luke.app.desktop.components.util;

import org.apache.lucene.luke.app.desktop.components.dialog.HelpDialogFactory;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

/** Cell render class for table header with help dialog. */
public class HelpHeaderRenderer implements TableCellRenderer {

  private JTable table;

  private final JPanel panel = new JPanel();

  private final JComponent helpContent;

  private final HelpDialogFactory helpDialogFactory;

  private final String title;

  private final String desc;

  public HelpHeaderRenderer(String title, String desc, JComponent helpContent, HelpDialogFactory helpDialogFactory) {
    this.desc = desc;
    this.helpContent = helpContent;
    this.helpDialogFactory = helpDialogFactory;
    this.title = title;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (table != null && this.table != table) {
      this.table = table;
      final JTableHeader header = table.getTableHeader();
      if (header != null) {
        panel.setLayout(new FlowLayout(FlowLayout.LEADING));
        panel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        panel.add(new JLabel(value.toString()));

        // add label with mouse click listener
        // when the label is clicked, help dialog will be displayed.
        JLabel helpLabel = new JLabel(MessageUtils.getLocalizedMessage("label.help"),
            ImageUtils.createImageIcon("/img/icon_question_alt2.png", 12, 12),
            JLabel.LEFT);
        helpLabel.setIconTextGap(5);
        helpLabel.setForeground(Color.decode("#0099ff"));
        Font font = helpLabel.getFont();
        Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        helpLabel.setFont(font.deriveFont(attributes));
        panel.add(helpLabel);

        // add mouse listener to JTableHeader object.
        // see: https://stackoverflow.com/questions/7137786/how-can-i-put-a-control-in-the-jtableheader-of-a-jtable
        header.addMouseListener(new HelpClickListener(column));
      }
    }
    return panel;
  }

  class HelpClickListener extends MouseAdapter {

    int column;

    HelpClickListener(int column) {
      this.column = column;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      showPopupIfNeeded(e);
    }

    private void showPopupIfNeeded(MouseEvent e) {
      JTableHeader header = (JTableHeader) e.getSource();
      int column = header.getTable().columnAtPoint(e.getPoint());
      if (column == this.column && e.getClickCount() == 1 && column != -1) {
        // only when the targeted column header is clicked, pop up the dialog
        new DialogOpener<>(helpDialogFactory).open(title, 600, 350,
            (factory) -> {
              factory.setDesc(desc);
              factory.setContent(helpContent);
            });
      }
    }

  }
}
