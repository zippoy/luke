package org.apache.lucene.luke.app.desktop.components;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.MessageBroker;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.util.ArrayList;
import java.util.List;

public class TabbedPaneProvider implements Provider<JTabbedPane> {

  private final MessageBroker messageBroker;

  private final JTabbedPane tabbedPane = new JTabbedPane();

  private final JPanel overviewPanel;

  private final JPanel documentsPanel;

  private final JPanel searchPanel;

  private final JPanel analysisPanel;

  private final JPanel commitsPanel;

  private final JPanel logsPanel;

  public class TabSwitcherImpl implements TabSwitcherProxy.TabSwitcher {
    public void switchTab(Tab tab) {
      tabbedPane.setSelectedIndex(tab.index());
      tabbedPane.setVisible(false);
      tabbedPane.setVisible(true);
      messageBroker.clearStatusMessage();
    }
  }

  public class Observer implements IndexObserver, DirectoryObserver {

    @Override
    public void openDirectory(LukeState state) {
      tabbedPane.setEnabledAt(Tab.COMMITS.index(), true);
    }

    @Override
    public void closeDirectory() {
      tabbedPane.setEnabledAt(Tab.OVERVIEW.index(), false);
      tabbedPane.setEnabledAt(Tab.DOCUMENTS.index(), false);
      tabbedPane.setEnabledAt(Tab.SEARCH.index(), false);
      tabbedPane.setEnabledAt(Tab.COMMITS.index(), false);
    }

    @Override
    public void openIndex(LukeState state) {
      tabbedPane.setEnabledAt(Tab.OVERVIEW.index(), true);
      tabbedPane.setEnabledAt(Tab.DOCUMENTS.index(), true);
      tabbedPane.setEnabledAt(Tab.SEARCH.index(), true);
      tabbedPane.setEnabledAt(Tab.COMMITS.index(), true);
    }

    @Override
    public void closeIndex() {
      tabbedPane.setEnabledAt(Tab.OVERVIEW.index(), false);
      tabbedPane.setEnabledAt(Tab.DOCUMENTS.index(), false);
      tabbedPane.setEnabledAt(Tab.SEARCH.index(), false);
      tabbedPane.setEnabledAt(Tab.COMMITS.index(), false);
    }
  }

  @Inject
  public TabbedPaneProvider(@Named("overview") JPanel overviewPanel,
                            @Named("documents") JPanel documentsPanel,
                            @Named("search") JPanel searchPanel,
                            @Named("analysis") JPanel analysisPanel,
                            @Named("commits") JPanel commitsPanel,
                            @Named("logs") JPanel logsPanel,
                            IndexHandler indexHandler,
                            DirectoryHandler directoryHandler,
                            TabSwitcherProxy tabSwitcher,
                            MessageBroker messageBroker) {
    this.overviewPanel = overviewPanel;
    this.documentsPanel = documentsPanel;
    this.searchPanel = searchPanel;
    this.analysisPanel = analysisPanel;
    this.commitsPanel = commitsPanel;
    this.logsPanel = logsPanel;

    this.messageBroker = messageBroker;

    tabSwitcher.set(new TabSwitcherImpl());

    Observer observer = new Observer();
    indexHandler.addObserver(observer);
    directoryHandler.addObserver(observer);
  }

  @Override
  public JTabbedPane get() {
    tabbedPane.addTab("Overview", ImageUtils.createImageIcon("/img/icon_house_alt.png", 20, 20), overviewPanel);
    tabbedPane.addTab("Documents", ImageUtils.createImageIcon("/img/icon_documents_alt.png", 20, 20), documentsPanel);
    tabbedPane.addTab("Search", ImageUtils.createImageIcon("/img/icon_search.png", 20, 20), searchPanel);
    tabbedPane.addTab("Analysis", ImageUtils.createImageIcon("/img/icon_pencil-edit_alt.png", 20, 20), analysisPanel);
    tabbedPane.addTab("Commits", ImageUtils.createImageIcon("/img/icon_drive.png", 20, 20), commitsPanel);
    tabbedPane.addTab("Logs", ImageUtils.createImageIcon("/img/icon_document.png", 20, 20), logsPanel);

    return tabbedPane;
  }

  public enum Tab {
    OVERVIEW(0), DOCUMENTS(1), SEARCH(2), ANALYZER(3), COMMITS(4);

    private int tabIdx;

    Tab(int tabIdx) {
      this.tabIdx = tabIdx;
    }

    int index() {
      return tabIdx;
    }
  }

  public static class TabSwitcherProxy {

    private final List<TabSwitcher> switcherHolder = new ArrayList<>();

    private void set(TabSwitcher switcher) {
      if (switcherHolder.isEmpty()) {
        switcherHolder.add(switcher);
      }
    }

    public void switchTab(TabbedPaneProvider.Tab tab) {
      if (switcherHolder.get(0) == null) {
        throw new IllegalStateException();
      }
      switcherHolder.get(0).switchTab(tab);
    }

    public interface TabSwitcher {
      void switchTab(TabbedPaneProvider.Tab tab);
    }

  }


}
