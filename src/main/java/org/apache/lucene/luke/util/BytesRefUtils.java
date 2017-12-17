package org.apache.lucene.luke.util;

import org.apache.lucene.util.BytesRef;

public class BytesRefUtils {

  public static String decode(BytesRef ref) {
    try {
      return ref.utf8ToString();
    } catch (Exception e) {
      return ref.toString();
    }
  }

  private BytesRefUtils() {
  }
}
