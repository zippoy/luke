package org.apache.lucene.luke.ui;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.luke.core.*;
import org.apache.lucene.luke.core.decoders.Decoder;
import org.apache.lucene.luke.ui.LukeWindow.LukeMediator;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.Version;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.TableView.Column;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

public class SearchTab extends SplitPane implements Bindable {

  private LukeMediator lukeMediator;

  private Analyzer stdAnalyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);

  @BXML
  private TextArea queryTextArea;
  @BXML
  private TableView searchResultsTable;
  @BXML
  private ListButton analyzerList;
  @BXML
  private Label parsedQueryLabel;
  @BXML
  private Label rewrittenQueryLabel;

  private DecimalFormat df = new DecimalFormat("0.0000");

  private IndexInfo indexInfo;

  public SearchTab() {
    Action.getNamedActions().put("search", new Action() {
      @Override
      public void perform(Component component) {
        search();
      }
    });

    Action.getNamedActions().put("showParsed", new Action() {
      @Override
      public void perform(Component component) {

      }
    });
    Action.getNamedActions().put("explainStructure", new Action() {
      @Override
      public void perform(Component component) {

      }
    });

  }

  @Override
  public void initialize(Map<String,Object> namespace, URL location, Resources resources) {
  }

  public void initLukeMediator(LukeMediator lukeMediator) {
    this.lukeMediator = lukeMediator;
    String lastAnalyzer = Prefs.getProperty(Prefs.P_ANALYZER);
    if (lastAnalyzer != null)
      analyzerList.setSelectedItem(lastAnalyzer);

    analyzerList.setListData(new ArrayList<String>(lukeMediator.getAnalyzerNames()));
    analyzerList.setSelectedIndex(0);
  }

  /**
   * Initialize GUI elements. This method is called when a new index is opened.
   * 
   * @throws Exception
   */
  public void onOpenIndex() throws Exception {
    this.indexInfo = lukeMediator.getIndexInfo();
  }

  /**
   * Update the parsed and rewritten query views.
   * 
   */
  public void showParsed(String query) {
    // Object qField = find("qField");

    if (query.trim().equals("")) {
      parsedQueryLabel.setText("<empty query>");
      return;
    }
    try {
      Query q = createQuery(query);
      parsedQueryLabel.setText(q.toString());
      // putProperty(qField, "qParsed", q);
      q = q.rewrite(lukeMediator.getIndexInfo().getReader());
      rewrittenQueryLabel.setText(q.toString());
      // putProperty(qField, "qRewritten", q);
    } catch (Throwable t) {
      parsedQueryLabel.setText(t.getMessage());
      // setString(qFieldRewritten, "text", t.getMessage());
    }
  }

  public void search() {
    if (lukeMediator.getIndexInfo().getReader() == null) {
      // TODO:
      // showStatus(MSG_NOINDEX);
      return;
    }
    String query = queryTextArea.getText();
    if (query.trim().equals("")) {
      // TODO:
      // showStatus("FAILED: Empty query.");
      return;
    }
    Object srchOpts = null; // find("srchOptTabs");
    // query parser opts
    DefaultSimilarity sim = createSimilarity();
    AccessibleHitCollector col;
    try {
      col = createCollector();
    } catch (Throwable t) {
      // TODO:
      // errorMsg("ERROR creating Collector: " + t.getMessage());
      return;
    }

    // Object cntRepeat = find("cntRepeat");
    int repeat = 1; // Integer.parseInt(getString(cntRepeat, "text"));
    // removeAll(sTable);
    Query q = null;
    try {
      q = createQuery(query);
      indexInfo.getIndexSearcher().setSimilarity(sim);
      showParsed(query);
      search(q, indexInfo.getIndexSearcher(), col, repeat);
    } catch (Throwable e) {
      e.printStackTrace();
      errorMsg(e.getMessage());
    }
  }

  int resStart = 0;
  int resCount = 20;
  LimitedException le = null;

  private Analyzer analyzer;

  protected AccessibleHitCollector collector;

  private void search(final Query q, final IndexSearcher is, AccessibleHitCollector hc, final int repeat) throws Exception {
    if (hc == null) {
      hc = new AccessibleTopHitCollector(1000, true, true);
    }
    final AccessibleHitCollector collector = hc;
    le = null;
    Task<Object> collectTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        long startTime = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
          if (i > 0) {
            collector.reset();
          }
          try {
            is.search(q, collector);
          } catch (LimitedException e) {
            le = e;
          } catch (Throwable th) {
            th.printStackTrace();
            errorMsg("ERROR searching: " + th.toString());
            return;
          }
        }
        long endTime = System.nanoTime();
        long delta = (endTime - startTime) / 1000 / repeat;
        String msg;
        if (delta > 100000) {
          msg = delta / 1000 + " ms";
        } else {
          msg = delta + " us";
        }
        if (repeat > 1) {
          msg += " (avg of " + repeat + " runs)";
        }
        // showSearchStatus(msg);
        // Object bsPrev = find("bsPrev");
        // Object bsNext = find("bsNext");
        // setBoolean(bsNext, "enabled", false);
        // setBoolean(bsPrev, "enabled", false);
        int resNum = collector.getTotalHits();
        if (resNum == 0) {
          // Object row = create("row");
          // Object cell = create("cell");
          // add(sTable, row);
          // add(row, cell);
          // cell = create("cell");
          // add(row, cell);
          // cell = create("cell");
          // setString(cell, "text", "No Results");
          // setBoolean(cell, "enabled", false);
          // add(row, cell);
          // setString(find("resNum"), "text", "0");
          return;
        }

        if (resNum > resCount) {
          // setBoolean(bsNext, "enabled", true);
        }
        // setString(find("resNum"), "text", String.valueOf(resNum));
        // putProperty(sTable, "resNum", new Integer(resNum));
        // putProperty(sTable, "query", q);
        // putProperty(sTable, "hc", collector);
        SearchTab.this.collector = collector;
        if (le != null) {
          // putProperty(sTable, "le", le);
        }
        resStart = 0;
        showSearchPage();
        // ai.setActive(false);
        // lukeWindow.setEnabled(true);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
        // lukeWindow.setEnabled(true);
      }
    };

    collectTask.execute(new TaskAdapter<Object>(taskListener));
  }

  /**
   * Create a Query instance that corresponds to values selected in the UI, such as analyzer class name and arguments, and default field.
   * 
   * @return
   */
  public Query createQuery(String queryString) throws Exception {
    // Object srchOpts = find("srchOptTabs");
    analyzer = createAnalyzer();
    Object srchOpts = null;
    String defField = getDefaultField(srchOpts);
    QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, defField, analyzer);
    // Object ckXmlParser = find(srchOpts, "ckXmlParser");
    // Object ckWild = find(srchOpts, "ckWild");
    // Object ckPosIncr = find(srchOpts, "ckPosIncr");
    // Object ckLoExp = find(srchOpts, "ckLoExp");
    // Object cbDateRes = find(srchOpts, "cbDateRes");
    // DateTools.Resolution resolution = Util.getResolution(getString(cbDateRes, "text"));
    // Object cbOp = find(srchOpts, "cbOp");
    // Object bqMaxCount = find(srchOpts, "bqMaxCount");
    int maxCount = 1024;
    try {
      // maxCount = Integer.parseInt(getString(bqMaxCount, "text"));
    } catch (Exception e) {
      e.printStackTrace();
      // TODO:
      // showStatus("Invalid BooleanQuery max clause count, using default 1024");
    }
    QueryParser.Operator op;
    BooleanQuery.setMaxClauseCount(maxCount);

    // qp.setAllowLeadingWildcard(getBoolean(ckWild, "selected"));
    // qp.setEnablePositionIncrements(getBoolean(ckPosIncr, "selected"));
    // qp.setLowercaseExpandedTerms(getBoolean(ckLoExp, "selected"));
    // qp.setDateResolution(resolution);
    // qp.setDefaultOperator(op);
    // if (getBoolean(ckXmlParser, "selected")) {
    //
    // CoreParser cp = createParser(defField,analyzer);
    // Query q = cp.parse(new ByteArrayInputStream(queryString.getBytes("UTF-8")));
    // return q;
    // } else {
    return qp.parse(queryString);
    // }
  }

  protected String getDefaultField(Object srchOptTabs) {
    String defField = null; // getString(find(srchOptTabs, "defFld"), "text");
    if (defField == null || defField.trim().equals("")) {
      if (lukeMediator.getIndexInfo().getReader() != null) {
        defField = lukeMediator.getIndexInfo().getFieldNames().get(0);
        // setString(find(srchOptTabs, "defFld"), "text", defField);
      } else {
        defField = "DEFAULT";
      }
    }
    return defField;
  }

  public Analyzer createAnalyzer() {
    Analyzer res = null;
    String sAType = (String) analyzerList.getSelectedItem();
    if (sAType.trim().equals("")) {
      sAType = "org.apache.lucene.analysis.standard.StandardAnalyzer";
      // setString(find("cbType"), "text", sAType);
      analyzerList.setSelectedItem(sAType);
    }
    String arg = null; // getString(find(srchOpts, "snoName"), "text");
    if (arg == null)
      arg = "";
    try {
      Constructor zeroArg = null, zeroArgV = null, oneArg = null, oneArgV = null;
      try {
        zeroArgV = Class.forName(sAType).getConstructor(new Class[] {Version.class});
      } catch (NoSuchMethodException e) {
        zeroArgV = null;
        try {
          zeroArg = Class.forName(sAType).getConstructor(new Class[0]);
        } catch (NoSuchMethodException e1) {
          zeroArg = null;
        }
      }
      try {
        oneArgV = Class.forName(sAType).getConstructor(new Class[] {Version.class, String.class});
      } catch (NoSuchMethodException e) {
        oneArgV = null;
        try {
          oneArg = Class.forName(sAType).getConstructor(new Class[] {String.class});
        } catch (NoSuchMethodException e1) {
          oneArg = null;
        }
      }
      if (arg.length() == 0) {
        if (zeroArgV != null) {
          res = (Analyzer) zeroArgV.newInstance(Version.LUCENE_CURRENT);
        } else if (zeroArg != null) {
          res = (Analyzer) zeroArg.newInstance();
        } else if (oneArgV != null) {
          res = (Analyzer) oneArgV.newInstance(new Object[] {Version.LUCENE_CURRENT, arg});
        } else if (oneArg != null) {
          res = (Analyzer) oneArg.newInstance(new Object[] {arg});
        } else {
          throw new Exception("Must have a zero-arg or (Version) or (Version, String) constructor");
        }
      } else {
        if (oneArgV != null) {
          res = (Analyzer) oneArgV.newInstance(new Object[] {Version.LUCENE_CURRENT, arg});
        } else if (oneArg != null) {
          res = (Analyzer) oneArg.newInstance(new Object[] {arg});
        } else if (zeroArgV != null) {
          res = (Analyzer) zeroArgV.newInstance(new Object[] {Version.LUCENE_CURRENT});
        } else if (zeroArg != null) {
          res = (Analyzer) zeroArg.newInstance(new Object[0]);
        } else {
          throw new Exception("Must have a zero-arg or (String) constructor");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      errorMsg("Analyzer '" + sAType + "' error: " + e.getMessage() + ". Using StandardAnalyzer.");
      res = stdAnalyzer;
    }
    Prefs.setProperty(Prefs.P_ANALYZER, res.getClass().getName());
    return res;
  }

  public DefaultSimilarity createSimilarity() {
    // Object ckSimDef = find(srchOpts, "ckSimDef");
    // Object ckSimSweet = find(srchOpts, "ckSimSweet");
    // Object ckSimOther = find(srchOpts, "ckSimOther");
    // Object simClass = find(srchOpts, "simClass");
    // Object ckSimCust = find(srchOpts, "ckSimCust");
    // if (getBoolean(ckSimDef, "selected")) {
    // return new DefaultSimilarity();
    // } else if (getBoolean(ckSimSweet, "selected")) {
    // return new SweetSpotSimilarity();
    // } else if (getBoolean(ckSimOther, "selected")) {
    // try {
    // Class clazz = Class.forName(getString(simClass, "text"));
    // if (Similarity.class.isAssignableFrom(clazz)) {
    // Similarity sim = (Similarity)clazz.newInstance();
    // return sim;
    // } else {
    // throw new Exception("Not a subclass of Similarity: " + clazz.getName());
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // showStatus("ERROR: invalid Similarity, using default");
    // setBoolean(ckSimDef, "selected", true);
    // setBoolean(ckSimOther, "selected", false);
    // return new DefaultSimilarity();
    // }
    // } else if (getBoolean(ckSimCust, "selected")) {
    // return similarity;
    // } else {
    return new DefaultSimilarity();
    // }
  }

  public AccessibleHitCollector createCollector() throws Exception {
    boolean orderRes = false;
    boolean scoreRes = false;
    // Object ckNormRes = find(srchOpts, "ckNormRes");
    // Object ckAllRes = find(srchOpts, "ckAllRes");
    // Object ckLimRes = find(srchOpts, "ckLimRes");
    // Object ckLimTime = find(srchOpts, "ckLimTime");
    // Object limTime = find(srchOpts, "limTime");
    // Object ckLimCount = find(srchOpts, "ckLimCount");
    // Object limCount = find(srchOpts, "limCount");
    // Object ckScoreRes = find(srchOpts, "ckScoreRes");
    // Object ckOrderRes = find(srchOpts, "ckOrderRes");
    // boolean scoreRes = getBoolean(ckScoreRes, "selected");
    // boolean orderRes = getBoolean(ckOrderRes, "selected");
    // Collector hc = null;
    // if (getBoolean(ckNormRes, "selected")) {
    return new AccessibleTopHitCollector(1000, orderRes, scoreRes);
    // } else if (getBoolean(ckAllRes, "selected")) {
    // return new AllHitsCollector(orderRes, scoreRes);
    // } else if (getBoolean(ckLimRes, "selected")) {
    // // figure out the type
    // if (getBoolean(ckLimCount, "selected")) {
    // int lim = Integer.parseInt(getString(limCount, "text"));
    // return new CountLimitedHitCollector(lim, orderRes, scoreRes);
    // } else if (getBoolean(ckLimTime, "selected")) {
    // int lim = Integer.parseInt(getString(limTime, "text"));
    // return new IntervalLimitedCollector(lim, orderRes, scoreRes);
    // } else {
    // throw new Exception("Unknown LimitedHitCollector type");
    // }
    // } else {
    // throw new Exception("Unknown HitCollector type");
    // }
  }

  private void showSearchPage() {
    Task<Object> buildTableTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        try {

          int resNum = collector.getTotalHits();
          int max = Math.min(resNum, resStart + resCount);
          // Object posLabel = find("resPos");
          // setString(posLabel, "text", resStart + "-" + (max - 1));
          List<String> fields = indexInfo.getFieldNames();
          for (int i = 0; i < fields.size(); i++) {
            String fieldName = fields.get(i);
            searchResultsTable.getColumns().add(new Column(fieldName, fieldName));
          }
          ArrayList<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
          for (int i = resStart; i < max; i++) {
            int docid = collector.getDocId(i);
            float score = collector.getScore(i);
            createResultRow(i, docid, score, tableData);
          }
          searchResultsTable.setTableData(tableData);
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
        }
        // ai.setActive(false);
        // lukeWindow.setEnabled(true);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
        // lukeWindow.setEnabled(true);
      }
    };

    buildTableTask.execute(new TaskAdapter<Object>(taskListener));

  }

  private void createResultRow(int pos, int docId, float score, ArrayList<Map<String,String>> tableData) throws IOException {
    Map<String,String> row = new HashMap<String,String>();

    tableData.add(row);
    row.put("num", String.valueOf(pos));

    row.put("score", String.valueOf(df.format(score)));

    row.put("docId", String.valueOf(docId));

    Document doc = indexInfo.getReader().document(docId);
    // putProperty(row, "docid", new Integer(docId));
    List<String> fields = indexInfo.getFieldNames();
    StringBuilder vals = new StringBuilder();

    for (int j = 0; j < fields.size(); j++) {
      String fieldName = fields.get(j);
      Decoder dec = lukeMediator.getDecoders().get(fieldName);
      if (dec == null)
        dec = lukeMediator.getDefDecoder();
      IndexableField[] values = doc.getFields(fieldName);
      vals.setLength(0);
      boolean decodeErr = false;
      if (values != null)
        for (int k = 0; k < values.length; k++) {
          if (k > 0)
            vals.append(' ');
          String v;
          try {
            v = dec.decodeStored(fieldName, (Field) values[k]);
          } catch (Throwable e) {
            e.printStackTrace();
            v = values[k].stringValue();
            decodeErr = true;
          }
          vals.append(Util.escape(v));
        }
      row.put(fieldName, vals.toString());
      if (decodeErr) {
        // TODO:
        // setColor(cell, "foreground", Color.RED);
      }

    }
  }

  private void errorMsg(String error) {
    Alert.alert(MessageType.ERROR, error, lukeMediator.getLukeWindow());
  }
}
