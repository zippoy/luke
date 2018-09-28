package org.apache.lucene.luke.app.desktop.util;

import javax.swing.JList;
import javax.swing.ListModel;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListUtil {

  public static <T> List<T> getAllItems(JList<T> jlist) {
    ListModel<T> model = jlist.getModel();
    return getAllItems(jlist, model::getElementAt);
  }

  public static <T, R> List<R> getAllItems(JList<T> jlist, IntFunction<R> mapFunc) {
    ListModel<T> model = jlist.getModel();
    return IntStream.range(0, model.getSize()).mapToObj(mapFunc).collect(Collectors.toList());
  }

}
