package org.apache.lucene.luke.app.desktop;

import java.util.ArrayList;
import java.util.List;

public class MessageBroker {

  private List<MessageReceiver> receivers = new ArrayList<>();

  public void registerReceiver(MessageReceiver receiver) {
    receivers.add(receiver);
  }

  public void showStatusMessage(String message) {
    for (MessageReceiver receiver : receivers) {
      receiver.showStatusMessage(message);
    }
  }

  public void showUnknownErrorMessage() {
    for (MessageReceiver receiver : receivers) {
      receiver.showUnknownErrorMessage();
    }
  }

  public void clearStatusMessage() {
    for (MessageReceiver receiver : receivers) {
      receiver.clearStatusMessage();
    }
  }

  public interface MessageReceiver {
    void showStatusMessage(String message);

    void showUnknownErrorMessage();

    void clearStatusMessage();
  }

}
