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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.index.TermsEnum.SeekStatus;
import org.apache.lucene.luke.core.IndexInfo;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.luke.core.decoders.Decoder;
import org.apache.lucene.luke.ui.LukeWindow.LukeMediator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;

import java.io.IOException;
import java.net.URL;

public class DocumentsTab extends TablePane implements Bindable {

  private int iNum;
  @BXML
  private TableView docTable;
  @BXML
  private TextInput docNum;
  @BXML
  private ListButton fieldsList;
  @BXML
  private TextInput termText;
  @BXML
  private Label dFreq;
  @BXML
  private Label tdMax;
  @BXML
  private Label maxDocs;
  @BXML
  private Label tFreq;
  @BXML
  private Label tdNum;
  @BXML
  private Label docNum2;
  @BXML
  private TextArea decText;

  private java.util.List<String> fieldNames = null;

  // this gets injected by LukeWindow at init
  private LukeMediator lukeMediator;

  private Resources resources;

  private TermsEnum te;
  private DocsAndPositionsEnum td;

  private String fld;
  private Term lastTerm;
  private IndexReader ir;
  private AtomicReader ar;
  private java.util.List<String> indexFields;
  private IndexInfo idxInfo;
  private FieldInfos infos;

  public DocumentsTab() {

    Action.getNamedActions().put("nextDoc", new Action() {
      @Override
      public void perform(Component component) {
        nextDoc();
      }
    });
    Action.getNamedActions().put("prevDoc", new Action() {
      @Override
      public void perform(Component component) {
        prevDoc();
      }
    });
    Action.getNamedActions().put("showFirstTermDoc", new Action() {
      @Override
      public void perform(Component component) {
        showFirstTermDoc();
      }
    });
    Action.getNamedActions().put("deleteTermDoc", new Action() {
      @Override
      public void perform(Component component) {

      }
    });
    Action.getNamedActions().put("showFirstTerm", new Action() {
      @Override
      public void perform(Component component) {
        showFirstTerm();
      }
    });
    Action.getNamedActions().put("showTerm", new Action() {
      @Override
      public void perform(Component component) {
        prevDoc();
      }
    });
    Action.getNamedActions().put("showNextTerm", new Action() {
      @Override
      public void perform(Component component) {
        showNextTerm();
      }
    });
    Action.getNamedActions().put("showAllTermDoc", new Action() {
      @Override
      public void perform(Component component) {

      }
    });
    Action.getNamedActions().put("showNextTermDoc", new Action() {
      @Override
      public void perform(Component component) {
        showNextTermDoc();
      }
    });
    Action.getNamedActions().put("showPositions", new Action() {
      @Override
      public void perform(Component component) {

      }
    });

  }

  @Override
  public void initialize(Map<String,Object> namespace, URL location, Resources resources) {
    this.resources = resources;
  }

  public void initLukeMediator(LukeMediator lukeMediator) {
    this.lukeMediator = lukeMediator;
  }

  /**
   * Initialize GUI elements. This method is called when a new index is opened.
   * 
   * @throws Exception
   */
  public void onOpenIndex() throws Exception {
    this.idxInfo = lukeMediator.getIndexInfo();
    this.ir = idxInfo.getReader();
    if (ir instanceof CompositeReader) {
      //ar = new SlowCompositeReaderWrapper((CompositeReader) ir);
      ar = SlowCompositeReaderWrapper.wrap(ir);
    } else if (ir instanceof AtomicReader) {
      ar = (AtomicReader) ir;
    }

    if (ar != null) {
      infos = ar.getFieldInfos();
    }

    this.indexFields = idxInfo.getFieldNames();

    List<String> fields = new ArrayList<String>(indexFields.size());
    for (String s : indexFields) {
      fields.add(s);
    }
    fieldsList.setListData(fields);

    fieldNames = idxInfo.getFieldNames();
    if (fieldNames.size() == 0) {
      // TODO:
      // showStatus("Empty index.");
    } else {
      fieldsList.setSelectedIndex(0);
    }
    maxDocs.setText(String.valueOf(ir.maxDoc() - 1));
  }

  private void showDoc(int incr) {
    if (ir == null) {
      // showStatus(resources.get("documentsTab_noOrClosedIndex"));
      // TODO:
      System.out.println(resources.get("documentsTab_noOrClosedIndex"));
      return;
    }

    String num = docNum.getText();
    if (num.trim().equals(""))
      num = String.valueOf(-incr);
    try {
      iNum = Integer.parseInt(num);
      iNum += incr;
      if (iNum < 0 || iNum >= ir.maxDoc()) {
        System.out.println(resources.get("documentsTab_docNumOutsideRange"));
        // TODO:
        // showStatus(resources.get("documentsTab_docNumOutsideRange"));
        return;
      }
      docNum.setText(String.valueOf(iNum));

      org.apache.lucene.util.Bits live = ar.getLiveDocs();
      if (live == null || live.get(iNum)) {
        Task<Object> populateTableTask = new Task<Object>() {

          @Override
          public Object execute() throws TaskExecutionException {
            return null;
          }
        };
        TaskListener<Object> tl = new TaskListener<Object>() {
          @Override
          public void taskExecuted(Task<Object> task) {
            try {
              Document doc = ir.document(iNum);
              popTableWithDoc(iNum, doc);
            } catch (Exception e) {
              e.printStackTrace();
              // TODO:
              // showStatus(e.getMessage());
            }
          }

          @Override
          public void executeFailed(Task<Object> task) {

          }
        };
        populateTableTask.execute(new TaskAdapter<Object>(tl));

      } else {

        Task<Object> populateTableTask = new Task<Object>() {

          @Override
          public Object execute() throws TaskExecutionException {
            return null;
          }
        };
        TaskListener<Object> tl = new TaskListener<Object>() {
          @Override
          public void taskExecuted(Task<Object> task) {

            // TODO:
            // showStatus("Deleted document - not available.");
            popTableWithDoc(iNum, null);
          }

          @Override
          public void executeFailed(Task<Object> task) {

          }
        };
        populateTableTask.execute(new TaskAdapter<Object>(tl));

      }
    } catch (Exception e) {
      e.printStackTrace();
      // TODO:
      // showStatus(e.getMessage());
    }
  }

  private void nextDoc() {
    showDoc(1);
  }

  private void prevDoc() {
    showDoc(-1);
  }

  public void popTableWithDoc(int docid, Document doc) {
    docNum.setText(String.valueOf(docid));
    List<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
    docTable.setTableData(tableData);

    // putProperty(table, "doc", doc);
    // putProperty(table, "docNum", new Integer(docid));
    if (doc == null) {
      docNum2.setText(String.valueOf(docid) + "  DELETED");
      return;
    }

    docNum2.setText(String.valueOf(docid));
    for (int i = 0; i < indexFields.size(); i++) {
      Map<String,String> row = new HashMap<String,String>();

      IndexableField[] fields = doc.getFields(indexFields.get(i));
      if (fields == null) {
        tableData.add(row);
        addFieldRow(row, indexFields.get(i), null, docid);
        continue;
      }
      for (int j = 0; j < fields.length; j++) {
        // if (fields[j].isBinary()) {
        // System.out.println("f.len=" + fields[j].getBinaryLength() +
        // ", doc.len=" + doc.getBinaryValue(indexFields[i]).length);
        // }
        tableData.add(row);
        addFieldRow(row, indexFields.get(i), fields[j], docid);
      }
    }
  }

  private void addFieldRow(Map<String,String> row, String fName, IndexableField field, int docid) {
    java.util.Map<String,Decoder> decoders = lukeMediator.getDecoders();
    Decoder defDecoder = lukeMediator.getDefDecoder();

    Field f = (Field) field;
    // putProperty(row, "field", f);
    // putProperty(row, "fName", fName);

    row.put("field", fName);
    row.put("itsvopfolb", Util.fieldFlags(f, infos.fieldInfo(fName)));

    // if (f == null) {
    // setBoolean(cell, "enabled", false);
    // }

    if (f != null) {
      try {
        FieldInfo info = infos.fieldInfo(fName);
        if (info.hasNorms()) {
          NumericDocValues norms = ar.getNormValues(fName);
          String val = Long.toString(norms.get(docid));
          row.put("norm", String.valueOf(norms.get(docid)));
        } else {
          row.put("norm", "---");
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
        row.put("norm", "!?!");
      }
    } else {
      row.put("norm", "---");
      // setBoolean(cell, "enabled", false);
    }

    if (f != null) {
      String text = f.stringValue();
      if (text == null) {
        if (f.binaryValue() != null) {
          text = Util.bytesToHex(f.binaryValue(), false);
        } else {
          text = "(null)";
        }
      }
      Decoder dec = decoders.get(f.name());
      if (dec == null)
        dec = defDecoder;
      try {
        if (f.fieldType().stored()) {
          text = dec.decodeStored(f.name(), f);
        } else {
          text = dec.decodeTerm(f.name(), text);
        }
      } catch (Throwable e) {
        // TODO:
        // setColor(cell, "foreground", Color.RED);
      }
      row.put("value", Util.escape(text));
    } else {
      row.put("value", "<not present or not stored>");
      // setBoolean(cell, "enabled", false);
    }
  }

  public void showFirstTerm() {
    if (ir == null) {
      // TODO:
      // showStatus(MSG_NOINDEX);
      return;
    }
    Task<Object> showFirstTermTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        try {

          fld = (String) fieldsList.getSelectedItem();
          System.out.println("fld:" + fld);
          Terms terms = MultiFields.getTerms(ir, fld);
          te = terms.iterator(null);
          BytesRef term = te.next();
          showTerm(new Term(fld, term));
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
        }
        // ai.setActive(false);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
      }
    };

    showFirstTermTask.execute(new TaskAdapter<Object>(taskListener));

  }

  public void showFirstTermDoc() {

    if (lastTerm == null)
      return;
    if (ir == null) {
      // TODO:
      // showStatus(resources.get("documentsTab_noOrClosedIndex"));
      return;
    }
    Task<Object> showFirstTermDocTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        try {
          DocsAndPositionsEnum td = MultiFields.getTermPositionsEnum(ir, null, lastTerm.field(), lastTerm.bytes());
          td.nextDoc();
          tdNum.setText("1");
          DocumentsTab.this.td = td;
          showTermDoc();
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
        }
        // ai.setActive(false);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
      }
    };

    showFirstTermDocTask.execute(new TaskAdapter<Object>(taskListener));

  }

  public void showNextTermDoc() {
    if (lastTerm == null)
      return;
    if (ir == null) {
      // TODO:
      // showStatus(MSG_NOINDEX);
      return;
    }
    Task<Object> showNextTermDocTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        try {
          if (td == null) {
            showFirstTermDoc();
            return;
          }
          if (td.nextDoc() == DocsEnum.NO_MORE_DOCS)
            return;

          String sCnt = tdNum.getText();
          int cnt = 1;
          try {
            cnt = Integer.parseInt(sCnt);
          } catch (Exception e) {}

          tdNum.setText(String.valueOf(cnt + 1));

          showTermDoc();
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
        }
        // ai.setActive(false);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
      }
    };

    showNextTermDocTask.execute(new TaskAdapter<Object>(taskListener));

  }

  private void showTerm(final Term t) {
    if (t == null) {
      // TODO:
      // showStatus("No terms?!");
      return;
    }
    if (ir == null) {
      // TODO:
      // showStatus(resources.get("documentsTab_noOrClosedIndex"));
      return;
    }

    lastTerm = t;

    fieldsList.setSelectedItem(t.field());
    Decoder dec = lukeMediator.getDecoders().get(t.field());

    if (dec == null)
      dec = lukeMediator.getDefDecoder();
    String s = null;
    boolean decodeErr = false;
    try {
      s = dec.decodeTerm(t.field(), t.text());
    } catch (Throwable e) {
      s = e.getMessage();
      decodeErr = true;
    }

    termText.setText(t.text());

    if (!s.equals(t.text())) {
      decText.setText(s);

      if (decodeErr) {
        // setColor(rawText, "foreground", Color.RED);
      } else {
        // setColor(rawText, "foreground", Color.BLUE);
      }
    } else {
      decText.setText("");
      // setColor(rawText, "foreground", Color.BLACK);
    }

    lastTerm = t;
    // putProperty(fText, "decText", s);

    td = null;
    tdNum.setText("?");
    tFreq.setText("?");

    Task<Object> updateDfTdTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {

        try {
          int freq = ir.docFreq(t);
          dFreq.setText(String.valueOf(freq));

          tdMax.setText(String.valueOf(freq));
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
          dFreq.setText("?");
        }
        // ai.setActive(false);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
      }
    };

    updateDfTdTask.execute(new TaskAdapter<Object>(taskListener));

  }

  public void showNextTerm() {
    if (ir == null) {
      // TODO:
      // showStatus(resources.get("documentsTab_noOrClosedIndex"));
      return;
    }
    Task<Object> showNextTermTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        try {
          String text;
          text = termText.getText();
          if (text == null || text.trim().length() == 0) {
            showFirstTerm();
            return;
          }

          String fld = (String) fieldsList.getSelectedItem();

          SeekStatus status;
          BytesRef rawTerm = null;
          if (te != null) {
            rawTerm = te.term();
          }
          String rawString = rawTerm != null ? rawTerm.utf8ToString() : null;

          if (te == null || !DocumentsTab.this.fld.equals(fld) || !text.equals(rawString)) {
            Terms terms = MultiFields.getTerms(ir, fld);
            te = terms.iterator(null);

            DocumentsTab.this.fld = fld;
            status = te.seekCeil(new BytesRef(text));
            if (status.equals(SeekStatus.FOUND)) {
              rawTerm = te.term();
            } else {
              rawTerm = null;
            }
          } else {
            rawTerm = te.next();
          }
          if (rawTerm == null) { // proceed to next field
            int idx = fieldNames.indexOf(fld);
            while (idx < fieldNames.size() - 1) {
              idx++;
              fieldsList.setSelectedIndex(idx);
              fld = fieldNames.get(idx);
              Terms terms = MultiFields.getTerms(ir, fld);
              if (terms == null) {
                continue;
              }
              te = terms.iterator(null);
              rawTerm = te.next();
              DocumentsTab.this.fld = fld;
              break;
            }
          }
          if (rawTerm == null) {
            // TODO:
            // showStatus("No more terms");
            return;
          }

          showTerm(new Term(fld, rawTerm));
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
        }
        // ai.setActive(false);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
      }
    };

    showNextTermTask.execute(new TaskAdapter<Object>(taskListener));

  }

  private void showTermDoc() {
    if (ir == null) {
      // TODO:
      // showStatus(resources.get("documentsTab_noOrClosedIndex"));
      return;
    }
    Task<Object> showTermDocTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        try {
          Document doc = ir.document(td.docID());
          docNum.setText(String.valueOf(td.docID()));

          tFreq.setText(String.valueOf(td.freq()));

          popTableWithDoc(td.docID(), doc);
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // showStatus(e.getMessage());
        }

        // ai.setActive(false);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        // ai.setActive(false);
      }
    };

    showTermDocTask.execute(new TaskAdapter<Object>(taskListener));

  }

  public void showAllTermDoc() {
    final IndexReader ir = lukeMediator.getIndexInfo().getReader();
    if (ir == null) {
      // TODO:
      // showStatus("MSG_NOINDEX");
      return;
    }
    // Object tabpane = find("maintpane");
    // setInteger(tabpane, "selected", 2);
    // Object qField = find("qField");
    // setString(qField, "text", t.field() + ":" + t.text());
    // Object qFieldParsed = find("qFieldParsed");
    // Object ckScoreRes = find("ckScoreRes");
    // Object ckOrderRes = find("ckOrderRes");
    // Object cntRepeat = find("cntRepeat");
    // final boolean scoreRes = getBoolean(ckScoreRes, "selected");
    // final boolean orderRes = getBoolean(ckOrderRes, "selected");
    // final int repeat = Integer.parseInt(getString(cntRepeat, "text"));
    final Query q = new TermQuery(lastTerm);
    // parsedQueryLabel.setText(q.toString());
    Task<Object> showAllTermDocTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        IndexSearcher is = null;
        try {
          is = new IndexSearcher(ir);
          // Object sTable = find("sTable");
          // removeAll(sTable);
          // AllHitsCollector ahc = new AllHitsCollector(orderRes, scoreRes);
          // _search(q, is, ahc, sTable, repeat);
        } catch (Exception e) {
          e.printStackTrace();
          // TODO:
          // errorMsg(e.getMessage());
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

    showAllTermDocTask.execute(new TaskAdapter<Object>(taskListener));

  }

}
