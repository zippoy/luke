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

import org.apache.lucene.index.*;
import org.apache.lucene.luke.core.*;
import org.apache.lucene.luke.core.decoders.Decoder;
import org.apache.lucene.luke.ui.LukeWindow.LukeMediator;
import org.apache.lucene.luke.ui.FieldsTableRow;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.*;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.TableViewRowEditor;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Collections;


public class OverviewTab extends SplitPane implements Bindable {

  @BXML
  private Label iPath;
  @BXML
  private Label indexCodec;
  @BXML
  private Label dirImpl;
  @BXML
  private Label iMod;
  @BXML
  private Label iDocs;
  @BXML
  private Label iFields;
  @BXML
  private TableView fieldsTable;
  @BXML
  private Label iTerms;

  @BXML
  private TablePane.Row fieldsAiRow;

  @BXML
  private TablePane.Row iTermsRow;

  @BXML
  private Label iFormat;
  @BXML
  private Label iCaps;
  @BXML
  private Label iDelOpt;
  @BXML
  private Label iVer;
  @BXML
  private Label iCommit;
  @BXML
  private Label iUser;
  @BXML
  private Spinner nTerms;
  @BXML
  private TableView tTable;
  @BXML
  private TablePane.Row topTermsAiRow;

  private java.util.Map<String,FieldTermCount> termCounts = new java.util.HashMap<String,FieldTermCount>();
  private int numTerms = 0;

  private LukeMediator lukeMediator;
  private Resources resources;
  private IndexReader ir;
  private IndexInfo indexInfo;

  public OverviewTab() {
    Action.getNamedActions().put("topTerms", new Action() {
      @Override
      public void perform(Component component) {
        actionTopTerms(getNTerms());
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
    this.indexInfo = lukeMediator.getIndexInfo();
    this.ir = indexInfo.getReader();

    String indexPath = indexInfo.getIndexPath();
    try {
      // initSolrTypes();

      // lastST = find("lastST");
      // setBoolean(find("bReload"), "enabled", true);
      // setBoolean(find("bClose"), "enabled", true);
      // setBoolean(find("bCommit"), "enabled", true);
      // Object cbType = find("cbType");
      // populateAnalyzers(cbType);
      // Object pOver = find("pOver");

      String idxName;
      if (indexPath.length() > 40) {
        idxName = indexPath.substring(0, 10) + "..." + indexPath.substring(indexPath.length() - 27);
      } else {
        idxName = indexPath;
      }
      iPath.setText(indexPath + (lukeMediator.getIndexInfo().isReadOnly() ? " (Read-Only)" : ""));

      // happens lower in the commented out code - future note

      String implName = "N/A";
      Directory dir = lukeMediator.getIndexInfo().getDirectory();
      if (dir == null) {
        if (ir != null) {
          implName = "N/A (reader is " + ir.getClass().getName() + ")";
        }
      } else {
        implName = dir.getClass().getName();
      }
      dirImpl.setText(implName);

      // Label iFileSize = (Label) beanSerializer.get("iFileSize");
      // long totalFileSize = Util.calcTotalFileSize(pName, dir);
      // iFileSize.setText(Util.normalizeSize(totalFileSize) +
      // Util.normalizeUnit(totalFileSize));
      if (ir == null) {
        return;
      }
      // we need IndexReader from now on

      // TODO: Codecs are now per field...
      indexCodec.setText(lukeMediator.getIndexInfo().getIndexCodec().getName());

      // String modText = "N/A";
      // if (dir != null) {
      // modText = new Date(IndexReader.lastModified(dir))
      // .toString();
      // }
      // iMod.setText(modText);
      iMod.setText(indexInfo.getLastModified());

      String numdocs = String.valueOf(ir.numDocs());
      iDocs.setText(numdocs);

      // TODO
      // Label iDocs1 = (Label) beanSerializer.get("iDocs1");
      // iDocs1.setText(String.valueOf(ir.maxDoc() - 1));

      java.util.List<String> indexFields = indexInfo.getFieldNames();
      iFields.setText(String.valueOf(indexFields.size()));
      // if (fn.size() == 0) {
      // showStatus("Empty index.");
      // }

      // Label defFld = (Label) beanSerializer.get("defFld");
      // Label fCombo = (Label) beanSerializer.get("fCombo");

      iFields.setText(String.valueOf(indexFields.size()));

      final ActivityIndicator ai = new ActivityIndicator();
      ai.setStyles("{backgroundColor:11}");
      ai.setPreferredHeight(10);
      ai.setPreferredWidth(10);
      ai.setActive(true);

      fieldsAiRow.add(ai);

      iTermsRow.remove(iTerms);

      final ActivityIndicator iTermsAi = new ActivityIndicator();
      iTermsAi.setStyles("{backgroundColor:'#fcfdfd'}");
      iTermsAi.setPreferredHeight(10);
      iTermsAi.setPreferredWidth(10);

      iTermsAi.setActive(true);
      iTermsRow.add(iTermsAi);

      TaskListener<String> taskListener = new TaskListener<String>() {
        @Override
        public void taskExecuted(Task<String> task) {
          // setBoolean(cell, "enabled", false);
          // setString(cell, "text", "..wait..");
          fieldsTable.setTableData(new ArrayList(0));
          termCounts.clear();
          FieldTermCount ftc = null;
          try {

            numTerms = indexInfo.getNumTerms();
            termCounts = indexInfo.getFieldTermCounts();

            iTerms.setText(String.valueOf(numTerms));
            initFieldList(null, null);
          } catch (Exception e) {
            // showStatus("ERROR: can't count terms per field");
            numTerms = -1;
            termCounts = Collections.emptyMap();
          }

          ai.setActive(false);
          iTermsAi.setActive(false);
          fieldsAiRow.remove(ai);
          iTermsRow.remove(iTermsAi);
          iTermsRow.add(iTerms);
          // lukeWindow.setEnabled(true);
        }

        @Override
        public void executeFailed(Task<String> task) {
          ai.setActive(false);
          iTermsAi.setActive(true);
          // lukeWindow.setEnabled(true);
        }
      };
      Task fListTask = new Task() {

        @Override
        public Object execute() throws TaskExecutionException {
          return null;
        }
      };

      fListTask.execute(new TaskAdapter<String>(taskListener));
      clearFieldsTableStatus();

      String sDel = ir.hasDeletions() ? "Yes (" + ir.numDeletedDocs() + ")" : "No";
      IndexCommit ic = ir instanceof DirectoryReader ? ((DirectoryReader) ir).getIndexCommit() : null;
      String sOpt = ic != null ? (ic.getSegmentCount() == 1 ? "Yes" : "No") : "?";
      String sDelOpt = sDel + " / " + sOpt;
      iDelOpt.setText(sDelOpt);

      String verText = "N/A";
      if (ic != null) {
        verText = Long.toHexString(((DirectoryReader) ir).getVersion());
      }
      iVer.setText(verText);

      String formatText = "N/A";
      String formatCaps = "N/A";
      if (dir != null) {
        //int format = IndexGate.getIndexFormat(dir);
        //IndexGate.FormatDetails formatDetails = IndexGate.getFormatDetails(format);
        //formatText = format + " (" + formatDetails.getGenericName() + ")";
        //formatCaps = formatDetails.getCapabilities();
        //int format = indexInfo.getIndexFormat();
        IndexGate.FormatDetails formatDetails = indexInfo.getIndexFormat();
        formatText = formatDetails.version + " (" + formatDetails.genericName + ")";
        formatCaps = formatDetails.capabilities;
        if (formatCaps == null) {
          formatCaps = (String) resources.get("overviewTab_newerVersionWarning");
        }
      }
      iFormat.setText(formatText);
      iCaps.setText(formatCaps);

      String commitText = "N/A";
      try {
        commitText = ic.getSegmentsFileName() + " (generation=" + ic.getGeneration() + ", segs=" + ic.getSegmentCount() + ")";
      } catch (UnsupportedOperationException uoe) {}
      iCommit.setText(commitText);

      String userData = null;
      try {
        java.util.Map<String,String> userDataMap = ic.getUserData();
        if (userDataMap != null && !userDataMap.isEmpty()) {
          userData = ic.getUserData().toString();
        } else {
          userData = "--";
        }
      } catch (UnsupportedOperationException uoe) {
        userData = "(not supported)";
      }
      iUser.setText(userData);
      final int nTermsInt = getNTerms();

      actionTopTerms(nTermsInt);

    } catch (Exception e) {
      e.printStackTrace();
      errorMsg(e.getMessage());
    }
  }

  /**
   * Update the list of top terms.
   */
  public void actionTopTerms(int nTerms) {
    if (ir == null) {
      // showStatus(MSG_NOINDEX);
      return;
    }

    int nd = nTerms;

    final int ndoc = nd;

    Sequence<?> fields = fieldsTable.getSelectedRows();

    final java.util.Map<String, Decoder> fldDecMap = new java.util.HashMap<String, Decoder>();
    if (fields == null || fields.getLength() == 0) {
      // no fields selected
      for (String fld : indexInfo.getFieldNames()) {
        Decoder dec = lukeMediator.getDecoders().get(fld);
        if (dec == null) {
          dec = lukeMediator.getDefDecoder();
        }
        fldDecMap.put(fld, dec);
      }
    } else {
      // some fields selected
      for (int i = 0; i < fields.getLength(); i++) {
        String fld = ((FieldsTableRow)fields.get(i)).getName();
        Decoder dec = ((FieldsTableRow)fields.get(i)).getDecoder();
        fldDecMap.put(fld, dec);
      }
    }

    tTable.setTableData(new ArrayList(0));

    final ActivityIndicator ai = new ActivityIndicator();
    try {
      ai.setStyles("{backgroundColor:11}");
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
    ai.setPreferredHeight(10);
    ai.setPreferredWidth(10);
    ai.setHeight(10);
    ai.setWidth(10);

    topTermsAiRow.add(ai);
    ai.setActive(true);

    Task<Object> topTermsTask = new Task<Object>() {

      @Override
      public Object execute() throws TaskExecutionException {
        return null;
      }
    };

    TaskListener<Object> taskListener = new TaskListener<Object>() {
      @Override
      public void taskExecuted(Task<Object> task) {
        // this must happen here rather than in the task because it must happen in the UI dispatch thread
        try {
          final String[] fflds = fldDecMap.keySet().toArray(new String[0]);
          TermStats[] topTerms = HighFreqTerms.getHighFreqTerms(ir, ndoc, fflds);

          List<Map<String,String>> tableData = new ArrayList<Map<String,String>>();

          if (topTerms == null || topTerms.length == 0) {
            Map<String,String> row = new HashMap<String,String>();
            row.put("text", "No Results");
            tableData.add(row);
          }

          for (int i = 0; i < topTerms.length; i++) {
            Map<String,String> row = new HashMap<String,String>();

            // putProperty(row, "term", tis[i].term);
            // putProperty(row, "ti", tis[i]);

            row.put("num", String.valueOf(i + 1));

            row.put("df", String.valueOf(topTerms[i].docFreq) + "  ");

            row.put("field", topTerms[i].field);

            Decoder dec = fldDecMap.get(topTerms[i].field);

            String s;
            try {
              s = dec.decodeTerm(topTerms[i].field, topTerms[i].termtext);
            } catch (Throwable e) {
              // e.printStackTrace();
              s = topTerms[i].termtext.utf8ToString();
              // TODO
              // setColor(cell, "foreground", Color.RED);
            }
            row.put("text", s);
            // hidden field. would be used when the user select 'Browse term docs' menu at top terms table.
            row.put("rawterm", topTerms[i].termtext.utf8ToString());
            tableData.add(row);
          }
          tTable.setTableData(tableData);
          topTermsAiRow.remove(ai);
        } catch (Exception e) {
          e.printStackTrace();
          errorMsg(e.getMessage());
        }

        ai.setActive(false);
        // lukeWindow.setEnabled(true);
      }

      @Override
      public void executeFailed(Task<Object> task) {
        ai.setActive(false);
        // lukeWindow.setEnabled(true);
      }
    };

    topTermsTask.execute(new TaskAdapter<Object>(taskListener));

    addListenerToTopTermsTable();
  }

  private void addListenerToTopTermsTable() {
    // register mouse button listener for more options.
    tTable.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter(){
      @Override
      public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        final Map<String, String> row = (Map<String, String>) tTable.getSelectedRow();
        if (row == null) {
          System.out.println("No term selected.");
          return false;
        }
        if (button.name().equals(Mouse.Button.RIGHT.name())) {
          MenuPopup popup = new MenuPopup();
          Menu menu = new Menu();
          Menu.Section section1 = new Menu.Section();
          Menu.Section section2 = new Menu.Section();
          Menu.Item item1 = new Menu.Item(resources.get("overviewTab_topTermTable_popup_menu1"));
          item1.setAction(new Action() {
            @Override
            public void perform(Component component) {
              // 'Browse term docs' menu selected. switch to Documents tab.
              Term term = new Term(row.get("field"), new BytesRef(row.get("rawterm")));
              lukeMediator.getDocumentsTab().showTerm(term);
              // TODO: index access isn't good...
              lukeMediator.getTabPane().setSelectedIndex(1);
            }
          });
          Menu.Item item2 = new Menu.Item(resources.get("overviewTab_topTermTable_popup_menu2"));
          item2.setAction(new Action() {
            @Override
            public void perform(Component component) {
              // 'Show all term docs' menu selected. switch to Search tab.
              // TODO
            }
          });
          Menu.Item item3 = new Menu.Item(resources.get("overviewTab_topTermTable_popup_menu3"));
          item3.setAction(new Action() {
            @Override
            public void perform(Component component) {
              // 'Copy to clipboard' menu selected.
              StringBuilder sb = new StringBuilder();
              sb.append(row.get("num") + "\t");
              sb.append(row.get("df") + "\t");
              sb.append(row.get("field") + "\t");
              sb.append(row.get("text") + "\t");
              LocalManifest content = new LocalManifest();
              content.putText(sb.toString());
              Clipboard.setContent(content);
            }
          });
          section1.add(item1);
          section1.add(item2);
          section2.add(item3);
          menu.getSections().add(section1);
          menu.getSections().add(section2);
          popup.setMenu(menu);

          popup.open(getWindow(), getMouseLocation().x + 20, getMouseLocation().y);
          return true;
        }
        return false;
      }
    });
  }

  private void initFieldList(Object fCombo, Object defFld) {
    // removeAll(fieldsTable);
    // removeAll(defFld);
    // setString(fCombo, "text", idxFields[0]);
    // setString(defFld, "text", idxFields[0]);
    NumberFormat intCountFormat = NumberFormat.getIntegerInstance();
    NumberFormat percentFormat = NumberFormat.getNumberInstance();
    intCountFormat.setGroupingUsed(true);
    percentFormat.setMaximumFractionDigits(2);
    // sort listener
    fieldsTable.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
      @Override
      public void sortChanged(TableView tableView) {
        @SuppressWarnings("unchecked")
        List<FieldsTableRow> tableData = (List<FieldsTableRow>) tableView.getTableData();
        tableData.setComparator(new TableComparator(tableView));
      }
    });
    // default sort : sorted by name in ascending order
    fieldsTable.setSort("name", SortDirection.ASCENDING);

    // row editor for decoders
    List decoders = new ArrayList();
    for (Decoder dec : Util.loadDecoders()) {
      decoders.add(dec);
    }
    ListButton decodersButton = new ListButton(decoders);
    decodersButton.setSelectedItemKey("decoder");
    TableViewRowEditor rowEditor = new TableViewRowEditor();
    rowEditor.getCellEditors().put("decoder", decodersButton);
    fieldsTable.setRowEditor(rowEditor);


    for (String fname : indexInfo.getFieldNames()) {
      FieldsTableRow row = new FieldsTableRow(lukeMediator);
      row.setName(fname);
      FieldTermCount ftc = termCounts.get(fname);
      if (ftc != null) {
        long cnt = ftc.termCount;
        row.setTermCount(intCountFormat.format(cnt));
        float pcent = (float) (cnt * 100) / (float) numTerms;
        row.setPercent(percentFormat.format(pcent) + " %");
      } else {
        row.setTermCount("0");
        row.setPercent("0.00%");
      }

      List<FieldsTableRow> tableData = (List<FieldsTableRow>)fieldsTable.getTableData();
      tableData.add(row);

      Decoder dec = lukeMediator.getDecoders().get(fname);
      if (dec == null)
        dec = lukeMediator.getDefDecoder();
      row.setDecoder(dec);

      // populate combos
      // Object choice = create("choice");
      // add(fCombo, choice);
      // setString(choice, "text", s);
      // putProperty(choice, "fName", s);
      // choice = create("choice");
      // add(defFld, choice);
      // setString(choice, "text", s);
      // putProperty(choice, "fName", s);
    }

  }

  private void clearFieldsTableStatus() {
    // clear the fields table view status
    fieldsTable.clearSelection();
  }

  private int getNTerms() {
    final int nTermsInt = nTerms.getSelectedIndex();
    return nTermsInt;
  }

  private void errorMsg(String error) {
    Alert.alert(MessageType.ERROR, error, getWindow());
  }
}
