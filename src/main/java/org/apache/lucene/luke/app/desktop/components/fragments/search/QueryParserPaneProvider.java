package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class QueryParserPaneProvider implements Provider<JScrollPane> {

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(selectParserPane());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(parserSettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(phraseQuerySettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(fuzzyQuerySettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(dateRangeQuerySettings());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(pointRangeQuerySettings());

    return new JScrollPane(panel);
  }

  private JPanel selectParserPane() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    JRadioButton standardRB = new JRadioButton("StandardQueryParser");
    standardRB.setActionCommand("standard");
    standardRB.setSelected(true);

    JRadioButton classicRB = new JRadioButton("Classic QueryParser");
    classicRB.setActionCommand("classic");

    ButtonGroup group = new ButtonGroup();
    group.add(standardRB);
    group.add(classicRB);

    panel.add(standardRB);
    panel.add(classicRB);

    return panel;
  }

  private JPanel parserSettings() {
    JPanel panel = new JPanel(new GridLayout(3, 2));

    JPanel defField = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JLabel dfLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.df"));
    defField.add(dfLabel);
    JComboBox<String> dfCB = new JComboBox<>();
    defField.add(dfCB);
    panel.add(defField);

    JPanel defOp = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JLabel defOpLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.dop"));
    defOp.add(defOpLabel);
    JComboBox<String> defOpCB = new JComboBox<>(new String[]{"OR", "AND"});
    defOp.add(defOpCB);
    panel.add(defOp);

    JCheckBox posIncCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_parser.checkbox.pos_incr"));
    panel.add(posIncCB);

    JCheckBox wildCardCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_parser.checkbox.lead_wildcard"));
    panel.add(wildCardCB);

    JCheckBox splitWS = new JCheckBox(MessageUtils.getLocalizedMessage("search_parser.checkbox.split_ws"));
    panel.add(splitWS);

    return panel;
  }

  private JPanel phraseQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.phrase_query")));
    panel.add(header);

    JPanel genPQ = new JPanel(new FlowLayout(FlowLayout.LEADING));
    genPQ.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JCheckBox genPQCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_parser.checkbox.gen_pq"));
    genPQ.add(genPQCB);
    panel.add(genPQ);

    JPanel genMTPQ = new JPanel(new FlowLayout(FlowLayout.LEADING));
    genMTPQ.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JCheckBox genMTPQCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_parser.checkbox.gen_mts"));
    genMTPQ.add(genMTPQCB);
    panel.add(genMTPQ);

    JPanel slop = new JPanel(new FlowLayout(FlowLayout.LEADING));
    slop.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel slopLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.phrase_slop"));
    slop.add(slopLabel);
    JTextField slopTF = new JTextField(5);
    slop.add(slopTF);
    slop.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.int_required")));
    panel.add(slop);

    return panel;
  }

  private JPanel fuzzyQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_query")));
    panel.add(header);

    JPanel minSim = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minSim.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel minSimLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_minsim"));
    minSim.add(minSimLabel);
    JTextField minSimTF = new JTextField(5);
    minSim.add(minSimTF);
    minSim.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.float_required")));
    panel.add(minSim);

    JPanel prefLen = new JPanel(new FlowLayout(FlowLayout.LEADING));
    prefLen.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel prefLenLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.fuzzy_preflen"));
    prefLen.add(prefLenLabel);
    JTextField prefLenTF = new JTextField(5);
    prefLen.add(prefLenTF);
    prefLen.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.int_required")));
    panel.add(prefLen);

    return panel;
  }

  private JPanel dateRangeQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.daterange_query")));
    panel.add(header);

    JPanel resolution = new JPanel(new FlowLayout(FlowLayout.LEADING));
    resolution.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel resLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.date_res"));
    resolution.add(resLabel);
    JComboBox<String> resCB = new JComboBox<>();
    resolution.add(resCB);
    panel.add(resolution);

    JPanel locale = new JPanel(new FlowLayout(FlowLayout.LEADING));
    locale.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JLabel locLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.locale"));
    locale.add(locLabel);
    JTextField locTF = new JTextField(10);
    locale.add(locTF);
    JLabel tzLabel = new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.timezone"));
    locale.add(tzLabel);
    JTextField tzTF = new JTextField(10);
    locale.add(tzTF);
    panel.add(locale);

    return panel;
  }

  private JPanel pointRangeQuerySettings() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.pointrange_query")));
    panel.add(header);

    JPanel headerNote = new JPanel(new FlowLayout(FlowLayout.LEADING));
    headerNote.add(new JLabel(MessageUtils.getLocalizedMessage("search_parser.label.pointrange_hint")));
    panel.add(headerNote);

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Field", "Numeric Type"};
    JTable table = new JTable(data, columnNames);
    table.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane);

    return panel;
  }
}
