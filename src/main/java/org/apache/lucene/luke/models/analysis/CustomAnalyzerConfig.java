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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomAnalyzerConfig {

  private Optional<String> configDir;

  private ComponentConfig tokenizerConfig;

  private List<ComponentConfig> charFilterConfigs;

  private List<ComponentConfig> tokenFilterConfigs;

  public CustomAnalyzerConfig(@Nonnull String tokenizerName, @Nonnull Map<String, String> tokenizerParams, @Nullable String configDir) {
    this.tokenizerConfig = new ComponentConfig(tokenizerName, tokenizerParams);
    this.configDir = Optional.ofNullable(configDir);
    this.charFilterConfigs = new ArrayList<>();
    this.tokenFilterConfigs = new ArrayList<>();
  }

  public void addCharFilterConfig(String name, Map<String, String> params) {
    this.charFilterConfigs.add(new ComponentConfig(name, params));
  }

  public void addTokenFilterConfig(String name, Map<String, String> params) {
    this.tokenFilterConfigs.add(new ComponentConfig(name, params));
  }

  Optional<String> getConfigDir() {
    return configDir;
  }

  ComponentConfig getTokenizerConfig() {
    return this.tokenizerConfig;
  }

  List<ComponentConfig> getCharFilterConfigs() {
    return this.charFilterConfigs;
  }

  List<ComponentConfig> getTokenFilterConfigs() {
    return this.tokenFilterConfigs;
  }

  class ComponentConfig {

    private String name;
    private Map<String, String> params;

    ComponentConfig(String name, Map<String, String> params) {
      this.name = name;
      this.params = params;
    }

    String getName() {
      return this.name;
    }

    Map<String, String> getParams() {
      return this.params;
    }
  }
}
