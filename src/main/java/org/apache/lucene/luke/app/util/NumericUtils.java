package org.apache.lucene.luke.app.util;

import com.google.common.base.Strings;

import java.util.Arrays;

public class NumericUtils {

  public static int[] convertToIntArray(String value, boolean ignoreException) throws NumberFormatException {
    if (Strings.isNullOrEmpty(value)) {
      return new int[]{0};
    }
    try {
      return Arrays.stream(value.trim().split(",")).mapToInt(Integer::parseInt).toArray();
    } catch (NumberFormatException e) {
      if (ignoreException) {
        return new int[]{0};
      } else {
        throw e;
      }
    }
  }

  public static long[] convertToLongArray(String value, boolean ignoreException) throws NumberFormatException {
    if (Strings.isNullOrEmpty(value)) {
      return new long[]{0};
    }
    try {
      return Arrays.stream(value.trim().split(",")).mapToLong(Long::parseLong).toArray();
    } catch (NumberFormatException e) {
      if (ignoreException) {
        return new long[]{0};
      } else {
        throw e;
      }
    }
  }

  public static float[] convertToFloatArray(String value, boolean ignoreException) throws NumberFormatException {
    if (Strings.isNullOrEmpty(value)) {
      return new float[]{0};
    }
    try {
      String[] strVals = value.trim().split(",");
      float[] values = new float[strVals.length];
      for (int i = 0; i < strVals.length; i++) {
        values[i] = Float.parseFloat(strVals[i]);
      }
      return values;
    } catch (NumberFormatException e) {
      if (ignoreException) {
        return new float[]{0};
      } else {
        throw e;
      }
    }
  }

  public static double[] convertToDoubleArray(String value, boolean ignoreException) throws NumberFormatException {
    if (Strings.isNullOrEmpty(value)) {
      return new double[]{0};
    }
    try {
      return Arrays.stream(value.trim().split(",")).mapToDouble(Double::parseDouble).toArray();
    } catch (NumberFormatException e) {
      if (ignoreException) {
        return new double[]{0};
      } else {
        throw e;
      }
    }
  }

  public static long tryConvertToLongValue(String value) throws NumberFormatException {
    try {
      // try parse to long
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      // try parse to double
      double dvalue = Double.parseDouble(value.trim());
      return org.apache.lucene.util.NumericUtils.doubleToSortableLong(dvalue);
    }
  }

  private NumericUtils() {
  }
}
