package org.apache.lucene.luke.models;

public class LukeException extends Exception {
  public LukeException(String message, Throwable t) {
    super(message, t);
  }

  public LukeException(Throwable t) {
    super(t);
  }

  public LukeException(String message) {
    super(message);
  }
}
