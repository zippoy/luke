package org.apache.lucene.luke.app.controllers;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.lucene.luke.app.controllers.dto.File;
import org.apache.lucene.luke.app.controllers.dto.Segment;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.commits.Commit;
import org.apache.lucene.luke.models.commits.Commits;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.lucene.luke.app.util.ExceptionHandler.runnableWrapper;

public class CommitsController implements ChildController {

  @FXML
  private ChoiceBox<Long> generation;

  private ObservableList<Long> generationList;

  @FXML
  private Label deleted;

  @FXML
  private Label segCount;

  @FXML
  private Label userData;

  @FXML
  private TableView<File> filesTable;

  @FXML
  private TableColumn<File, String> fileNameColumn;

  @FXML
  private TableColumn<File, String> fileSizeColumn;

  private ObservableList<File> fileList;

  @FXML
  private TableView<Segment> segmentsTable;

  @FXML
  private TableColumn<Segment, String> segNameColumn;

  @FXML
  private TableColumn<Segment, Integer> maxDocColumn;

  @FXML
  private TableColumn<Segment, Integer> delsColumn;

  @FXML
  private TableColumn<Segment, Long> delGenColumn;

  @FXML
  private TableColumn<Segment, String> versionColumn;

  @FXML
  private TableColumn<Segment, String> codecColumn;

  @FXML
  private TableColumn<Segment, String> segSizeColumn;

  private ObservableList<Segment> segmentList;

  @FXML
  private ToggleGroup detailsGroup;

  @FXML
  private RadioButton diagRadio;

  @FXML
  private RadioButton attRadio;

  @FXML
  private RadioButton codecRadio;

  @FXML
  private ListView<String> segDetails;

  @FXML
  private ObservableList<String> segDetailList;

  @FXML
  private void initialize() {
    generationList = FXCollections.observableArrayList();
    generation.setItems(generationList);
    generation.setOnAction(e -> runnableWrapper(this::selectCommit));

    fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
    fileList = FXCollections.observableArrayList();
    filesTable.setItems(fileList);

    segNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    maxDocColumn.setCellValueFactory(new PropertyValueFactory<>("maxDoc"));
    delsColumn.setCellValueFactory(new PropertyValueFactory<>("delCount"));
    delGenColumn.setCellValueFactory(new PropertyValueFactory<>("delGen"));
    versionColumn.setCellValueFactory(new PropertyValueFactory<>("luceneVer"));
    codecColumn.setCellValueFactory(new PropertyValueFactory<>("codecName"));
    segSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
    segmentList = FXCollections.observableArrayList();
    segmentsTable.setItems(segmentList);
    segmentsTable.setOnMouseClicked(e -> {
      setDisableRadios(false);
      runnableWrapper(this::showSegmentDetails);
    });

    diagRadio.setSelected(true);
    diagRadio.setOnAction(e -> runnableWrapper(this::showSegmentDetails));
    attRadio.setDisable(true);
    attRadio.setOnAction(e -> runnableWrapper(this::showSegmentDetails));
    codecRadio.setDisable(true);
    codecRadio.setOnAction(e -> runnableWrapper(this::showSegmentDetails));
    setDisableRadios(true);

    segDetailList = FXCollections.observableArrayList();
    segDetails.setItems(segDetailList);
  }

  @Override
  public void onDirectoryOpen() throws LukeException {
    populateCommitGenerations();
  }

  @Override
  public void onIndexOpen() throws LukeException {
    populateCommitGenerations();
  }

  @Override
  public void onClose() {
    deleted.setText("");
    segCount.setText("");
    userData.setText("");
    generation.setValue(null);
    generationList.clear();
    fileList.clear();
    segmentList.clear();
  }

  private void populateCommitGenerations() throws LukeException {
    generationList.clear();
    commitsModel.listCommits().ifPresent(commits -> {
      for (Commit commit : commits) {
        generationList.add(commit.getGeneration());
      }
    });
    if (generationList.size() > 0) {
      generation.setValue(generationList.get(0));
    }
  }

  private void selectCommit() throws LukeException {
    if (generation.getValue() == null) {
      return;
    }

    deleted.setText("");
    segCount.setText("");
    userData.setText("");
    fileList.clear();
    segmentList.clear();
    segDetailList.clear();

    setDisableRadios(true);

    long commitGen = generation.getValue();
    commitsModel.getCommit(commitGen).ifPresent(commit -> {
      deleted.setText(String.valueOf(commit.isDeleted()));
      segCount.setText(String.valueOf(commit.getSegCount()));
      userData.setText(commit.getUserData());
    });

    commitsModel.getFiles(commitGen).ifPresent(files -> {
      fileList.addAll(files.stream()
          .map(File::of)
          .collect(Collectors.toList()));
    });

    commitsModel.getSegments(commitGen).ifPresent(segs -> {
      segmentList.addAll(segs.stream()
          .map(Segment::of)
          .collect(Collectors.toList()));
    });
  }

  @SuppressWarnings("unchecked")
  private void showSegmentDetails() throws LukeException {
    if (generation.getValue() == null ||
        segmentsTable.getSelectionModel().getSelectedItem() == null) {
      return;
    }
    segDetailList.clear();

    long commitGen = generation.getValue();
    String segName = segmentsTable.getSelectionModel().getSelectedItem().getName();
    String selected = detailsGroup.getSelectedToggle().getUserData().toString();

    if (selected.equals("diag")) {
      commitsModel.getSegmentDiagnostics(commitGen, segName).ifPresent(map ->
          segDetailList.addAll(
              map.entrySet().stream()
                  .map(e -> e.getKey() + " = " + e.getValue())
                  .collect(Collectors.toList()))
      );
    } else if (selected.equals("att")) {
      commitsModel.getSegmentAttributes(commitGen, segName).ifPresent(map ->
          segDetailList.addAll(
              map.entrySet().stream()
                  .map(e -> e.getKey() + " = " + e.getValue())
                  .collect(Collectors.toList()))
      );
    } else if (selected.equals("codec")) {
      commitsModel.getSegmentCodec(commitGen, segName).ifPresent(codec -> {
        Map<String, String> map = new HashMap<>();
        map.put("Codec name", codec.getName());
        map.put("Codec class name", codec.getClass().getName());
        map.put("Compound format", codec.compoundFormat().getClass().getName());
        map.put("DocValues format", codec.docValuesFormat().getClass().getName());
        map.put("FieldInfos format", codec.fieldInfosFormat().getClass().getName());
        map.put("LiveDocs format", codec.liveDocsFormat().getClass().getName());
        map.put("Norms format", codec.normsFormat().getClass().getName());
        map.put("Points format", codec.pointsFormat().getClass().getName());
        map.put("Postings format", codec.postingsFormat().getClass().getName());
        map.put("SegmentInfo format", codec.segmentInfoFormat().getClass().getName());
        map.put("StoredFields format", codec.storedFieldsFormat().getClass().getName());
        map.put("TermVectors format", codec.termVectorsFormat().getClass().getName());
        segDetailList.addAll(
            map.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.toList())
        );
      });
    }

  }

  private void setDisableRadios(boolean value) {
    diagRadio.setDisable(value);
    attRadio.setDisable(value);
    codecRadio.setDisable(value);
  }

  @Override
  public void setParent(LukeController parent) {
    this.parent = parent;
  }

  private LukeController parent;

  private Commits commitsModel;

  @Inject
  public CommitsController(Commits commitsModel) {
    this.commitsModel = commitsModel;
  }

}
