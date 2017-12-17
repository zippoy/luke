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
