package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.util.BytesRef;

import javax.annotation.Nonnull;
import java.io.IOException;

public class DocumentField {
  // field name
  private String name;

  // basic index information
  private IndexOptions idxOptions;
  private boolean hasTermVectors;
  private boolean hasPayloads;
  private boolean hasNorms;
  private long norm;

  // stored value
  private boolean isStored;
  private String stringValue;
  private BytesRef binaryValue;
  private Number numericValue;

  // doc values
  private DocValuesType dvType;

  // point values
  private int pointDimensionCount;
  private int pointNumBytes;

  static DocumentField of(@Nonnull FieldInfo finfo, @Nonnull IndexReader reader, int docId)
      throws IOException {
    DocumentField dfield = new DocumentField();
    dfield.name = finfo.name;
    dfield.idxOptions = finfo.getIndexOptions();
    dfield.hasTermVectors = finfo.hasVectors();
    dfield.hasPayloads = finfo.hasPayloads();
    dfield.hasNorms = finfo.hasNorms();
    if (finfo.hasNorms()) {
      NumericDocValues norms = MultiDocValues.getNormValues(reader, finfo.name);
      if (norms.advanceExact(docId)) {
        dfield.norm = norms.longValue();
      }
    }
    dfield.dvType = finfo.getDocValuesType();
    dfield.pointDimensionCount = finfo.getPointDimensionCount();
    dfield.pointNumBytes = finfo.getPointNumBytes();
    return dfield;
  }

  static DocumentField of(@Nonnull FieldInfo finfo, @Nonnull IndexableField field, @Nonnull IndexReader reader, int docId)
      throws IOException {
    DocumentField dfield = of(finfo, reader, docId);
    dfield.isStored = field.fieldType().stored();
    dfield.stringValue = field.stringValue();
    dfield.binaryValue = field.binaryValue();
    dfield.numericValue = field.numericValue();
    return dfield;
  }

  public String getName() {
    return name;
  }

  public IndexOptions getIdxOptions() {
    return idxOptions;
  }

  public boolean hasTermVectors() {
    return hasTermVectors;
  }

  public boolean hasPayloads() {
    return hasPayloads;
  }

  public boolean hasNorms() {
    return hasNorms;
  }

  public long getNorm() {
    return norm;
  }

  public boolean isStored() {
    return isStored;
  }

  public String getStringValue() {
    return stringValue;
  }

  public BytesRef getBinaryValue() {
    return binaryValue;
  }

  public Number getNumericValue() {
    return numericValue;
  }

  public DocValuesType getDvType() {
    return dvType;
  }

  public int getPointDimensionCount() {
    return pointDimensionCount;
  }

  public int getPointNumBytes() {
    return pointNumBytes;
  }

  private DocumentField() {
  }
}
