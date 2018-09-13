package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class SearchSortPaneProvider implements Provider<JScrollPane> {

  private final JComboBox<String> fieldCombo1 = new JComboBox<>();

  private final JComboBox<String> typeCombo1 = new JComboBox<>();

  private final JComboBox<String> orderCombo1 = new JComboBox<>(new String[]{"ASC", "DESC"});

  private final JComboBox<String> fieldCombo2 = new JComboBox<>();

  private final JComboBox<String> typeCombo2 = new JComboBox<>();

  private final JComboBox<String> orderCombo2 = new JComboBox<>(new String[]{"ASC", "DESC"});


  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(sortSettings());

    return new JScrollPane(panel);
  }

  private JPanel sortSettings() {
    JPanel panel = new JPanel(new GridLayout(5, 1));
    panel.setMaximumSize(new Dimension(500, 200));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.primary")));

    JPanel primary = new JPanel(new FlowLayout(FlowLayout.LEADING));
    primary.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    primary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.field")));
    primary.add(fieldCombo1);
    primary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.type")));
    typeCombo1.setEnabled(false);
    primary.add(typeCombo1);
    primary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.order")));
    orderCombo1.setEnabled(false);
    primary.add(orderCombo1);
    panel.add(primary);

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.secondary")));

    JPanel secondary = new JPanel(new FlowLayout(FlowLayout.LEADING));
    secondary.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    secondary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.field")));
    secondary.add(fieldCombo2);
    secondary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.type")));
    typeCombo2.setEnabled(false);
    secondary.add(typeCombo2);
    secondary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.order")));
    orderCombo2.setEnabled(false);
    secondary.add(orderCombo2);
    panel.add(secondary);

    JPanel clear = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JButton clearBtn = new JButton(MessageUtils.getLocalizedMessage("search_sort.button.clear"));
    clear.add(clearBtn);
    panel.add(clear);

    return panel;
  }
}
