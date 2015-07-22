package org.apache.lucene.luke.core;

import org.apache.lucene.util.FixedBitSet;

@SuppressWarnings("serial")
public class Ranges {

  //public static Ranges parse(String expr) throws Exception {
  public static FixedBitSet parse(String expr) throws Exception {
    FixedBitSet res = new FixedBitSet(64);
    expr = expr.replaceAll("\\s+", "");
    if (expr.length() == 0) {
      return res;
    }
    String[] ranges = expr.split(",");
    for (int i = 0; i < ranges.length; i++) {
      String[] ft = ranges[i].split("-");
      int from, to;
      from = Integer.parseInt(ft[0]);
      if (ft.length == 1) {
        res = FixedBitSet.ensureCapacity(res, from);
        res.set(from);
      } else {
        to = Integer.parseInt(ft[1]);
        res = FixedBitSet.ensureCapacity(res, to);
        res.set(from, to);
      }
    }
    return res;
  }

  /*
  public void set(int from, int to) {
    if (from > to) return;
    for (int i = from; i <= to; i++) {
      set(i);
    }
  }
  */
}