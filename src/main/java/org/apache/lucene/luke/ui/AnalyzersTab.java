package org.apache.lucene.luke.ui;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.luke.ui.LukeWindow.LukeMediator;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.*;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.ListView.SelectMode;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Iterator;

public class AnalyzersTab extends TablePane implements Bindable {
  
  @BXML
  private ListButton analyzersListButton;
  @BXML
  private ListButton luceneVersionListButton;
  @BXML
  private TextArea inputText;
  @BXML
  private TableView tokenAttsTable;
  @BXML
  private ListView resultsList;
  
  private Map<Integer,AttributeSource> attribs;
  
  public AnalyzersTab() {
    Action.getNamedActions().put("analyze", new Action() {
      @Override
      public void perform(Component component) {
        analyze();
      }
    });
  }
  
  public void initLukeMediator(LukeMediator lukeMediator) {
    
    analyzersListButton.setListData(new ArrayList<String>(lukeMediator
        .getAnalyzerNames()));
    analyzersListButton.setSelectedIndex(0);
    List<String> versions = new ArrayList<String>();
    // TODO: Version.values() was removed, and Version.LUCENE_X_X_X were all depricated. How do we fix this line?
    //Version[] values = Version.values();
    /*
    Version[] values = {
      Version.LUCENE_4_1_0, Version.LUCENE_4_2_0, Version.LUCENE_4_3_0, Version.LUCENE_4_4_0,
      Version.LUCENE_4_5_0, Version.LUCENE_4_6_0, Version.LUCENE_4_7_0, Version.LUCENE_4_8_0,
      Version.LUCENE_4_9_0, Version.LUCENE_4_10_0
    };
    for (int i = 0; i < values.length; i++) {
      Version v = values[i];
      versions.add(v.toString());
    }
    luceneVersionListButton.setListData(versions);
    luceneVersionListButton.setSelectedItem("LUCENE_CURRENT");
    */
  }
  
  @Override
  public void initialize(Map<String,Object> namespace, URL location,
      Resources resources) {
    resultsList.setSelectMode(SelectMode.SINGLE);
    resultsList.getListViewSelectionListeners().add(
        new ListViewSelectionListener() {
          
          @Override
          public void selectedRangesChanged(ListView listView,
              Sequence<Span> previousSelectedRanges) {
            // TODO Auto-generated method stub
            
          }
          
          @Override
          public void selectedRangeRemoved(ListView listView, int rangeStart,
              int rangeEnd) {
            // TODO Auto-generated method stub
            
          }
          
          @Override
          public void selectedRangeAdded(ListView listView, int rangeStart,
              int rangeEnd) {
            // TODO Auto-generated method stub
            
          }
          
          @Override
          public void selectedItemChanged(ListView listView,
              Object previousSelectedItem) {
            tokenChange();
            
          }
        });
  }
  
  public void analyze() {
    try {
      //Version v = Version.valueOf((String) luceneVersionListButton
      // .getSelectedItem());
      Version v = Version.parseLeniently((String) luceneVersionListButton
        .getSelectedItem());
      Class clazz = Class.forName((String) analyzersListButton
        .getSelectedItem());
      Analyzer analyzer = null;
      try {
        Constructor<Analyzer> c = clazz.getConstructor(Version.class);
        analyzer = c.newInstance(v);
      } catch (Throwable t) {
        try {
          // no constructor with Version ?
          analyzer = (Analyzer) clazz.newInstance();
        } catch (Throwable t1) {
          t1.printStackTrace();
          // TODO:
          // app
          // .showStatus("Couldn't instantiate analyzer - public 0-arg or 1-arg constructor(Version) required");
          return;
        }
      }
      attribs = new HashMap<Integer,AttributeSource>();
      TokenStream ts = analyzer.tokenStream("text",
          new StringReader(inputText.getText()));
      List<String> listData = new ArrayList<String>();
      ts.reset();
      int cnt = 0;
      while (ts.incrementToken()) {
        
        listData.add(((CharTermAttribute) ts
            .getAttribute(CharTermAttribute.class)).toString());
        
        attribs.put(cnt, ts.cloneAttributes());
        cnt++;
      }
      
      resultsList.setListData(listData);
      
    } catch (Throwable t) {
      t.printStackTrace();
      // TODO:
      // app.showStatus("Error analyzing:" + t.getMessage());
    }
    tokenChange();
  }
  
  public void tokenChange() {
    int selectedIndex = resultsList.getSelectedIndex();
    if (selectedIndex == -1) {
      return;
    }
    AttributeSource attribSource = attribs.get(selectedIndex);
    if (attribSource == null) {
      return;
    }
    Iterator it = attribSource.getAttributeClassesIterator();
    ArrayList<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
    
    tokenAttsTable.setTableData(tableData);
    while (it.hasNext()) {
      Class cl = (Class) it.next();
      String attClass = cl.getName();
      if (attClass.startsWith("org.apache.lucene.")) {
        attClass = cl.getSimpleName();
      }
      Attribute att = attribSource.getAttribute(cl);
      String implClass = att.getClass().getName();
      if (implClass.startsWith("org.apache.lucene.")) {
        implClass = att.getClass().getSimpleName();
      }
      
      Map<String,String> row = new HashMap<String,String>();
      
      tableData.add(row);
      
      row.put("class", attClass);
      row.put("impl", implClass);
      
      String val = null;
      if (attClass.equals("CharTermAttribute")) {
        val = ((CharTermAttribute) att).toString();
      } else if (attClass.equals("FlagsAttribute")) {
        val = Integer.toHexString(((FlagsAttribute) att).getFlags());
      } else if (attClass.equals("OffsetAttribute")) {
        OffsetAttribute off = (OffsetAttribute) att;
        val = off.startOffset() + "," + off.endOffset();
      } else if (attClass.equals("PayloadAttribute")) {
        BytesRef payload = ((PayloadAttribute) att).getPayload();
        if (payload != null) {
          byte[] data = payload.bytes;
          val = Util.bytesToHex(data, 0, data.length, false);
        } else {
          val = "";
        }
      } else if (attClass.equals("PositionIncrementAttribute")) {
        val = ((PositionIncrementAttribute) att).getPositionIncrement() + "";
      } else if (attClass.equals("TypeAttribute")) {
        val = ((TypeAttribute) att).type();
      } else {
        val = att.toString();
      }
      row.put("value", val);
    }
    
    if (attribSource.hasAttribute(OffsetAttribute.class)) {
      OffsetAttribute off = (OffsetAttribute) attribSource
          .getAttribute(OffsetAttribute.class);
      
      inputText.setSelection(off.startOffset(),
          off.endOffset() - off.startOffset());
      inputText.requestFocus();
    }
  }
  
}