package org.apache.lucene.luke.app.controllers;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.app.controllers.dialog.AddDocumentController;
import org.apache.lucene.luke.app.controllers.dialog.DocValuesController;
import org.apache.lucene.luke.app.controllers.dialog.InfoController;
import org.apache.lucene.luke.app.controllers.dialog.StoredValueController;
import org.apache.lucene.luke.app.controllers.dialog.TermVectorController;
import org.apache.lucene.luke.app.controllers.dto.DocumentField;
import org.apache.lucene.luke.app.controllers.dto.TermPosting;
import org.apache.lucene.luke.app.util.DialogOpener;
import org.apache.lucene.luke.app.util.IntegerTextFormatter;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.documents.DocValues;
import org.apache.lucene.luke.models.documents.Documents;
import org.apache.lucene.luke.models.documents.TermVectorEntry;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.util.MessageUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class DocumentsController implements ChildController {

  @FXML
  private Spinner<Integer> docNum;

  @FXML
  private Hyperlink mltSearch;

  @FXML
  private Button addDoc;

  @FXML
  private Label maxDocs;

  @FXML
  private ChoiceBox<String> field;

  @FXML
  private TextField term;

  @FXML
  private Button firstTerm;

  @FXML
  private Button nextTerm;

  @FXML
  private Label showedDocNum;

  @FXML
  private TextField showedTerm;

  @FXML
  private Label termDocsNum;

  @FXML
  private TextField termDocIdx;

  @FXML
  private Button firstTermDoc;

  @FXML
  private Button nextTermDoc;

  @FXML
  private TableView<TermPosting> posTable;

  @FXML
  private TableColumn<TermPosting, Integer> posColumn;

  @FXML
  private TableColumn<TermPosting, String> offsetColumn;

  @FXML
  private TableColumn<TermPosting, String> payloadColumn;

  private ObservableList<TermPosting> posList;

  @FXML
  private TableView<DocumentField> documentTable;

  @FXML
  private TableColumn<DocumentField, String> fieldColumn;

  @FXML
  private TableColumn<DocumentField, String> flagColumn;

  @FXML
  private TableColumn<DocumentField, Long> normColumn;

  @FXML
  private TableColumn<DocumentField, String> valueColumn;

  private ObservableList<DocumentField> documentFieldList;

  @FXML
  private void initialize() {
    // initialize postings table view
    posColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
    offsetColumn.setCellValueFactory(new PropertyValueFactory<>("offset"));
    payloadColumn.setCellValueFactory(new PropertyValueFactory<>("payload"));
    posList = FXCollections.observableArrayList();
    posTable.setItems(posList);

    // initialize documents table view
    fieldColumn.setCellValueFactory(new PropertyValueFactory<>("field"));
    flagColumn.setCellValueFactory(new PropertyValueFactory<>("flag"));
    normColumn.setCellValueFactory(new PropertyValueFactory<>("norm"));
    valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    documentFieldList = FXCollections.observableArrayList();
    documentTable.setItems(documentFieldList);
    documentTable.setContextMenu(createDocTableMenu());

    ImageView imageView = new ImageView(new Image("/img/icon_question_alt2.png"));
    imageView.setFitWidth(12);
    imageView.setFitHeight(12);
    Hyperlink helpLink = new Hyperlink(MessageUtils.getLocalizedMessage("label.help"), imageView);
    helpLink.setOnMouseClicked(e -> runnableWrapper(this::showFlagsHelpDialog));
    Label flagLabel = new Label("Flags");
    flagLabel.setPadding(new Insets(0, 30, 0, 0));
    FlowPane flowPane = new FlowPane();
    flowPane.setOrientation(Orientation.HORIZONTAL);
    flowPane.setMaxWidth(Double.MAX_VALUE);
    flowPane.setAlignment(Pos.CENTER);
    flowPane.getChildren().addAll(flagLabel, helpLink);
    flagColumn.setGraphic(flowPane);

    firstTerm.setOnAction(e -> runnableWrapper(this::showFirstTerm));
    nextTerm.setOnAction(e -> runnableWrapper(this::showNextTerm));
    term.setOnKeyPressed(e -> runnableWrapper(() -> {
      if (e.getCode() == KeyCode.ENTER) {
        seekTermCeil();
      }
    }));

    firstTermDoc.setOnAction(e -> runnableWrapper(this::showFirstTermDoc));
    nextTermDoc.setOnAction(e -> runnableWrapper(this::showNextTermDoc));

    nextTerm.setDisable(true);
    term.setEditable(false);
    firstTermDoc.setDisable(true);
    nextTermDoc.setDisable(true);

    mltSearch.setOnAction(e -> runnableWrapper(() -> {
      parent.getSearchController().mltSearch(docNum.getValue());
      parent.switchTab(LukeController.Tab.SEARCH);
    }));

    addDoc.setOnAction(e -> runnableWrapper(this::showAddDocumentDialog));
  }

  @Override
  public void onIndexOpen() throws LukeException {
    addDoc.setDisable(parent.isReadOnly());

    int maxDoc = documentsModel.getMaxDoc();
    maxDocs.setText(String.format("in %d docs", maxDoc));
    if (maxDoc > 0) {
      int max = Math.max(maxDoc - 1, 0);
      SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
          new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0, 1);
      docNum.setValueFactory(valueFactory);
      TextFormatter<Integer> textFormatter = new IntegerTextFormatter(valueFactory.getConverter(), 0);
      docNum.getEditor().setTextFormatter(textFormatter);
      docNum.valueProperty().addListener(e -> runnableWrapper(() ->
          showDoc(docNum.getValueFactory().getValue())
      ));
      docNum.focusedProperty().addListener((obs, oldV, newV) -> runnableWrapper(() -> {
        if (newV) {
          // won't not change value, but commit editor
          // https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
          docNum.increment(0);
          showDoc(docNum.getValueFactory().getValue());
        }
      }));
    } else {
      docNum.setDisable(true);
    }

    List<String> fields = documentsModel.getFieldNames().stream().sorted().collect(Collectors.toList());
    field.getItems().addAll(fields);
    if (fields.size() > 0) {
      field.setValue(fields.get(0));
    }
    field.setOnAction(e -> runnableWrapper(this::showFirstTerm));

    if (documentsModel.getMaxDoc() > 0) {
      showDoc(0);
    }
  }

  @Override
  public void onClose() {
    maxDocs.setText("in ? docs");
    docNum.getEditor().setText("");
    field.getItems().clear();
    term.setText("");
    showedDocNum.setText("");
    showedTerm.setText("");
    termDocsNum.setText("");
    termDocIdx.setText("");
    posList.clear();
    documentFieldList.clear();
  }

  @Override
  public void setParent(LukeController parent) {
    this.parent = parent;
  }

  private void showDoc(int docid) throws LukeException {
    showedDocNum.setText(String.valueOf(docid));
    documentFieldList.clear();

    List<DocumentField> doc = documentsModel.getDocumentFields(docid).map(docFields ->
        docFields.stream()
            .map(DocumentField::of)
            .collect(Collectors.toList())).orElse(Collections.emptyList());
    documentFieldList.addAll(doc);
    parent.clearStatusMessage();
  }

  private void showFirstTerm() throws LukeException {
    String fieldName = field.getValue();
    if (fieldName == null || fieldName.length() == 0) {
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.field.message.not_selected"));
      return;
    }

    termDocIdx.setText("");
    posList.clear();

    String firstTermText = documentsModel.firstTerm(fieldName).map(Term::text).orElse("");
    term.setText(firstTermText);
    showedTerm.setText(firstTermText);
    if (showedTerm.getText().length() > 0) {
      String num = documentsModel.getDocFreq().map(String::valueOf).orElse("?");
      termDocsNum.setText(String.format("in %s docs", num));

      nextTerm.setDisable(false);
      term.setEditable(true);
      firstTermDoc.setDisable(false);
    } else {
      nextTerm.setDisable(true);
      term.setEditable(false);
      firstTermDoc.setDisable(true);
    }
    nextTermDoc.setDisable(true);

    parent.clearStatusMessage();
  }

  private void showNextTerm() throws LukeException {
    termDocIdx.setText("");
    posList.clear();

    String nextTermText = documentsModel.nextTerm().map(Term::text).orElse("");
    term.setText(nextTermText);
    showedTerm.setText(nextTermText);
    if (showedTerm.getText().length() > 0) {
      String num = documentsModel.getDocFreq().map(String::valueOf).orElse("?");
      termDocsNum.setText(String.format("in %s docs", num));

      term.setEditable(true);
      firstTermDoc.setDisable(false);
    } else {
      nextTerm.setDisable(true);
      term.setEditable(false);
      firstTermDoc.setDisable(true);
    }
    nextTermDoc.setDisable(true);

    parent.clearStatusMessage();
  }

  private void seekTermCeil() throws LukeException {
    termDocIdx.setText("");
    posList.clear();
    String termText = term.getText();

    String nextTermText = documentsModel.seekTerm(termText).map(Term::text).orElse("");
    term.setText(nextTermText);
    showedTerm.setText(nextTermText);
    if (showedTerm.getText().length() > 0) {
      String num = documentsModel.getDocFreq().map(String::valueOf).orElse("?");
      termDocsNum.setText(String.format("in %s docs", num));

      term.setEditable(true);
      firstTermDoc.setDisable(false);
    } else {
      nextTerm.setDisable(true);
      term.setEditable(false);
      firstTermDoc.setDisable(true);
    }
    nextTermDoc.setDisable(true);

    parent.clearStatusMessage();
  }

  private void showFirstTermDoc() throws LukeException {
    int doc = documentsModel.firstTermDoc().orElse(-1);
    if (doc < 0) {
      nextTermDoc.setDisable(true);
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.termdocs.message.not_available"));
      return;
    }
    termDocIdx.setText(String.valueOf(1));
    docNum.getEditor().setText(String.valueOf(doc));
    showDoc(doc);
    List<org.apache.lucene.luke.models.documents.TermPosting> postings = documentsModel.getTermPositions();
    posList.clear();
    posList.addAll(
        postings.stream()
            .filter(p -> p.getPosition() >= 0)
            .map(TermPosting::of)
            .collect(Collectors.toList()));

    nextTermDoc.setDisable(false);
    parent.clearStatusMessage();
  }

  private void showNextTermDoc() throws LukeException {
    int doc = documentsModel.nextTermDoc().orElse(-1);
    if (doc < 0) {
      nextTermDoc.setDisable(true);
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.termdocs.message.not_available"));
      return;
    }
    int curIdx = Integer.parseInt(termDocIdx.getCharacters().toString());
    termDocIdx.setText(String.valueOf(curIdx + 1));
    docNum.getEditor().setText(String.valueOf(doc));
    showDoc(doc);
    List<org.apache.lucene.luke.models.documents.TermPosting> postings = documentsModel.getTermPositions();
    posList.clear();
    posList.addAll(
        postings.stream()
            .filter(p -> p.getPosition() >= 0)
            .map(TermPosting::of)
            .collect(Collectors.toList()));

    nextTermDoc.setDisable(false);
    parent.clearStatusMessage();
  }

  private Stage addDocumentDialog;

  private void showAddDocumentDialog() throws Exception {
    addDocumentDialog = new DialogOpener<AddDocumentController>(parent).show(
        addDocumentDialog,
        "Add Document",
        "/fxml/dialog/add_document.fxml",
        600, 500,
        (controller) -> {
          controller.setParent(parent, this);
          controller.setAnalyzer(curAnalyzer);
          controller.setPresetFields(indexToolsModel.getPresetFields());
        }
    );
  }

  private Stage flagsHelpDialog;

  private void showFlagsHelpDialog() throws Exception {
    String content = getClass().getResource("/html/flagsHelp.html").toExternalForm();
    flagsHelpDialog = new DialogOpener<InfoController>(parent).show(
        flagsHelpDialog,
        "About Flags",
        "/fxml/dialog/info.fxml",
        600, 350,
        (controller) -> controller.setContent(content));
  }

  private ContextMenu createDocTableMenu() {
    ContextMenu menu = new ContextMenu();
    // show term vector
    MenuItem item1 = new MenuItem(MessageUtils.getLocalizedMessage("documents.doctable.menu.item1"));
    item1.setOnAction(event -> runnableWrapper(() -> {
      String selectedField = documentTable.getSelectionModel().getSelectedItem().getField();
      showTermVectorDialog(selectedField);
    }));

    // show doc values
    MenuItem item2 = new MenuItem(MessageUtils.getLocalizedMessage("documents.doctable.menu.item2"));
    item2.setOnAction(event -> runnableWrapper(() -> {
      DocumentField selected = documentTable.getSelectionModel().getSelectedItem();
      showDocValuesDialog(selected.getField());
    }));

    // show stored value
    MenuItem item3 = new MenuItem(MessageUtils.getLocalizedMessage("documents.doctable.menu.item3"));
    item3.setOnAction(event -> runnableWrapper(() -> {
      DocumentField selected = documentTable.getSelectionModel().getSelectedItem();
      showStoredValueDialog(selected.getField(), selected.getValue());
    }));

    // copy stored value to clipboard
    MenuItem item4 = new MenuItem(MessageUtils.getLocalizedMessage("documents.doctable.menu.item4"));
    item4.setOnAction(event -> {
      DocumentField selected = documentTable.getSelectionModel().getSelectedItem();
      copyStoredValue(selected.getField(), selected.getValue());
    });

    menu.getItems().addAll(item1, item2, item3, item4);
    return menu;
  }

  private Stage termVectorDialog = null;

  private void showTermVectorDialog(@Nonnull String field) throws Exception {
    int docid = Integer.parseInt(showedDocNum.getText());
    List<TermVectorEntry> tvEntries = documentsModel.getTermVectors(docid, field);
    if (tvEntries.isEmpty()) {
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.termvector.message.not_available", field, docid));
      return;
    }

    termVectorDialog = new DialogOpener<TermVectorController>(parent).show(
        termVectorDialog,
        "Term Vector",
        "/fxml/dialog/termvector.fxml",
        400, 300,
        (controller) -> controller.setTermVector(field, tvEntries));

    parent.clearStatusMessage();
  }

  private Stage docValuesDialog = null;

  private void showDocValuesDialog(@Nonnull String field) throws Exception {
    int docid = Integer.parseInt(showedDocNum.getText());
    Optional<DocValues> docValues = documentsModel.getDocValues(docid, field);
    if (docValues.isPresent()) {
      docValuesDialog = new DialogOpener<DocValuesController>(parent).show(
          docValuesDialog,
          "Doc Values",
          "/fxml/dialog/docvalues.fxml",
          400, 300,
          (controller) -> controller.setValue(field, docValues.get()));
      parent.clearStatusMessage();
    } else {
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.docvalues.message.not_available", field, docid));
    }
  }

  private Stage storedValueDialog = null;

  private void showStoredValueDialog(@Nonnull String field, @Nullable String stored) throws Exception {
    if (stored == null || stored.length() == 0) {
      int docid = Integer.parseInt(showedDocNum.getText());
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.stored.message.not_availabe", field, docid));
      return;
    }
    storedValueDialog = new DialogOpener<StoredValueController>(parent).show(
        storedValueDialog,
        "Stored Value",
        "/fxml/dialog/stored.fxml",
        400, 300,
        (controller) -> controller.setValue(field, stored));

    parent.clearStatusMessage();
  }

  private void copyStoredValue(@Nonnull String field, @Nullable String stored) {
    if (stored == null || stored.length() == 0) {
      int docid = Integer.parseInt(showedDocNum.getText());
      parent.showStatusMessage(MessageUtils.getLocalizedMessage("documents.stored.message.not_availabe", field, docid));
      return;
    }

    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(stored);
    clipboard.setContent(content);

    parent.clearStatusMessage();
  }

  // -------------------------------------------------
  // methods for interaction with other controllers
  // -------------------------------------------------

  void browseDocsByTerm(@Nonnull String fieldName, @Nonnull String termText) throws LukeException {
    field.setValue(fieldName);
    term.setText(termText);
    seekTermCeil();
    showFirstTermDoc();
  }

  void displayDoc(int docId) {
    docNum.getEditor().setText(String.valueOf(docId));
  }

  public void displayLatestDoc() throws LukeException {
    int docId = documentsModel.getMaxDoc() - 1;
    docNum.getEditor().setText(String.valueOf(docId));
    showDoc(docId);
  }

  private Documents documentsModel;

  private IndexTools indexToolsModel;

  private LukeController parent;

  private Analyzer curAnalyzer;

  @Inject
  public DocumentsController(Documents documentsModel, IndexTools indexToolsModel) {
    this.documentsModel = documentsModel;
    this.indexToolsModel = indexToolsModel;
  }

  public void setCurrentAnalyzer(Analyzer analyzer) {
    this.curAnalyzer = analyzer;
  }

  public void addDocument(Document doc) throws LukeException {
    indexToolsModel.addDocument(doc, curAnalyzer);
  }
}
