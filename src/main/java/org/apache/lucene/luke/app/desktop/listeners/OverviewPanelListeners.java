package org.apache.lucene.luke.app.desktop.listeners;

import org.apache.lucene.luke.app.desktop.components.DocumentsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.OverviewPanelProvider;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.models.overview.Overview;
import org.apache.lucene.luke.models.overview.TermStats;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class OverviewPanelListeners {

  private final OverviewPanelProvider.Controller controller;

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  private final DocumentsPanelProvider.DocumentsTabProxy documentsTabProxy;

  private Overview overviewModel;

  public OverviewPanelListeners(
      OverviewPanelProvider.Controller controller,
      DocumentsPanelProvider.DocumentsTabProxy documentsTabProxy,
      TabbedPaneProvider.TabSwitcherProxy tabSwitcher) {
    this.controller = controller;
    this.documentsTabProxy = documentsTabProxy;
    this.tabSwitcher = tabSwitcher;
  }

  public void setOverviewModel(Overview overviewModel) {
    this.overviewModel = overviewModel;
  }

  public MouseListener getTermCountsTableListener() {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        controller.getCurrentTermCountsField().ifPresent(field -> {
          controller.setSelectedField(field);
          controller.enableShowTopTermBtn();
        });
      }
    };
  }

  public ActionListener getShowTopTermsBtnListener() {
    return (ActionEvent e) -> {
      String field = controller.getSelectedField();
      Integer numTerms = controller.getNumTopTerms();
      List<TermStats> termStats = overviewModel.getTopTerms(field, numTerms);
      controller.updateTopTerms(termStats, numTerms);
    };
  }

  public MouseListener getTopTermsTableListener() {
    return new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        showPopupIfNeeded(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        showPopupIfNeeded(e);
      }

      private void showPopupIfNeeded(MouseEvent e) {
        if (e.isPopupTrigger()) {
          createTopTermsPopup().show(e.getComponent(), e.getX(), e.getY());
        }
      }
    };
  }

  private JPopupMenu createTopTermsPopup() {
    JPopupMenu popup = new JPopupMenu();

    JMenuItem item1 = new JMenuItem(MessageUtils.getLocalizedMessage("overview.toptermtable.menu.item1"));
    item1.addActionListener(getTopTermsPopupBrowseListener());
    popup.add(item1);

    JMenuItem item2 = new JMenuItem(MessageUtils.getLocalizedMessage("overview.toptermtable.menu.item2"));
    item2.addActionListener(getTopTermsPopupSearchListener());
    popup.add(item2);

    return popup;
  }

  private ActionListener getTopTermsPopupBrowseListener() {
    return (ActionEvent e) -> {
      String field = controller.getSelectedField();
      String term = controller.getSelectedTerm();
      documentsTabProxy.browseTerm(field, term);
      tabSwitcher.switchTab(TabbedPaneProvider.Tab.DOCUMENTS);
    };
  }

  private ActionListener getTopTermsPopupSearchListener() {
    return (ActionEvent e) -> {
      String field = controller.getSelectedField();
      String term = controller.getSelectedTerm();
      // TODO
      System.out.println(String.format("Search term %s in %s", term, field));
      tabSwitcher.switchTab(TabbedPaneProvider.Tab.SEARCH);
    };
  }
}
