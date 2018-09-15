package org.apache.lucene.luke.app.desktop.components;

import java.util.ArrayList;
import java.util.List;

public abstract class ComponentProxy<T extends ComponentProxy.Component> {

  private final List<T> holder = new ArrayList<>();

  public void set(T component) {
    if (holder.isEmpty()) {
      holder.add(component);
    }
  }

  public T get() {
    if (holder.isEmpty()) {
      throw new IllegalStateException("Component is not set.");
    }
    return holder.get(0);
  }

  public interface Component {
  }
}
