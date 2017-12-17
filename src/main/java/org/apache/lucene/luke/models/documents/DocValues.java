package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.util.BytesRef;

import java.util.List;

public class DocValues {
  private DocValuesType dvType;
  private List<BytesRef> values;
  private List<Long> numericValues;

  static DocValues of(DocValuesType dvType, List<BytesRef> values, List<Long> numericValues) {
    DocValues dv = new DocValues();
    dv.dvType = dvType;
    dv.values = values;
    dv.numericValues = numericValues;
    return dv;
  }

  public DocValuesType getDvType() {
    return dvType;
  }

  public List<BytesRef> getValues() {
    return values;
  }

  public List<Long> getNumericValues() {
    return numericValues;
  }

  private DocValues() {
  }
}
