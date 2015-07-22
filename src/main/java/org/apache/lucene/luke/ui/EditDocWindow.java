package org.apache.lucene.luke.ui;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Dialog;

import java.net.URL;

public class EditDocWindow extends Dialog implements Bindable {

  private Resources resources;

  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }
}
