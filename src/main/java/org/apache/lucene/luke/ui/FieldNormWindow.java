package org.apache.lucene.luke.ui;

import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import java.net.URL;

public class FieldNormWindow extends Dialog implements Bindable {

  @BXML
  private Label field;
  @BXML
  private Label normVal;
  @BXML
  private TextInput simclass;
  @BXML
  private Label simErr;
  @BXML
  private PushButton refreshButton;
  @BXML
  private TextInput otherNorm;
  @BXML
  private Label encNorm;

  private Resources resources;

  private String fieldName;

  private static TFIDFSimilarity defaultSimilarity = new DefaultSimilarity();

  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  public void initFieldNorm(int docId, String fieldName, NumericDocValues norms) throws Exception {
    this.fieldName = fieldName;
    TFIDFSimilarity sim = defaultSimilarity;
    byte curBVal = (byte) norms.get(docId);
    float curFVal = Util.decodeNormValue(curBVal, fieldName, sim);
    field.setText(fieldName);
    normVal.setText(Float.toString(curFVal));
    simclass.setText(sim.getClass().getName());
    otherNorm.setText(Float.toString(curFVal));
    encNorm.setText(Float.toString(curFVal) + " (0x" + Util.byteToHex(curBVal) + ")");

    refreshButton.setAction(new Action() {
      @Override
      public void perform(Component component) {
        changeNorms();
      }
    });
    otherNorm.getTextInputContentListeners().add(new TextInputContentListener.Adapter(){
      @Override
      public void textChanged(TextInput textInput) {
        changeNorms();
      }
    });
  }

  private void changeNorms() {
    String simClassString = simclass.getText();

    Similarity sim = createSimilarity(simClassString);
    TFIDFSimilarity s = null;
    if (sim != null && (sim instanceof TFIDFSimilarity)) {
      s = (TFIDFSimilarity)sim;
    } else {
      s = defaultSimilarity;
    }
    if (s == null) {
      s = defaultSimilarity;
    }
    //setString(sim, "text", s.getClass().getName());
    simclass.setText(s.getClass().getName());
    try {
      float newFVal = Float.parseFloat(otherNorm.getText());
      long newBVal = Util.encodeNormValue(newFVal, fieldName, s);
      float encFVal = Util.decodeNormValue(newBVal, fieldName, s);
      encNorm.setText(String.valueOf(encFVal) + " (0x" + Util.byteToHex((byte) (newBVal & 0xFF)) + ")");
    } catch (Exception e) {
      // TODO:
      e.printStackTrace();
    }
  }

  public Similarity createSimilarity(String simClass) {
    //Object ckSimDef = find(srchOpts, "ckSimDef");
    //Object ckSimSweet = find(srchOpts, "ckSimSweet");
    //Object ckSimOther = find(srchOpts, "ckSimOther");
    //Object simClass = find(srchOpts, "simClass");
    //Object ckSimCust = find(srchOpts, "ckSimCust");
    //if (getBoolean(ckSimDef, "selected")) {
    //      return new DefaultSimilarity();
    //} else if (getBoolean(ckSimSweet, "selected")) {
    //  return new SweetSpotSimilarity();
    //} else if (getBoolean(ckSimOther, "selected")) {
    try {
      Class clazz = Class.forName(simClass);
      if (Similarity.class.isAssignableFrom(clazz)) {
        Similarity sim = (Similarity) clazz.newInstance();
        simErr.setVisible(false);
        return sim;
      } else {
        simErr.setText("Not a subclass of Similarity: " + clazz.getName());
        simErr.setVisible(true);
      }
    } catch (Exception e) {
      simErr.setText("Invalid similarity class " + simClass + ", using DefaultSimilarity.");
      simErr.setVisible(true);
    }
    return new DefaultSimilarity();
  }
}
