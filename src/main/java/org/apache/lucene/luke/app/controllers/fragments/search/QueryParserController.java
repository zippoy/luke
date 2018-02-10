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

package org.apache.lucene.luke.app.controllers.fragments.search;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.luke.app.controllers.dto.PVField;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.search.QueryParserConfig;
import org.apache.lucene.luke.models.search.Search;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class QueryParserController {

  private static final String PARSER_STANDARD = "standard";
  private static final String PARSER_CLASSIC = "classic";

  private static final int ROW_HEIGHT = 30;

  @FXML
  private ToggleGroup parserGroup;

  @FXML
  private ChoiceBox<String> defField;

  private ObservableList<String> defFieldList;

  @FXML
  private ChoiceBox<String> defOp;

  private ObservableList<String> defOpList;

  @FXML
  private CheckBox posIncr;

  @FXML
  private CheckBox leadWildcard;

  @FXML
  private CheckBox splitWs;

  @FXML
  private CheckBox genPq;

  @FXML
  private CheckBox genMTS;

  @FXML
  private TextField slop;

  @FXML
  private TextField fuzzyMinSim;

  @FXML
  private TextField fuzzyPrefLen;

  @FXML
  private ChoiceBox<String> dateRes;

  private ObservableList<String> dateResList;

  @FXML
  private TextField locale;

  @FXML
  private TextField timeZone;

  @FXML
  private TableView<PVField> pvFieldsTable;

  @FXML
  private TableColumn<PVField, String> fieldColumn;

  @FXML
  private TableColumn<PVField, PVField.Type> typeColumn;

  private ObservableList<PVField> pvFieldList;

  @FXML
  private void initialize() {
    this.config = new QueryParserConfig();

    parserGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
      if (newV.getUserData().equals(PARSER_CLASSIC)) {
        splitWs.setDisable(false);
        genPq.setDisable(false);
        genMTS.setDisable(false);
        pvFieldsTable.setDisable(true);
      } else {
        splitWs.setDisable(true);
        genPq.setDisable(true);
        genMTS.setDisable(true);
        pvFieldsTable.setDisable(false);
      }
    });
    defFieldList = FXCollections.observableArrayList();
    defField.setItems(defFieldList);

    defOpList = FXCollections.observableArrayList(
        Arrays.stream(QueryParser.Operator.values())
            .map(QueryParser.Operator::name).collect(Collectors.toList()));
    defOp.setItems(defOpList);
    defOp.setValue(config.getDefaultOperator().name());

    posIncr.setSelected(config.isEnablePositionIncrements());
    leadWildcard.setSelected(config.isAllowLeadingWildcard());
    splitWs.setSelected(config.isSplitOnWhitespace());
    splitWs.setDisable(true);

    genPq.setSelected(config.isAutoGeneratePhraseQueries());
    genPq.setDisable(true);
    genMTS.setSelected(config.isAutoGenerateMultiTermSynonymsPhraseQuery());
    genMTS.setDisable(true);
    slop.setText(String.valueOf(config.getPhraseSlop()));

    fuzzyMinSim.setText(String.valueOf(config.getFuzzyMinSim()));
    fuzzyPrefLen.setText(String.valueOf(config.getFuzzyPrefixLength()));

    dateResList = FXCollections.observableArrayList(
        Arrays.stream(DateTools.Resolution.values())
            .map(DateTools.Resolution::name).collect(Collectors.toList())
    );
    dateRes.setItems(dateResList);
    dateRes.setValue(config.getDateResolution().name());

    locale.setText(config.getLocale().toString());
    timeZone.setText(config.getTimeZone().getID());

    fieldColumn.setCellValueFactory(new PropertyValueFactory<>("field"));
    typeColumn.setCellValueFactory(data -> data.getValue().getTypeProperty());
    typeColumn.setCellFactory(col -> {
      ChoiceBoxTableCell<PVField, PVField.Type> cell = new ChoiceBoxTableCell<>();
      cell.setConverter(new StringConverter<PVField.Type>() {
        @Override
        public String toString(PVField.Type type) {
          return type.name();
        }

        @Override
        public PVField.Type fromString(String name) {
          return PVField.Type.valueOf(name);
        }
      });
      cell.getItems().addAll(PVField.Type.values());
      return cell;
    });
    pvFieldList = FXCollections.observableArrayList();
    pvFieldsTable.setItems(pvFieldList);

  }

  private QueryParserConfig config;

  private Search modelSearch;

  @Inject
  public QueryParserController(Search modelSearch) {
    this.modelSearch = modelSearch;
  }

  public QueryParserConfig getConfig() throws LukeException {
    if (parserGroup.getSelectedToggle().getUserData().equals(PARSER_CLASSIC)) {
      config.setUseClassicParser(true);
    } else {
      config.setUseClassicParser(false);
    }
    config.setDefaultOperator(QueryParserConfig.Operator.valueOf(defOp.getValue()));
    config.setEnablePositionIncrements(posIncr.isSelected());
    config.setAllowLeadingWildcard(leadWildcard.isSelected());
    config.setSplitOnWhitespace(splitWs.isSelected());
    config.setAutoGeneratePhraseQueries(genPq.isSelected());
    config.setAutoGenerateMultiTermSynonymsPhraseQuery(genMTS.isSelected());
    try {
      config.setPhraseSlop(Integer.parseInt(slop.getText()));
    } catch (NumberFormatException e) {
      throw new LukeException("Invalid input for phrase slop: " + slop.getText(), e);
    }
    try {
      config.setFuzzyMinSim(Float.parseFloat(fuzzyMinSim.getText()));
    } catch (NumberFormatException e) {
      throw new LukeException("Invalid input for fuzzy minimal similarity: " + fuzzyMinSim.getText(), e);
    }
    try {
      config.setFuzzyPrefixLength(Integer.parseInt(fuzzyPrefLen.getText()));
    } catch (NumberFormatException e) {
      throw new LukeException("Invalid input for fuzzy prefix length: " + fuzzyPrefLen.getText(), e);
    }
    config.setDateResolution(DateTools.Resolution.valueOf(dateRes.getValue()));
    config.setLocale(new Locale(locale.getText()));
    config.setTimeZone(TimeZone.getTimeZone(timeZone.getText()));

    Map<String, Class<? extends Number>> typeMap = new HashMap<>();
    for (PVField pvField : pvFieldList) {
      switch (pvField.getType()) {
        case INT:
          typeMap.put(pvField.getField(), Integer.class);
          break;
        case LONG:
          typeMap.put(pvField.getField(), Long.class);
          break;
        case FLOAT:
          typeMap.put(pvField.getField(), Float.class);
          break;
        case DOUBLE:
          typeMap.put(pvField.getField(), Double.class);
          break;
        default:
          break;
      }
    }
    config.setTypeMap(typeMap);

    return config;
  }

  public String getDefField() {
    return defField.getValue();
  }

  public void populateFields() {
    Collection<String> searchableFields = modelSearch.getSearchableFieldNames();
    defFieldList.clear();
    defFieldList.addAll(searchableFields);
    if (defFieldList.size() > 0) {
      defField.setValue(defFieldList.get(0));
    }

    Collection<String> rangeSearchableFields = modelSearch.getRangeSearchableFieldNames();
    pvFieldList.clear();
    pvFieldList.addAll(rangeSearchableFields.stream().map(PVField::of).collect(Collectors.toSet()));
    pvFieldsTable.setPrefHeight(Math.max(ROW_HEIGHT * pvFieldList.size(), 80));
  }
}
