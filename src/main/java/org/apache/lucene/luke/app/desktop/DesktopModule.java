package org.apache.lucene.luke.app.desktop;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.LukeModule;
import org.apache.lucene.luke.app.desktop.components.AnalysisPanelProvider;
import org.apache.lucene.luke.app.desktop.components.CommitsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.DocumentsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.LogsPanelProvider;
import org.apache.lucene.luke.app.desktop.components.LukeWindowProvider;
import org.apache.lucene.luke.app.desktop.components.MenuBarProvider;
import org.apache.lucene.luke.app.desktop.components.OverviewPanelProvider;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.SearchPanelProvider;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;
import org.apache.lucene.luke.app.desktop.components.dialog.ConfirmDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.HelpDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.EditFiltersDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.EditParamsDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.TokenAttributeDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.AddDocumentDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.DocValuesDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.IndexOptionsDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.StoredValueDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.documents.TermVectorDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.AboutDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.CheckIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OpenIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.components.dialog.menubar.OptimizeIndexDialogFactory;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.CustomAnalyzerPanelProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.analysis.PresetAnalyzerPanelProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.AnalyzerPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.FieldValuesPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.MLTPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.QueryParserPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SimilarityPaneProvider;
import org.apache.lucene.luke.app.desktop.components.fragments.search.SortPaneProvider;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.models.tools.IndexToolsFactory;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


public class DesktopModule extends AbstractModule {

  private static final Injector injector = Guice.createInjector(new DesktopModule());

  public static Injector getIngector() {
    return injector;
  }

  @Override
  protected void configure() {
    // luke core module
    install(new LukeModule());

    // UI components and fragments
    bind(ComponentOperatorRegistry.class).toInstance(new ComponentOperatorRegistry());
    bind(TabbedPaneProvider.TabSwitcherProxy.class).toInstance(new TabbedPaneProvider.TabSwitcherProxy());
    bind(MessageBroker.class).toInstance(new MessageBroker());

    bind(JMenuBar.class).toProvider(MenuBarProvider.class);

    bind(JTextArea.class).annotatedWith(Names.named("log_area")).toInstance(new JTextArea());

    bind(JPanel.class).annotatedWith(Names.named("overview")).toProvider(OverviewPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("documents")).toProvider(DocumentsPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("search")).toProvider(SearchPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("analysis")).toProvider(AnalysisPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("commits")).toProvider(CommitsPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("logs")).toProvider(LogsPanelProvider.class);
    bind(JTabbedPane.class).annotatedWith(Names.named("main")).toProvider(TabbedPaneProvider.class);

    bind(JScrollPane.class).annotatedWith(Names.named("search_qparser")).toProvider(QueryParserPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_analyzer")).toProvider(AnalyzerPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_similarity")).toProvider(SimilarityPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_sort")).toProvider(SortPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_values")).toProvider(FieldValuesPaneProvider.class);
    bind(JScrollPane.class).annotatedWith(Names.named("search_mlt")).toProvider(MLTPaneProvider.class);

    bind(JPanel.class).annotatedWith(Names.named("analysis_preset")).toProvider(PresetAnalyzerPanelProvider.class);
    bind(JPanel.class).annotatedWith(Names.named("analysis_custom")).toProvider(CustomAnalyzerPanelProvider.class);

    bind(JFrame.class).toProvider(LukeWindowProvider.class);

    bind(OpenIndexDialogFactory.class).toInstance(new OpenIndexDialogFactory());
    bind(IndexOptionsDialogFactory.class).toInstance(new IndexOptionsDialogFactory());
    bind(TermVectorDialogFactory.class).toInstance(new TermVectorDialogFactory());
    bind(DocValuesDialogFactory.class).toInstance(new DocValuesDialogFactory());
    bind(StoredValueDialogFactory.class).toInstance(new StoredValueDialogFactory());
    bind(TokenAttributeDialogFactory.class).toInstance(new TokenAttributeDialogFactory());
    bind(AboutDialogFactory.class).toInstance(new AboutDialogFactory());
    bind(HelpDialogFactory.class).toInstance(new HelpDialogFactory());
    bind(ConfirmDialogFactory.class).toInstance(new ConfirmDialogFactory());
  }

  @Provides
  @Singleton
  public OptimizeIndexDialogFactory provideOptimizeIndexDialogFactory(
      IndexToolsFactory indexToolsFactory, IndexHandler indexHandler) {
    return new OptimizeIndexDialogFactory(indexToolsFactory, indexHandler);
  }

  @Provides
  @Singleton
  public CheckIndexDialogFactory provideCheckIndexDialogFactory(
      IndexToolsFactory indexToolsFactory, IndexHandler indexHandler, DirectoryHandler directoryHandler) {
    return new CheckIndexDialogFactory(indexToolsFactory, indexHandler, directoryHandler);
  }

  @Provides
  @Singleton
  public AddDocumentDialogFactory provideAddDocumentDialogFactory(
      IndexOptionsDialogFactory indexOptionsDialogFactory, HelpDialogFactory helpDialogFactory,
      IndexHandler indexHandler, IndexToolsFactory toolsFactory,
      TabbedPaneProvider.TabSwitcherProxy tabSwitcherProxy, ComponentOperatorRegistry operatorRegistry) {
    return new AddDocumentDialogFactory(indexOptionsDialogFactory, helpDialogFactory, indexHandler, toolsFactory, tabSwitcherProxy, operatorRegistry);
  }

  @Provides
  @Singleton
  public EditFiltersDialogFactory provideEditFiltersDialogFactory(
      ComponentOperatorRegistry operatorRegistry,
      EditParamsDialogFactory editParamsDialogFactory) {
    return new EditFiltersDialogFactory(operatorRegistry, editParamsDialogFactory);
  }

  @Provides
  @Singleton
  public EditParamsDialogFactory provideEditParamsDialogFactory(ComponentOperatorRegistry operatorRegistry) {
    return new EditParamsDialogFactory(operatorRegistry);
  }

  private DesktopModule() {}
}
