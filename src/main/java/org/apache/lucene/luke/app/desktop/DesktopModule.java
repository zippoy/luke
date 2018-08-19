package org.apache.lucene.luke.app.desktop;

import com.apple.laf.AquaButtonBorder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.lucene.luke.app.LukeModule;
import org.apache.lucene.luke.app.desktop.components.AnalysisPanelProvider;
import org.apache.lucene.luke.app.desktop.components.CommitsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.DocumentsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.LogsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.LukeWindowProvider;
import org.apache.lucene.luke.app.desktop.components.MenuBarProvider;
import org.apache.lucene.luke.app.desktop.components.OverviewPanelProvider;
import org.apache.lucene.luke.app.desktop.components.SearchPanelProvider;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.CheckIndexDialogProvider;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogProvider;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OptimizeIndexDialogProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.CustomAnalyzerPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.PresetAnalyzerPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.AnalyzerPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.FieldValuesPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.MLTPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.QueryParserPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SimilarityPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SearchSortPaneProvider;

import javax.swing.*;

public class DesktopModule extends AbstractModule {

  private static final Injector injector = Guice.createInjector(new DesktopModule());

  public static Injector getIngector() {
    return injector;
  }

  @Override
  protected void configure() {
    install(new LukeModule());

    bind(JMenuBar.class).toProvider(MenuBarProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("overview")).toProvider(OverviewPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("documents")).toProvider(DocumentsPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("search")).toProvider(SearchPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("analysis")).toProvider(AnalysisPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("commits")).toProvider(CommitsPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("logs")).toProvider(LogsPanelProvider.class);

    bind(JScrollPane.class).annotatedWith(Names.named("search_qparser")).toProvider(QueryParserPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_analyzer")).toProvider(AnalyzerPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_similarity")).toProvider(SimilarityPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_sort")).toProvider(SearchSortPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_values")).toProvider(FieldValuesPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_mlt")).toProvider(MLTPaneProvider.class);

    bind(JPanel.class).annotatedWith(Names.named("analysis_preset")).toProvider(PresetAnalyzerPaneProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("analysis_custom")).toProvider(CustomAnalyzerPaneProvider.class);

    bind(JFrame.class).toProvider(LukeWindowProvider.class);

    bind(JDialog.class).annotatedWith(Names.named("menubar_openindex")).toProvider(OpenIndexDialogProvider.class);
    bind(JDialog.class).annotatedWith(Names.named("menubar_optimize")).toProvider(OptimizeIndexDialogProvider.class);
    bind(JDialog.class).annotatedWith(Names.named("menubar_checkidx")).toProvider(CheckIndexDialogProvider.class);
  }

  private DesktopModule() {}
}
