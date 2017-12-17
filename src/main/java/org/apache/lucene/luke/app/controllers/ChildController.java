package org.apache.lucene.luke.app.controllers;

import org.apache.lucene.luke.models.LukeException;

public interface ChildController {

  default void onDirectoryOpen() throws LukeException {
  }

  default void setParent(LukeController parent) {
  }

  void onIndexOpen() throws LukeException;

  void onClose();

}
