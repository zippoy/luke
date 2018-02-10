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

package org.apache.lucene.luke.models.search;

import org.apache.lucene.document.DateTools;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class QueryParserConfig {

  public enum Operator {
    AND, OR
  }

  private boolean useClassicParser = true;

  private boolean enablePositionIncrements = true;

  private boolean allowLeadingWildcard = false;

  private DateTools.Resolution dateResolution = DateTools.Resolution.MILLISECOND;

  private Operator defaultOperator = Operator.OR;

  private float fuzzyMinSim = 2f;

  private int fuzzyPrefixLength = 0;

  private Locale locale = Locale.getDefault();

  private TimeZone timeZone = TimeZone.getDefault();

  private int phraseSlop = 0;

  // classic parser only configurations
  private boolean autoGenerateMultiTermSynonymsPhraseQuery = false;

  private boolean autoGeneratePhraseQueries = false;

  private boolean splitOnWhitespace = false;

  // standard parser only configurations
  private Map<String, Class<? extends Number>> typeMap = new HashMap<>();

  public boolean isUseClassicParser() {
    return useClassicParser;
  }

  public void setUseClassicParser(boolean useClassicParser) {
    this.useClassicParser = useClassicParser;
  }

  public boolean isAutoGenerateMultiTermSynonymsPhraseQuery() {
    return autoGenerateMultiTermSynonymsPhraseQuery;
  }

  public void setAutoGenerateMultiTermSynonymsPhraseQuery(boolean autoGenerateMultiTermSynonymsPhraseQuery) {
    this.autoGenerateMultiTermSynonymsPhraseQuery = autoGenerateMultiTermSynonymsPhraseQuery;
  }

  public boolean isEnablePositionIncrements() {
    return enablePositionIncrements;
  }

  public void setEnablePositionIncrements(boolean enablePositionIncrements) {
    this.enablePositionIncrements = enablePositionIncrements;
  }

  public boolean isAllowLeadingWildcard() {
    return allowLeadingWildcard;
  }

  public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
    this.allowLeadingWildcard = allowLeadingWildcard;
  }

  public boolean isAutoGeneratePhraseQueries() {
    return autoGeneratePhraseQueries;
  }

  public void setAutoGeneratePhraseQueries(boolean autoGeneratePhraseQueries) {
    this.autoGeneratePhraseQueries = autoGeneratePhraseQueries;
  }

  public boolean isSplitOnWhitespace() {
    return splitOnWhitespace;
  }

  public void setSplitOnWhitespace(boolean splitOnWhitespace) {
    this.splitOnWhitespace = splitOnWhitespace;
  }

  public DateTools.Resolution getDateResolution() {
    return dateResolution;
  }

  public void setDateResolution(DateTools.Resolution dateResolution) {
    this.dateResolution = dateResolution;
  }

  public Operator getDefaultOperator() {
    return defaultOperator;
  }

  public void setDefaultOperator(Operator defaultOperator) {
    this.defaultOperator = defaultOperator;
  }

  public float getFuzzyMinSim() {
    return fuzzyMinSim;
  }

  public void setFuzzyMinSim(float fuzzyMinSim) {
    this.fuzzyMinSim = fuzzyMinSim;
  }

  public int getFuzzyPrefixLength() {
    return fuzzyPrefixLength;
  }

  public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
    this.fuzzyPrefixLength = fuzzyPrefixLength;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public TimeZone getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  public int getPhraseSlop() {
    return phraseSlop;
  }

  public void setPhraseSlop(int phraseSlop) {
    this.phraseSlop = phraseSlop;
  }

  public void setTypeMap(Map<String, Class<? extends Number>> typeMap) {
    this.typeMap = typeMap;
  }

  public Map<String, Class<? extends Number>> getTypeMap() {
    return typeMap;
  }

  @Override
  public String toString() {
    return "QueryParserConfig: [" +
        String.format(" default operator=%s;", defaultOperator.name()) +
        String.format(" enable position increment=%s;", enablePositionIncrements) +
        String.format(" allow leading wildcard=%s; ", allowLeadingWildcard) +
        String.format(" split whitespace=%s;", splitOnWhitespace) +
        String.format(" generate phrase query=%s;", autoGeneratePhraseQueries) +
        String.format(" generate multiterm sysnonymsphrase query=%s;", autoGenerateMultiTermSynonymsPhraseQuery) +
        String.format(" phrase slop=%d;", phraseSlop) +
        String.format(" date resolution=%s;", dateResolution.name()) +
        String.format(" locale=%s;", locale.toString()) +
        String.format(" time zone=%s;", timeZone.getID()) +
        String.format(" numeric types=%s;", String.join(",", getTypeMap().entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue().toString()).collect(Collectors.toSet()))) +
        "]";
  }
}
