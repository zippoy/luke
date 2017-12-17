package org.apache.lucene.luke.models.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.luke.models.LukeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Analysis {

  class Token {
    public String term;
    public List<TokenAttribute> attributes = new ArrayList<>();
  }

  class TokenAttribute {
    public String attClass;
    public Map<String, String> attValues;
  }

  Collection<Class<? extends Analyzer>> getPresetAnalyzerTypes();

  Collection<Class<? extends CharFilterFactory>> getAvailableCharFilterFactories();

  Collection<Class<? extends TokenizerFactory>> getAvailableTokenizerFactories();

  Collection<Class<? extends TokenFilterFactory>> getAvailableTokenFilterFactories();

  Analyzer createAnalyzerFromClassName(String analyzerType) throws LukeException;

  CustomAnalyzer buildCustomAnalyzer(CustomAnalyzerConfig config) throws LukeException;

  List<Token> analyze(String text) throws LukeException;

  Analyzer currentAnalyzer() throws LukeException;

  void addExternalJars(List<String> jarFiles) throws LukeException;

}
