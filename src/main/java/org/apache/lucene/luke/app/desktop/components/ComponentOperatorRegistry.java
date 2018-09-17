package org.apache.lucene.luke.app.desktop.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentOperatorRegistry {

  private final Map<Class<?>, Object> operators = new HashMap<>();

  public <T extends ComponentOperator> void register(Class<T> type, T operator) {
    if (!operators.containsKey(type)) {
      operators.put(type, operator);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends ComponentOperator> Optional<T> get(Class<T> type) {
    return Optional.ofNullable((T) operators.get(type));
  }

  public interface ComponentOperator {}

}
