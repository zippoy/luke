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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexGate;
import org.apache.lucene.luke.core.IndexInfo;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.luke.ui.LukeWindow.LukeMediator;
import org.apache.lucene.store.Directory;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.TableView;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class FilesTab extends SplitPane implements Bindable {
  @BXML
  private TableView filesTable;
  @BXML
  private TableView commitsTable;
  private LukeMediator lukeMediator;
  
  @Override
  public void initialize(Map<String,Object> namespace, URL location,
      Resources resources) {
    
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
    IndexInfo indexInfo = lukeMediator.getIndexInfo();
    showFiles(indexInfo.getIndexPath(), indexInfo.getDirectory(), null);
    showCommits(indexInfo.getDirectory());
  }
  
  public void showFiles(String indexPath, Directory dir, java.util.List<? extends IndexCommit> commits)
      throws Exception {
    if (dir == null) {
      // TODO
      System.out.println("show files:  no dir");
      // removeAll(filesTable);
      
      // setString(cell, "text", "<not available>");
      // setBoolean(cell, "enabled", false);
      
      return;
    }
    String[] physFiles = dir.listAll();
    java.util.List<String> files = new java.util.ArrayList<String>();
    if (commits != null && commits.size() > 0) {
      for (int i = 0; i < commits.size(); i++) {
        IndexCommit commit = (IndexCommit) commits.get(i);
        files.addAll(commit.getFileNames());
      }
    } else {
      files.addAll(Arrays.asList(physFiles));
    }
    Collections.sort(files);
    java.util.List<String> segs = getIndexFileNames(dir);
    java.util.List<String> dels = getIndexDeletableNames(dir);
    
    // removeAll(filesTable);
    List<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
    for (int i = 0; i < files.size(); i++) {
      String fileName = files.get(i);
      String pathName;
      if (indexPath.endsWith(File.separator)) {
        pathName = indexPath;
      } else {
        pathName = indexPath + File.separator;
      }
      File file = new File(pathName + fileName);
      Map<String,String> row = new HashMap<String,String>();
      
      row.put("filename", fileName);
      
      row.put("size", Util.normalizeSize(file.length()));
      row.put("unit", Util.normalizeUnit(file.length()));
      
      boolean deletable = dels.contains(fileName.intern());
      
      row.put("deletable", deletable ? "YES" : "-");
      String inuse = getFileFunction(fileName);
      
      row.put("inuse", inuse);
      tableData.add(row);
    }
    filesTable.setTableData(tableData);
  }
  
  public void showCommits(Directory dir) throws Exception {
    // removeAll(commitsTable);
    if (dir == null) {
      // TODO
      System.out.println("not available");
      return;
    }
    java.util.List<IndexCommit> commits = DirectoryReader.listCommits(dir);
    // commits are ordered from oldest to newest ?
    Iterator<IndexCommit> it = commits.iterator();
    int rowNum = 0;
    List<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
    while (it.hasNext()) {
      Map<String,String> row = new HashMap<String,String>();
      IndexCommit commit = it.next();
      // figure out the name of the segment files
      Collection<String> files = commit.getFileNames();
      Iterator<String> itf = files.iterator();
      
      boolean enabled = rowNum < commits.size() - 1;
      Color color = null;
      rowNum++;
      
      // putProperty(row, "commit", commit);
      // if (enabled) {
      // putProperty(row, "commitDeletable", Boolean.TRUE);
      // }
      row.put("gen", String.valueOf(commit.getGeneration()));
      
      char[] flags = new char[] {'-', '-'};
      if (commit.isDeleted()) flags[0] = 'D';
      
      // TODO: show segment count instead?
      //if (commit.isOptimized()) flags[1] = 'O';
      row.put("flags", new String(flags));
      
      java.util.Map<String,String> userData = commit.getUserData();
      if (userData != null && !userData.isEmpty()) {
        row.put("userdata", userData.toString());
      } else {
        row.put("userdata", "--");
      }
      tableData.add(row);
    }
    commitsTable.setTableData(tableData);
  }
  
  public java.util.List<String> getIndexDeletableNames(Directory d) {
    if (d == null) return null;
    java.util.List<String> deletable = null;
    try {
      deletable = IndexGate.getDeletableFiles(d);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // for (int i = 0; i < deletable.size(); i++) {
    // System.out.println(" -del " + deletable.get(i));
    // }
    if (deletable == null) deletable = Collections.emptyList();
    return deletable;
  }
  
  public java.util.List<String> getIndexFileNames(Directory d) {
    if (d == null) return null;
    java.util.List<String> names = null;
    try {
      names = IndexGate.getIndexFiles(d);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // for (int i = 0; i < names.size(); i++) {
    // System.out.println(" -seg " + names.get(i));
    // }
    return names;
  }
  
  private String getFileFunction(String file) {
    String res = IndexGate.getFileFunction(file);
    if (res == null) {
      res = "YES";
    }
    return res;
  }
  
}
