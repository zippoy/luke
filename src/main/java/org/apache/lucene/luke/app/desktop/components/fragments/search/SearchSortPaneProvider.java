package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class SearchSortPaneProvider implements Provider<JScrollPane> {

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
    JComboBox<String> fieldsCB = new JComboBox<>(new String[]{});
    primary.add(fieldsCB);
    primary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.type")));
    JComboBox<String> typeCB = new JComboBox<>(new String[]{});
    primary.add(typeCB);
    primary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.order")));
    JComboBox<String> orderCB = new JComboBox<>(new String[]{});
    primary.add(orderCB);
    panel.add(primary);

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.secondary")));

    JPanel secondary = new JPanel(new FlowLayout(FlowLayout.LEADING));
    secondary.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    secondary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.field")));
    JComboBox<String> fieldsCB2 = new JComboBox<>(new String[]{});
    secondary.add(fieldsCB2);
    secondary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.type")));
    JComboBox<String> typeCB2 = new JComboBox<>(new String[]{});
    secondary.add(typeCB2);
    secondary.add(new JLabel(MessageUtils.getLocalizedMessage("search_sort.label.order")));
    JComboBox<String> orderCB2 = new JComboBox<>(new String[]{});
    secondary.add(orderCB2);
    panel.add(secondary);

    JPanel clear = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JButton clearBtn = new JButton(MessageUtils.getLocalizedMessage("search_sort.button.clear"));
    clear.add(clearBtn);
    panel.add(clear);

    return panel;
  }
}
