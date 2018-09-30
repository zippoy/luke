package org.apache.lucene.luke.app.desktop.util;

import org.apache.lucene.luke.app.desktop.LukeMain;
import org.apache.lucene.luke.app.desktop.components.dialog.HelpDialogFactory;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/** Cell render class for table header with help dialog. */
public class HelpHeaderRenderer implements TableCellRenderer {

  private JTable table;

  private final JPanel panel = new JPanel();

  private final JComponent helpContent;

  private final HelpDialogFactory helpDialogFactory;

  private final String title;

  private final String desc;

  private final JDialog parent;

  public HelpHeaderRenderer(String title, String desc, JComponent helpContent, HelpDialogFactory helpDialogFactory) {
    this(title, desc, helpContent, helpDialogFactory, null);
  }

  public HelpHeaderRenderer(String title, String desc, JComponent helpContent, HelpDialogFactory helpDialogFactory,
                            JDialog parent) {
    this.title = title;
    this.desc = desc;
    this.helpContent = helpContent;
    this.helpDialogFactory = helpDialogFactory;
    this.parent = parent;
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
        panel.add(FontUtil.toLinkText(helpLabel));

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
        if (Objects.nonNull(parent)) {
          new DialogOpener<>(helpDialogFactory).open(parent, title, 600, 350,
              (factory) -> {
                factory.setDesc(desc);
                factory.setContent(helpContent);
              });
        } else {
          new DialogOpener<>(helpDialogFactory).open(title, 600, 350,
              (factory) -> {
                factory.setDesc(desc);
                factory.setContent(helpContent);
              });
        }
      }
    }

  }
}
