package org.apache.lucene.luke.app;

public class MessageHandler extends AbstractHandler<MessageObserver> {

  private MessageType type = MessageType.CLEAR_MSG;

  private String message = "";

  @Override
  protected void notifyOne(MessageObserver observer) {
    switch (type) {
      case STATUS_MSG:
        observer.showStatusMessage(message);
        break;
      case CLEAR_MSG:
        observer.clearStatusMessage();
        break;
      case UNKNOWN_ERROR:
        observer.showUnknownErrorMessage();
    }
  }

  public void showMessage(String message) {
    this.type = MessageType.STATUS_MSG;
    this.message = message;
    notifyObservers();
  }

  public void clear() {
    this.type = MessageType.CLEAR_MSG;
    this.message = "";
    notifyObservers();
  }

  public void showError() {
    this.type = MessageType.UNKNOWN_ERROR;
    this.message = "";
    notifyObservers();
  }

  private enum MessageType {
    STATUS_MSG,
    CLEAR_MSG,
    UNKNOWN_ERROR
  }

}
