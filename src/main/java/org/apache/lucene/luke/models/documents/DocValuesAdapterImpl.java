package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.luke.util.IndexUtils;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DocValuesAdapterImpl implements DocValuesAdapter {

  private IndexReader reader;

  DocValuesAdapterImpl() {
  }

  @Override
  public void reset(IndexReader reader) {
    this.reader = reader;
  }

  @Override
  public Optional<DocValues> getDocValues(int docid, String field) throws IOException {
    DocValuesType dvType = IndexUtils.getFieldInfo(reader, field).getDocValuesType();
    switch (dvType) {
      case BINARY:
        BinaryDocValues bvalues = IndexUtils.getBinaryDocValues(reader, field);
        if (bvalues.advanceExact(docid)) {
          DocValues dv = DocValues.of(
              dvType,
              Collections.singletonList(BytesRef.deepCopyOf(bvalues.binaryValue())),
              Collections.emptyList());
          return Optional.of(dv);
        }
        break;
      case NUMERIC:
        NumericDocValues nvalues = IndexUtils.getNumericDocValues(reader, field);
        if (nvalues.advanceExact(docid)) {
          DocValues dv = DocValues.of(
              dvType,
              Collections.emptyList(),
              Collections.singletonList(nvalues.longValue())
          );
          return Optional.of(dv);
        }
        break;
      case SORTED_NUMERIC:
        SortedNumericDocValues snvalues = IndexUtils.getSortedNumericDocValues(reader, field);
        if (snvalues.advanceExact(docid)) {
          List<Long> numericValues = new ArrayList<>();
          int dvCount = snvalues.docValueCount();
          for (int i = 0; i < dvCount; i++) {
            numericValues.add(snvalues.nextValue());
          }
          DocValues dv = DocValues.of(
              dvType,
              Collections.emptyList(),
              numericValues
          );
          return Optional.of(dv);
        }
        break;
      case SORTED:
        SortedDocValues svalues = IndexUtils.getSortedDocValues(reader, field);
        if (svalues.advanceExact(docid)) {
          DocValues dv = DocValues.of(
              dvType,
              Collections.singletonList(BytesRef.deepCopyOf(svalues.binaryValue())),
              Collections.emptyList()
          );
          return Optional.of(dv);
        }
        break;
      case SORTED_SET:
        SortedSetDocValues ssvalues = IndexUtils.getSortedSetDocvalues(reader, field);
        if (ssvalues.advanceExact(docid)) {
          List<BytesRef> values = new ArrayList<>();
          long ord;
          while ((ord = ssvalues.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
            values.add(BytesRef.deepCopyOf(ssvalues.lookupOrd(ord)));
          }
          DocValues dv = DocValues.of(
              dvType,
              values,
              Collections.emptyList()
          );
          return Optional.of(dv);
        }
        break;
      default:
        break;
    }
    return Optional.empty();
  }
}
