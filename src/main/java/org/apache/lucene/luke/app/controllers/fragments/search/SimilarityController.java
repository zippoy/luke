package org.apache.lucene.luke.app.controllers.fragments.search;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.search.SimilarityConfig;

public class SimilarityController {

  @FXML
  private CheckBox useClassic;

  @FXML
  private CheckBox discountOverlaps;

  @FXML
  private TextField k1Val;

  @FXML
  private TextField bVal;

  @FXML
  private void initialize() {
    this.config = new SimilarityConfig();

    useClassic.setSelected(config.isUseClassicSimilarity());
    useClassic.setOnAction(e -> {
      if (useClassic.isSelected()) {
        k1Val.setDisable(true);
        bVal.setDisable(true);
      } else {
        k1Val.setDisable(false);
        bVal.setDisable(false);
      }
    });

    discountOverlaps.setSelected(config.isUseClassicSimilarity());
    k1Val.setText(String.valueOf(config.getK1()));
    bVal.setText(String.valueOf(config.getB()));
  }

  private SimilarityConfig config;

  public SimilarityConfig getConfig() throws LukeException {
    config.setUseClassicSimilarity(useClassic.isSelected());
    config.setDiscountOverlaps(discountOverlaps.isSelected());
    try {
      config.setK1(Float.parseFloat(k1Val.getText()));
    } catch (NumberFormatException e) {
      throw new LukeException("Invalid input for k1: " + k1Val.getText());
    }
    try {
      config.setB(Float.parseFloat(bVal.getText()));
    } catch (NumberFormatException e) {
      throw new LukeException("Invalid input for b: " + bVal.getText());
    }

    return config;
  }
}
