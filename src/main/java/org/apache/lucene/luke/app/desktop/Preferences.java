package org.apache.lucene.luke.app.desktop;

import org.apache.lucene.luke.app.controllers.LukeController;

import java.io.IOException;
import java.util.List;

public interface Preferences {

  List<String> getHistory();

  void addHistory(String indexPath) throws IOException;

  LukeController.ColorTheme getTheme();

  void setTheme(LukeController.ColorTheme theme) throws IOException;

  boolean isReadOnly();

  String getDirImpl();

  boolean isNoReader();

  boolean isUseCompound();

  boolean isKeepAllCommits();

  void setIndexOpenerPrefs(boolean readOnly, String dirImpl, boolean noReader, boolean useCompound, boolean keepAllCommits) throws IOException;
}
