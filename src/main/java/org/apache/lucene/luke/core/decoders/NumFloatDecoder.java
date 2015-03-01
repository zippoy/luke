package org.apache.lucene.luke.core.decoders;

import org.apache.lucene.document.Field;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

public class NumFloatDecoder implements Decoder {
  @Override
  public String decodeTerm(String fieldName, Object value) {
    BytesRef ref = new BytesRef(value.toString());
    return Float.toString(NumericUtils.sortableIntToFloat(NumericUtils.prefixCodedToInt(ref)));
  }

  @Override
  public String decodeStored(String fieldName, Field value) {
    return value.stringValue();
  }

  public String toString() {
    return "numeric-float";
  }

}
