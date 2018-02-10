/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
