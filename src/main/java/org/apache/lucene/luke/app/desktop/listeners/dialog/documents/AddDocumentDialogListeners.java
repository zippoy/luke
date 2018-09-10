package org.apache.lucene.luke.app.desktop.listeners.dialog.documents;

import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.AddDocumentDialogFactory;
import org.apache.lucene.luke.app.desktop.dto.documents.NewField;
import org.apache.lucene.luke.app.desktop.util.NumericUtils;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.util.BytesRef;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

public class AddDocumentDialogListeners {

  private final AddDocumentDialogFactory.Controller controller;


  public AddDocumentDialogListeners(AddDocumentDialogFactory.Controller controller) {
    this.controller = controller;
  }

  public ActionListener getAddBtnListener() {
    return (ActionEvent e) -> {
      List<NewField> validFields = controller.getNewFieldList().stream()
          .filter(nf -> !nf.isDeleted())
          .filter(nf -> !Strings.isNullOrEmpty(nf.getName()))
          .filter(nf -> !Strings.isNullOrEmpty(nf.getValue()))
          .collect(Collectors.toList());
      if (validFields.isEmpty()) {
        controller.setInfo("Please add one or more fields. Name and Value are both required.");
        return;
      }

      Document doc = new Document();
      try {
        for (NewField nf : validFields) {
          doc.add(toIndexableField(nf));
          System.out.println(doc);
        }
      } catch (NumberFormatException ex) {
        //logger.error(ex.getMessage(), e);
        throw new LukeException("Invalid value: " + ex.getMessage(), ex);
      } catch (Exception ex) {
        //logger.error(ex.getMessage(), e);
        throw new LukeException(ex.getMessage(), ex);
      }

      controller.addDocument(doc);
    };
  }

  @SuppressWarnings("unchecked")
  private IndexableField toIndexableField(NewField nf) throws Exception {
    if (nf.getType().equals(TextField.class) || nf.getType().equals(StringField.class)) {
      Field.Store store = nf.isStored() ? Field.Store.YES : Field.Store.NO;
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, String.class, Field.Store.class);
      return constr.newInstance(nf.getName(), nf.getValue(), store);
    } else if (nf.getType().equals(IntPoint.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, int[].class);
      int[] values = NumericUtils.convertToIntArray(nf.getValue(), false);
      return constr.newInstance(nf.getName(), values);
    } else if (nf.getType().equals(LongPoint.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, long[].class);
      long[] values = NumericUtils.convertToLongArray(nf.getValue(), false);
      return constr.newInstance(nf.getName(), values);
    } else if (nf.getType().equals(FloatPoint.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, float[].class);
      float[] values = NumericUtils.convertToFloatArray(nf.getValue(), false);
      return constr.newInstance(nf.getName(), values);
    } else if (nf.getType().equals(DoublePoint.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, double[].class);
      double[] values = NumericUtils.convertToDoubleArray(nf.getValue(), false);
      return constr.newInstance(nf.getName(), values);
    } else if (nf.getType().equals(SortedDocValuesField.class) ||
        nf.getType().equals(SortedSetDocValuesField.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, BytesRef.class);
      return constr.newInstance(nf.getName(), new BytesRef(nf.getValue()));
    } else if (nf.getType().equals(NumericDocValuesField.class) ||
        nf.getType().equals(SortedNumericDocValuesField.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, long.class);
      long value = NumericUtils.tryConvertToLongValue(nf.getValue());
      return constr.newInstance(nf.getName(), value);
    } else if (nf.getType().equals(StoredField.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, String.class);
      return constr.newInstance(nf.getName(), nf.getValue());
    } else if (nf.getType().equals(Field.class)) {
      Constructor<IndexableField> constr = nf.getType().getConstructor(String.class, String.class, IndexableFieldType.class);
      return constr.newInstance(nf.getName(), nf.getValue(), nf.getFieldType());
    } else {
      // TODO: unknown field
      return new StringField(nf.getName(), nf.getValue(), Field.Store.YES);
    }
  }


}
