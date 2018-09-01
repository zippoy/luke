package org.apache.lucene.luke.app;

public interface MessageObserver extends Observer {

  void showStatusMessage(String message);

  void showUnknownErrorMessage();

  void clearStatusMessage();

}
