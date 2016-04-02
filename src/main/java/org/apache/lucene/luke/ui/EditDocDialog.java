package org.apache.lucene.luke.ui;

import com.sun.istack.NotNull;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.luke.core.KeepAllIndexDeletionPolicy;
import org.apache.lucene.luke.core.KeepLastIndexDeletionPolicy;
import org.apache.lucene.luke.ui.form.EditDocField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;

import java.net.URL;
import java.util.Arrays;

public class EditDocDialog extends Dialog implements Bindable {

  private Resources resources;

  private int iNum;
  private Document doc;
  private LeafReader lr = null;
  private boolean keepCommits;
  private LukeWindow.LukeMediator lukeMediator;
  private int numTerms;
  private List<String> ld;
  private List<EditDocField> docFields;
  private Bits live;

  @BXML
  private Label docNum;
  @BXML
  private ListView fieldList;
  @BXML
  private Form editDocForm;
  @BXML
  private ListButton indexOptions;
  @BXML
  private BoxPane tokens;
  @BXML
  private ButtonData tokensTabButton;
  @BXML
  private ListButton analyzers;
  @BXML
  private TextInput newFieldName;
  @BXML
  private PushButton newField;
  @BXML
  private PushButton delField;
  @BXML
  private PushButton saveDoc;


  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  public void initDocumentInfo(int iNum, @NotNull LeafReader lr, LukeWindow.LukeMediator lukeMediator)
      throws Exception {
    this.iNum = iNum;
    this.lr = lr;
    this.lukeMediator = lukeMediator;
    this.keepCommits = lukeMediator.getIndexInfo().isKeepCommits();
    this.live = MultiFields.getLiveDocs(lr);
    if (live != null && !live.get(iNum)) {
      throw new Exception("Document is deleted.");
    }
    this.doc = lr.document(iNum);
    // doc num
    docNum.setText(String.valueOf(iNum));
    // populate index options list button
    List<String> idxOptions = new ListAdapter(Arrays.asList(EditDocField.indexOptionList));
    indexOptions.setListData(idxOptions);
    // populate analyzers list button
    //List<String> analyzerNames = new ListAdapter<>(Arrays.asList(lukeMediator.getAnalyzerNames()));
    //analyzers.setListData(analyzerNames);
    //analyzers.setSelectedIndex(0);

    // add button listeners
    /*
    newField.getButtonPressListeners().add(new ButtonPressListener(){
      @Override
      public void buttonPressed(Button button) {
        String fieldName = newFieldName.getText();
        if (fieldName.equals("")) {
          Alert alert = new Alert(MessageType.INFO, "Please input field name.", null, true);
          alert.open(getDisplay(), getWindow());
        } else {
          EditDocField newField = new EditDocField(fieldName);
          docFields.add(newField);
          ld.add(fieldName);
          fieldList.setSelectedIndex(ld.getLength()-1);
          editDocForm.load(new BeanAdapter(newField));
          saveDoc.setEnabled(true);
        }
      }
    });
    saveDoc.getButtonPressListeners().add(new ButtonPressListener() {
      @Override
      public void buttonPressed(Button button) {
        // TODO
        System.out.println("saved.");
      }
    });
    delField.getButtonPressListeners().add(new ButtonPressListener() {
      @Override
      public void buttonPressed(Button button) {
        // TODO
        System.out.println("deleted.");
      }
    });
    */

    // populate field info
    docFields = new ArrayList<>();
    ld = new ArrayList<>();
    for (IndexableField f : doc.getFields()) {
      Terms tv = lr.getTermVector(iNum, f.name());
      docFields.add(new EditDocField(f, tv));
      ld.add(f.name());
    }
    fieldList.setListData(ld);
    fieldList.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
      @Override
      public void selectedItemChanged(ListView listView, Object prev) {
        int selectedIndex = listView.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= docFields.getLength()) {
          return;
        }
        EditDocField selectedField = docFields.get(selectedIndex);
        editDocForm.load(new BeanAdapter(selectedField));
        if (selectedField.getHasTermVector()) {
          tokens.setEnabled(true);
        } else {
          tokens.setEnabled(false);
        }
      }
    });
    if (ld.getLength() > 0) {
      fieldList.setSelectedIndex(0);
      EditDocField selectedField = docFields.get(0);
      editDocForm.load(new BeanAdapter(selectedField));
      if (selectedField.getHasTermVector()) {
        tokens.setEnabled(true);
      } else {
        tokens.setEnabled(false);
      }
    }
  }

  public void saveDocument() {
    Document doc = new Document();

  }

  private IndexWriter createIndexWriter() {
    try {
      IndexWriterConfig cfg = new IndexWriterConfig(new WhitespaceAnalyzer());
      IndexDeletionPolicy policy;
      if (keepCommits) {
        policy = new KeepAllIndexDeletionPolicy();
      } else {
        policy = new KeepLastIndexDeletionPolicy();
      }
      cfg.setIndexDeletionPolicy(policy);
      Directory dir = lukeMediator.getIndexInfo().getDirectory();
      cfg.setUseCompoundFile(IndexGate.preferCompoundFormat(dir));
      IndexWriter iw = new IndexWriter(dir, cfg);
      return iw;
    } catch (Exception e) {
      // TODO
      e.printStackTrace();
      return null;
    }
  }

}
