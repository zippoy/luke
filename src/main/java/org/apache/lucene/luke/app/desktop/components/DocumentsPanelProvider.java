package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.components.dialog.HelpDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.AddDocumentDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.DocValuesDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.StoredValueDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.TermVectorDialogFactory;
import org.apache.lucene.luke.app.desktop.components.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.components.util.StyleConstants;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.listeners.DocumentsPanelListeners;
import org.apache.lucene.luke.app.desktop.components.util.HelpHeaderRenderer;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.documents.DocValues;
import org.apache.lucene.luke.models.documents.DocumentField;
import org.apache.lucene.luke.models.documents.Documents;
import org.apache.lucene.luke.models.documents.DocumentsFactory;
import org.apache.lucene.luke.models.documents.TermPosting;
import org.apache.lucene.luke.models.documents.TermVectorEntry;
import org.apache.lucene.luke.util.BytesRefUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DocumentsPanelProvider implements Provider<JPanel> {

  private final DocumentsFactory documentsFactory;

  private final MessageBroker messageBroker;

  private final DocumentsPanelListeners listeners;

  private final Controller controller = new Controller();

  private final AddDocumentDialogFactory addDocDialogFactory;

  private final TermVectorDialogFactory tvDialogFactory;

  private final DocValuesDialogFactory dvDialogFactory;

  private final StoredValueDialogFactory valueDialogFactory;

  private final HelpDialogFactory helpDialogFactory;

  private final JComboBox<String> fieldsCB = new JComboBox<>();

  private final JButton firstTermBtn = new JButton();

  private final JTextField termTF = new JTextField();

  private final JButton nextTermBtn = new JButton();

  private final JTextField selectedTermTF = new JTextField();

  private final JButton firstTermDocBtn = new JButton();

  private final JTextField termDocIdxTF = new JTextField();

  private final JButton nextTermDocBtn = new JButton();

  private final JLabel termDocsNumLbl = new JLabel();

  private final JTable posTable = new JTable();

  private final JSpinner docNumSpnr = new JSpinner();

  private final JLabel maxDocsLbl = new JLabel();

  private final JButton mltBtn = new JButton();

  private final JButton addDocBtn = new JButton();

  private final JTable documentTable = new JTable();

  public class Controller {

    private Documents documentsModel;

    private void setDocumentsModel(Documents documentsModel) {
      this.documentsModel = documentsModel;
    }

    public void showFirstTerm() {
      String fieldName = (String)fieldsCB.getSelectedItem();
      if (fieldName == null || fieldName.length() == 0) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.field.message.not_selected"));
        return;
      }

      termDocIdxTF.setText("");
      clearPosTable();

      String firstTermText = documentsModel.firstTerm(fieldName).map(Term::text).orElse("");
      termTF.setText(firstTermText);
      selectedTermTF.setText(firstTermText);
      if (selectedTermTF.getText().length() > 0) {
        String num = documentsModel.getDocFreq().map(String::valueOf).orElse("?");
        termDocsNumLbl.setText(String.format("in %s docs", num));

        nextTermBtn.setEnabled(true);
        termTF.setEditable(true);
        firstTermDocBtn.setEnabled(true);
      } else {
        nextTermBtn.setEnabled(false);
        termTF.setEditable(false);
        firstTermDocBtn.setEnabled(false);
      }
      nextTermDocBtn.setEnabled(false);
      messageBroker.clearStatusMessage();
    }

    public void showNextTerm() {
      termDocIdxTF.setText("");
      clearPosTable();

      String nextTermText = documentsModel.nextTerm().map(Term::text).orElse("");
      termTF.setText(nextTermText);
      selectedTermTF.setText(nextTermText);
      if (selectedTermTF.getText().length() > 0) {
        String num = documentsModel.getDocFreq().map(String::valueOf).orElse("?");
        termDocsNumLbl.setText(String.format("in %s docs", num));

        termTF.setEditable(true);
        firstTermDocBtn.setEnabled(true);
      } else {
        nextTermBtn.setEnabled(false);
        termTF.setEditable(false);
        firstTermDocBtn.setEnabled(false);
      }
      nextTermDocBtn.setEnabled(false);
      messageBroker.clearStatusMessage();
    }

    public void seekNextTerm() {
      termDocIdxTF.setText("");
      clearPosTable();

      String termText = termTF.getText();

      String nextTermText = documentsModel.seekTerm(termText).map(Term::text).orElse("");
      termTF.setText(nextTermText);
      selectedTermTF.setText(nextTermText);
      if (selectedTermTF.getText().length() > 0) {
        String num = documentsModel.getDocFreq().map(String::valueOf).orElse("?");
        termDocsNumLbl.setText(String.format("in %s docs", num));

        termTF.setEditable(true);
        firstTermDocBtn.setEnabled(true);
      } else {
        nextTermBtn.setEnabled(false);
        termTF.setEditable(false);
        firstTermDocBtn.setEnabled(false);
      }
      nextTermDocBtn.setEnabled(false);
      messageBroker.clearStatusMessage();
    }

    public void showFirstTermDoc() {
      int doc = documentsModel.firstTermDoc().orElse(-1);
      if (doc < 0) {
        nextTermDocBtn.setEnabled(false);
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.termdocs.message.not_available"));
        return;
      }
      termDocIdxTF.setText(String.valueOf(1));
      //docNumSpnr.setValue(doc);
      showDoc(doc);

      List<TermPosting> postings = documentsModel.getTermPositions();
      posTable.setModel(new PosTableModel(postings));
      posTable.getColumnModel().getColumn(PosTableModel.Column.POSITION.getIndex()).setPreferredWidth(80);
      posTable.getColumnModel().getColumn(PosTableModel.Column.OFFSETS.getIndex()).setPreferredWidth(120);
      posTable.getColumnModel().getColumn(PosTableModel.Column.PAYLOAD.getIndex()).setPreferredWidth(170);

      nextTermDocBtn.setEnabled(true);
      messageBroker.clearStatusMessage();
    }

    public void showNextTermDoc() {
      int doc = documentsModel.nextTermDoc().orElse(-1);
      if (doc < 0) {
        nextTermDocBtn.setEnabled(false);
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.termdocs.message.not_available"));
        return;
      }
      int curIdx = Integer.parseInt(termDocIdxTF.getText());
      termDocIdxTF.setText(String.valueOf(curIdx + 1));
      //docNumSpnr.setValue(doc);
      showDoc(doc);

      List<TermPosting> postings = documentsModel.getTermPositions();
      posTable.setModel(new PosTableModel(postings));

      nextTermDocBtn.setDefaultCapable(true);
      messageBroker.clearStatusMessage();
    }

    public int getMLTDocNum() {
      return (int)docNumSpnr.getValue();
    }

    public void showAddDocumentDialog() {
      new DialogOpener<>(addDocDialogFactory).open("Add document", 600, 500,
          (factory) -> {
          });
    }

    public void showLatestDoc() {
      int docid = documentsModel.getMaxDoc() - 1;
      showDoc(docid);
    }

    private void showDoc(int docid) {
      docNumSpnr.setValue(docid);

      List<DocumentField> doc = documentsModel.getDocumentFields(docid);
      documentTable.setModel(new DocumentTableModel(doc));
      documentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      documentTable.setFont(StyleConstants.FONT_MONOSPACE_LARGE);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.FIELD.getIndex()).setPreferredWidth(150);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.FLAGS.getIndex()).setMinWidth(240);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.FLAGS.getIndex()).setMaxWidth(240);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.NORM.getIndex()).setMinWidth(80);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.NORM.getIndex()).setMaxWidth(80);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.VALUE.getIndex()).setPreferredWidth(1000);
      documentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      TableCellRenderer renderer = new HelpHeaderRenderer(
          "About Flags", "Format: IdfpoNPSB#txxVDtxxxxTx/x",
          createFlagsHelpDialog(),
          helpDialogFactory);
      documentTable.getColumnModel().getColumn(DocumentTableModel.Column.FLAGS.getIndex()).setHeaderRenderer(renderer);

      messageBroker.clearStatusMessage();
    }

    private JComponent createFlagsHelpDialog() {
      String[] values = new String[]{
          "I - index options(docs, frequencies, positions, offsets)",
          "N - norms",
          "P - payloads",
          "S - stored",
          "B - binary stored values",
          "#txx - numeric stored values(type, precision)",
          "V - term vectors",
          "Dtxxxxx - doc values(type)",
          "Tx/x - point values(num bytes/dimension)"
      };
      JList<String> list = new JList<>(values);
      return new JScrollPane(list);
    }

    public void showCurrentDoc() {
      int docid = (Integer)docNumSpnr.getValue();
      showDoc(docid);
    }

    public void showTermVectorDialog() {
      int docid = (Integer)docNumSpnr.getValue();
      String field = (String)documentTable.getModel().getValueAt(documentTable.getSelectedRow(), DocumentTableModel.Column.FIELD.getIndex());
      List<TermVectorEntry> tvEntries = documentsModel.getTermVectors(docid, field);
      if (tvEntries.isEmpty()) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.termvector.message.not_available", field, docid));
        return;
      }

      new DialogOpener<>(tvDialogFactory).open(
          "Term Vector", 400, 300,
          (factory) -> {
            factory.setField(field);
            factory.setTvEntries(tvEntries);
          });
      messageBroker.clearStatusMessage();
    }

    public void showDocValuesDialog() {
      int docid = (Integer)docNumSpnr.getValue();
      String field = (String)documentTable.getModel().getValueAt(documentTable.getSelectedRow(), DocumentTableModel.Column.FIELD.getIndex());
      Optional<DocValues> docValues = documentsModel.getDocValues(docid, field);
      if (docValues.isPresent()) {
        new DialogOpener<>(dvDialogFactory).open(
            "Doc Values", 400, 300,
            (factory) -> {
              factory.setValue(field, docValues.get());
            });
        messageBroker.clearStatusMessage();
      } else {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.docvalues.message.not_available", field, docid));
      }
    }

    public void showStoredValueDialog() {
      int docid = (Integer)docNumSpnr.getValue();
      String field = (String)documentTable.getModel().getValueAt(documentTable.getSelectedRow(), DocumentTableModel.Column.FIELD.getIndex());
      String value = (String)documentTable.getModel().getValueAt(documentTable.getSelectedRow(), DocumentTableModel.Column.VALUE.getIndex());
      if (Objects.isNull(value)) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.stored.message.not_availabe", field, docid));
        return;
      }
      new DialogOpener<>(valueDialogFactory).open(
          "Stored Value", 400, 300,
          (factory) -> {
            factory.setField(field);
            factory.setValue(value);
          });
      messageBroker.clearStatusMessage();
    }

    public void copyStoredValue() {
      int docid = (Integer)docNumSpnr.getValue();
      String field = (String)documentTable.getModel().getValueAt(documentTable.getSelectedRow(), DocumentTableModel.Column.FIELD.getIndex());
      String value = (String)documentTable.getModel().getValueAt(documentTable.getSelectedRow(), DocumentTableModel.Column.VALUE.getIndex());
      if (Objects.isNull(value)) {
        messageBroker.showStatusMessage(MessageUtils.getLocalizedMessage("documents.stored.message.not_availabe", field, docid));
        return;
      }
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection selection = new StringSelection(value);
      clipboard.setContents(selection, null);
      messageBroker.clearStatusMessage();
    }

    private void clearPosTable() {
      TableUtil.setupTable(posTable, ListSelectionModel.SINGLE_SELECTION, new PosTableModel(), null, 80, 120);
    }

    private void clearDocumentTable() {
      documentTable.setModel(new DocumentTableModel());
    }

    private Controller() {}
  }

  public class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      Documents documentsModel = documentsFactory.newInstance(state.getIndexReader());
      controller.setDocumentsModel(documentsModel);

      addDocBtn.setEnabled(!state.readOnly() && state.hasDirectoryReader());

      int maxDoc = documentsModel.getMaxDoc();
      maxDocsLbl.setText(String.format("in %d docs", maxDoc));
      if (maxDoc > 0) {
        int max = Math.max(maxDoc - 1, 0);
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, max, 1);
        docNumSpnr.setModel(spinnerModel);
        docNumSpnr.setEnabled(true);
        controller.showDoc(0);
      } else {
        docNumSpnr.setEnabled(false);
      }

      documentsModel.getFieldNames().stream().sorted().forEach(fieldsCB::addItem);
    }

    @Override
    public void closeIndex() {
      maxDocsLbl.setText("in ? docs");
      docNumSpnr.setEnabled(false);
      fieldsCB.removeAllItems();
      termTF.setText("");
      selectedTermTF.setText("");
      termDocsNumLbl.setText("");
      termDocIdxTF.setText("");

      controller.clearPosTable();
      controller.clearDocumentTable();
    }
  }

  class DocumentsTabOperatorImpl implements DocumentsTabOperator {

    @Override
    public void browseTerm(String field, String term) {
      fieldsCB.setSelectedItem(field);
      termTF.setText(term);
      controller.seekNextTerm();
    }

    @Override
    public void displayLatestDoc() {
      controller.showLatestDoc();
    }

    @Override
    public void displayDoc(int docid) {
      controller.showDoc(docid);
    }
  }

  @Inject
  public DocumentsPanelProvider(DocumentsFactory documentsFactory,
                                MessageBroker messageBroker,
                                IndexHandler indexHandler,
                                TabbedPaneProvider.TabSwitcherProxy tabSwitcher,
                                ComponentOperatorRegistry operatorRegistry,
                                AddDocumentDialogFactory addDocDialogFactory,
                                TermVectorDialogFactory tvDialogFactory,
                                DocValuesDialogFactory dvDialogFactory,
                                StoredValueDialogFactory valueDialogFactory,
                                HelpDialogFactory helpDialogFactory) {
    this.documentsFactory = documentsFactory;
    this.messageBroker = messageBroker;
    this.listeners = new DocumentsPanelListeners(controller, tabSwitcher, operatorRegistry);
    this.addDocDialogFactory = addDocDialogFactory;
    this.tvDialogFactory = tvDialogFactory;
    this.dvDialogFactory = dvDialogFactory;
    this.valueDialogFactory = valueDialogFactory;
    this.helpDialogFactory = helpDialogFactory;

    indexHandler.addObserver(new Observer());
    operatorRegistry.register(DocumentsTabOperator.class, new DocumentsTabOperatorImpl());
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.setBorder(BorderFactory.createLineBorder(Color.gray));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPanel(), createLowerPanel());
    splitPane.setDividerLocation(0.4);
    panel.add(splitPane);
    return panel;
  }

  private JPanel createUpperPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(browseTermsPanel(), c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(browseDocsByTermPanel(), c);

    return panel;
  }

  private JPanel browseTermsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("documents.label.browse_terms"));
    top.add(label);

    panel.add(top, BorderLayout.PAGE_START);

    JPanel center = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    fieldsCB.addActionListener(listeners.getFieldsCBListener());
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 0.0;
    c.gridwidth = 2;
    center.add(fieldsCB, c);

    firstTermBtn.setText(MessageUtils.getLocalizedMessage("documents.button.first_term"));
    firstTermBtn.setIcon(ImageUtils.createImageIcon("/img/arrow_carrot-2left.png", 20, 20));
    firstTermBtn.setMaximumSize(new Dimension(80, 30));
    firstTermBtn.addActionListener(listeners.getFirstTermBtnListener());
    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 0.2;
    c.gridwidth = 1;
    center.add(firstTermBtn, c);

    termTF.setColumns(20);
    termTF.setMinimumSize(new Dimension(50, 25));
    termTF.setFont(StyleConstants.FONT_MONOSPACE_LARGE);
    termTF.addActionListener(listeners.getTermTFListener());
    c.gridx = 1;
    c.gridy = 1;
    c.insets = new Insets(5, 5,5, 5);
    c.weightx = 0.5;
    c.gridwidth = 1;
    center.add(termTF, c);

    nextTermBtn.setText(MessageUtils.getLocalizedMessage("documents.button.next"));
    nextTermBtn.addActionListener(listeners.getNextTermBtnListener());
    c.gridx = 2;
    c.gridy = 1;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 0.1;
    c.gridwidth = 1;
    center.add(nextTermBtn, c);

    panel.add(center, BorderLayout.CENTER);

    return panel;
  }

  private JPanel browseDocsByTermPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel center = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    JLabel label = new JLabel(MessageUtils.getLocalizedMessage("documents.label.browse_doc_by_term"));
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.gridwidth = 2;
    c.insets = new Insets(5, 5, 5, 5);
    center.add(label, c);

    selectedTermTF.setColumns(20);
    selectedTermTF.setFont(StyleConstants.FONT_MONOSPACE_LARGE);
    selectedTermTF.setEditable(false);
    selectedTermTF.setBackground(Color.white);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.gridwidth = 2;
    c.insets = new Insets(5, 5, 5, 5);
    center.add(selectedTermTF, c);

    firstTermDocBtn.setText(MessageUtils.getLocalizedMessage("documents.button.first_termdoc"));
    firstTermDocBtn.setIcon(ImageUtils.createImageIcon("/img/arrow_carrot-2left.png", 20, 20));
    firstTermDocBtn.addActionListener(listeners.getFirstTermDocBtnListener());
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = new Insets(5, 3, 5, 5);
    center.add(firstTermDocBtn, c);

    termDocIdxTF.setEditable(false);
    termDocIdxTF.setBackground(Color.white);
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.5;
    c.gridwidth = 1;
    c.insets = new Insets(5, 5, 5, 5);
    center.add(termDocIdxTF, c);

    nextTermDocBtn.setText(MessageUtils.getLocalizedMessage("documents.button.next"));
    nextTermDocBtn.addActionListener(listeners.getNextTermDocBtnListener());
    c.gridx = 2;
    c.gridy = 2;
    c.weightx = 0.2;
    c.gridwidth = 1;
    c.insets = new Insets(5, 5, 5, 5);
    center.add(nextTermDocBtn, c);

    termDocsNumLbl.setText("in ? docs");
    c.gridx = 3;
    c.gridy = 2;
    c.weightx = 0.3;
    c.gridwidth = 1;
    c.insets = new Insets(5, 5, 5, 5);
    center.add(termDocsNumLbl, c);

    TableUtil.setupTable(posTable, ListSelectionModel.SINGLE_SELECTION, new PosTableModel(), null, 80, 120);
    JScrollPane scrollPane = new JScrollPane(posTable);
    scrollPane.setMinimumSize(new Dimension(100, 100));
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 4;
    c.insets = new Insets(5, 5, 5, 5);
    center.add(scrollPane, c);

    panel.add(center, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createLowerPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel browseDocsPanel = new JPanel();
    browseDocsPanel.setLayout(new BoxLayout(browseDocsPanel, BoxLayout.PAGE_AXIS));
    browseDocsPanel.add(createBrowseDocsBar());

    JPanel browseDocsNote = new JPanel(new FlowLayout(FlowLayout.LEADING));
    browseDocsNote.add(new JLabel(MessageUtils.getLocalizedMessage("documents.label.doc_table_note")));
    browseDocsPanel.add(browseDocsNote);

    panel.add(browseDocsPanel, BorderLayout.PAGE_START);

    TableUtil.setupTable(documentTable, ListSelectionModel.SINGLE_SELECTION, new DocumentTableModel(), listeners.getDocumentTableListener());
    JPanel flagsHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
    flagsHeader.add(new JLabel("Flags"));
    flagsHeader.add(new JLabel("Help"));
    documentTable.getColumnModel().getColumn(DocumentTableModel.Column.FLAGS.getIndex()).setHeaderValue(flagsHeader);
    JScrollPane scrollPane = new JScrollPane(documentTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createBrowseDocsBar() {
    JPanel panel = new JPanel(new GridLayout(1, 2));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
    JLabel label = new JLabel(
        MessageUtils.getLocalizedMessage("documents.label.browse_doc_by_idx"),
        ImageUtils.createImageIcon("/img/icon_document_alt.png", 20, 20),
        JLabel.LEFT);
    left.add(label);
    docNumSpnr.setPreferredSize(new Dimension(100, 25));
    docNumSpnr.addChangeListener(listeners.getDocNumSpnrListener());
    left.add(docNumSpnr);
    maxDocsLbl.setText("in ? docs");
    left.add(maxDocsLbl);
    panel.add(left);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 2));
    mltBtn.setText(MessageUtils.getLocalizedMessage("documents.button.mlt"));
    mltBtn.setIcon(ImageUtils.createImageIcon("/img/icon_heart_alt.png", 15, 15));
    mltBtn.setMargin(new Insets(3, 5, 3, 5));
    mltBtn.addActionListener(listeners.getMltSearchBtnListener());
    right.add(mltBtn);
    addDocBtn.setText(MessageUtils.getLocalizedMessage("documents.button.add"));
    addDocBtn.setIcon(ImageUtils.createImageIcon("/img/icon_plus-box.png", 15, 15));
    addDocBtn.setMargin(new Insets(3, 5, 3, 5));
    addDocBtn.addActionListener(listeners.getAddDocBtnListener());
    right.add(addDocBtn);
    panel.add(right);

    return panel;
  }

  public interface DocumentsTabOperator extends ComponentOperatorRegistry.ComponentOperator {
    void browseTerm(String field, String term);
    void displayLatestDoc();
    void displayDoc(int donid);
  }

}

class PosTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {

    POSITION("Position", 0, Integer.class),
    OFFSETS("Offsets", 1, String.class),
    PAYLOAD("Payload", 2, String.class);

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

  PosTableModel() {
    this.data = new Object[0][colNames.length];
  }

  PosTableModel(List<TermPosting> postings) {
    this.data = new Object[postings.size()][colNames.length];

    for (int i = 0; i < postings.size(); i++) {
      TermPosting p = postings.get(i);

      int position = postings.get(i).getPosition();
      String offset = null;
      if (p.getStartOffset() >= 0 && p.getEndOffset() >= 0) {
        offset = String.format("%d-%d", p.getStartOffset(), p.getEndOffset());
      }
      String payload = null;
      if (p.getPayload() != null) {
        payload = BytesRefUtils.decode(p.getPayload());
      }

      data[i] = new Object[]{ position, offset, payload };
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
}

class DocumentTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    FIELD("Field", 0, String.class),
    FLAGS("Flags", 1, String.class),
    NORM("Norm", 2, Long.class),
    VALUE("Value", 3, String.class);

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

  DocumentTableModel() {
    this.data = new Object[0][colNames.length];
  }

  DocumentTableModel(List<DocumentField> doc) {
    this.data = new Object[doc.size()][colNames.length];

    for (int i = 0; i < doc.size(); i++) {
      DocumentField docField = doc.get(i);
      String field = docField.getName();
      String flags = flags(docField);
      long norm = docField.getNorm();
      String value = null;
      if (docField.getStringValue() != null) {
        value = docField.getStringValue();
      } else if (docField.getNumericValue() != null) {
        value = String.valueOf(docField.getNumericValue());
      } else if (docField.getBinaryValue() != null) {
        value = String.valueOf(docField.getBinaryValue());
      }
      data[i] = new Object[]{ field, flags, norm, value };
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

  private static String flags(org.apache.lucene.luke.models.documents.DocumentField f) {
    StringBuilder sb = new StringBuilder();
    // index options
    if (f.getIdxOptions() == null || f.getIdxOptions() == IndexOptions.NONE) {
      sb.append("-----");
    } else {
      sb.append("I");
      switch (f.getIdxOptions()) {
        case DOCS:
          sb.append("d---");
          break;
        case DOCS_AND_FREQS:
          sb.append("df--");
          break;
        case DOCS_AND_FREQS_AND_POSITIONS:
          sb.append("dfp-");
          break;
        case DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS:
          sb.append("dfpo");
          break;
        default:
          sb.append("----");
      }
    }
    // has norm?
    if (f.hasNorms()) {
      sb.append("N");
    } else {
      sb.append("-");
    }
    // has payloads?
    if (f.hasPayloads()) {
      sb.append("P");
    } else {
      sb.append("-");
    }
    // stored?
    if (f.isStored()) {
      sb.append("S");
    } else {
      sb.append("-");
    }
    // binary?
    if (f.getBinaryValue() != null) {
      sb.append("B");
    } else {
      sb.append("-");
    }
    // numeric?
    if (f.getNumericValue() == null) {
      sb.append("----");
    } else {
      sb.append("#");
      // try faking it
      Number numeric = f.getNumericValue();
      if (numeric instanceof Integer) {
        sb.append("i32");
      } else if (numeric instanceof Long) {
        sb.append("i64");
      } else if (numeric instanceof Float) {
        sb.append("f32");
      } else if (numeric instanceof Double) {
        sb.append("f64");
      } else if (numeric instanceof Short) {
        sb.append("i16");
      } else if (numeric instanceof Byte) {
        sb.append("i08");
      } else if (numeric instanceof BigDecimal) {
        sb.append("b^d");
      } else if (numeric instanceof BigInteger) {
        sb.append("b^i");
      } else {
        sb.append("???");
      }
    }
    // has term vector?
    if (f.hasTermVectors()) {
      sb.append("V");
    } else {
      sb.append("-");
    }
    // doc values
    if (f.getDvType() == null || f.getDvType() == DocValuesType.NONE) {
      sb.append("-------");
    } else {
      sb.append("D");
      switch (f.getDvType()) {
        case NUMERIC:
          sb.append("number");
          break;
        case BINARY:
          sb.append("binary");
          break;
        case SORTED:
          sb.append("sorted");
          break;
        case SORTED_NUMERIC:
          sb.append("srtnum");
          break;
        case SORTED_SET:
          sb.append("srtset");
          break;
        default:
          sb.append("??????");
      }
    }
    // point values
    if (f.getPointDimensionCount() == 0) {
      sb.append("----");
    } else {
      sb.append("T");
      sb.append(f.getPointNumBytes());
      sb.append("/");
      sb.append(f.getPointDimensionCount());
    }
    return sb.toString();
  }

}
