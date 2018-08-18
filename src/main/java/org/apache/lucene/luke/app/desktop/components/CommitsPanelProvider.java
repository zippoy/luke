package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class CommitsPanelProvider implements IndexObserver, DirectoryObserver, Provider<JPanel> {

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(120);
    panel.add(splitPane);

    return panel;
  }

  private JPanel createUpperPanel() {
    JPanel panel = new JPanel(new BorderLayout(20, 0));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING));
    left.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.select_gen")));
    JComboBox<String> segGenCB = new JComboBox<>();
    left.add(segGenCB);
    panel.add(left, BorderLayout.LINE_START);

    JPanel right = new JPanel(new GridBagLayout());
    GridBagConstraints c1 = new GridBagConstraints();
    c1.ipadx = 5;
    c1.ipady = 5;

    c1.gridx = 0;
    c1.gridy = 0;
    c1.weightx = 0.2;
    c1.anchor = GridBagConstraints.EAST;
    right.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.deleted")), c1);

    JLabel deletedLabel = new JLabel("false");
    c1.gridx = 1;
    c1.gridy = 0;
    c1.weightx = 0.5;
    c1.anchor = GridBagConstraints.WEST;
    right.add(deletedLabel, c1);

    c1.gridx = 0;
    c1.gridy = 1;
    c1.weightx = 0.2;
    c1.anchor = GridBagConstraints.EAST;
    right.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.segcount")), c1);

    JLabel segCntLabel = new JLabel("1");
    c1.gridx = 1;
    c1.gridy = 1;
    c1.weightx = 0.5;
    c1.anchor = GridBagConstraints.WEST;
    right.add(segCntLabel, c1);

    c1.gridx = 0;
    c1.gridy = 2;
    c1.weightx = 0.2;
    c1.anchor = GridBagConstraints.EAST;
    right.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.userdata")), c1);

    JTextArea userDataArea = new JTextArea(3, 30);
    JScrollPane userDataScroll = new JScrollPane(userDataArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    c1.gridx = 1;
    c1.gridy = 2;
    c1.weightx = 0.5;
    c1.anchor = GridBagConstraints.WEST;
    right.add(userDataScroll, c1);

    panel.add(right, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createFilesPanel(), createSegmentsPanel());
    splitPane.setDividerLocation(300);
    panel.add(splitPane);
    return panel;
  }

  private JPanel createFilesPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.files")));
    panel.add(header, BorderLayout.PAGE_START);

    String[][] data = new String[][]{};
    String[] columnNames = new String[]{"Filename", "Size"};
    JTable fileTable = new JTable(data, columnNames);
    fileTable.setFillsViewportHeight(true);
    panel.add(new JScrollPane(fileTable), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSegmentsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JPanel segments = new JPanel(new FlowLayout(FlowLayout.LEADING));
    segments.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.segments")));
    panel.add(segments);

    String[][] data = new String[][]{};
    String[] colNames = new String[]{"Name", "Max docs", "Dels", "Del gen", "Lucene ver.", "Codec", "Size"};
    JTable segTable = new JTable(data, colNames);
    segTable.setFillsViewportHeight(true);
    panel.add(new JScrollPane(segTable));

    JPanel segDetails = new JPanel(new FlowLayout(FlowLayout.LEADING));
    segDetails.add(new JLabel(MessageUtils.getLocalizedMessage("commits.label.segdetails")));
    panel.add(segDetails);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING));

    JRadioButton diagRB = new JRadioButton("Diagnostics");
    diagRB.setActionCommand("diagnostics");
    diagRB.setSelected(true);
    buttons.add(diagRB);

    JRadioButton attRB = new JRadioButton("Attributes");
    attRB.setActionCommand("attributes");
    attRB.setSelected(false);
    buttons.add(attRB);

    JRadioButton codecRB = new JRadioButton("Codec");
    codecRB.setActionCommand("codec");
    codecRB.setSelected(false);
    buttons.add(codecRB);

    ButtonGroup group = new ButtonGroup();
    group.add(diagRB);
    group.add(attRB);
    group.add(codecRB);

    panel.add(buttons);

    JList<String> segDetailList = new JList<>();
    segDetailList.setVisibleRowCount(10);
    panel.add(new JScrollPane(segDetailList));

    return panel;
  }

  @Override
  public void openIndex(LukeState state) {

  }

  @Override
  public void closeIndex() {

  }

  @Override
  public void openDirectory(LukeState state) {

  }

  @Override
  public void closeDirectory() {

  }
}
