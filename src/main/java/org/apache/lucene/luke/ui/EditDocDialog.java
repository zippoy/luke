package org.apache.lucene.luke.ui;

import com.sun.istack.NotNull;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.luke.ui.form.EditDocField;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

public class EditDocDialog extends Dialog implements Bindable {

  private Resources resources;

  private int iNum;
  private Document doc;
  private LeafReader lr = null;
  private int numTerms;
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


  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  public void initDocumentInfo(int iNum, @NotNull LeafReader lr)
      throws Exception {
    this.iNum = iNum;
    this.lr = lr;
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

    this.docFields = new ArrayList<>();
    List<String> ld = new ArrayList<>();
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
          //System.out.println("Invalid field index?");
          return;
        }
        EditDocField selectedField = docFields.get(selectedIndex);
        editDocForm.load(new BeanAdapter(selectedField));
        if (selectedField.getHasTermVector()) {
          tokens.setEnabled(true);
          tokensTabButton.setText(String.format("Tokens for all '%s' field", selectedField.getName()));
        }
      }
    });
    if (ld.getLength() > 0) {
      fieldList.setSelectedIndex(0);
      EditDocField selectedField = docFields.get(0);
      editDocForm.load(new BeanAdapter(selectedField));
      if (selectedField.getHasTermVector()) {
        tokens.setEnabled(true);
        tokensTabButton.setText(String.format("Tokens for all '%s' field", selectedField.getName()));
      }
    }
  }
}
