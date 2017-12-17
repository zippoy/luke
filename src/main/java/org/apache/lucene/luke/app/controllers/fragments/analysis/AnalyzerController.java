package org.apache.lucene.luke.app.controllers.fragments.analysis;

import org.apache.lucene.luke.app.controllers.AnalysisController;
import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.luke.models.analysis.Analysis;

public interface AnalyzerController {
  void setParent(AnalysisController analysisController, LukeController parent);

  void populate(Analysis modelAnalysis);

  void resetSelectedAnalyzer() throws Exception;
}
