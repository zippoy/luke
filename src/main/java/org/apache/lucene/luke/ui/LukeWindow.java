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
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.luke.core.ClassFinder;
import org.apache.lucene.luke.core.IndexInfo;
import org.apache.lucene.luke.core.decoders.Decoder;
import org.apache.lucene.luke.core.decoders.StringDecoder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Mouse.Button;
import org.apache.pivot.wtk.TablePane.Row;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashSet;

public class LukeWindow extends Frame implements Bindable {

  // private static final String MSG_READONLY = "FAILED: Read-Only index.";
  // private static final String MSG_CONV_ERROR =
  // "Some values could not be properly represented in this format. "
  // + "They are marked in grey and presented as a hex dump.";

  @BXML
  private LukeInitWindow lukeInitWindow;
  @BXML
  private TabPane tabPane;
  @BXML
  private FilesTab filesTab;
  @BXML
  private DocumentsTab documentsTab;
  @BXML
  private SearchTab searchTab;
  @BXML
  private OverviewTab overviewTab;
  @BXML
  private AnalyzersTab analyzersTab;
  @BXML
  private Label indexName;

  private LukeMediator lukeMediator = new LukeMediator();

  private Class[] analyzers = null;
  private Class[] defaultAnalyzers = {SimpleAnalyzer.class, StandardAnalyzer.class, StopAnalyzer.class, WhitespaceAnalyzer.class};

  private String baseDir;
  private String[] analyzerNames;

  /**
   * Set of themes given as hexadecimal color codes
   */
  private final int[][] themes = { {0xece9d0, 0x000000, 0xf5f4f0, 0x919b9a, 0xb0b0b0, 0xeeeeee, 0xb9b9b9, 0xff8080, 0xc5c5dd}, // default
      {0xe6e6e6, 0x000000, 0xffffff, 0x909090, 0xb0b0b0, 0xededed, 0xb9b9b9, 0x89899a, 0xc5c5dd}, // gray
      {0xeeeecc, 0x000000, 0xffffff, 0x999966, 0xb0b096, 0xededcb, 0xcccc99, 0xcc6600, 0xffcc66}, // sandstone
      {0xf0f0ff, 0x0000a0, 0xffffff, 0x8080ff, 0xb0b0b0, 0xededed, 0xb0b0ff, 0xff0000, 0xfde0e0}, // sky
      {0x6375d6, 0xffffff, 0x7f8fdd, 0xd6dff5, 0x9caae5, 0x666666, 0x003399, 0xff3333, 0x666666} // navy
  };

  /**
   * Listener for coloring buttons when pressed
   */
  ComponentMouseButtonListener mouseButtonPressedListener = new ComponentMouseButtonListener() {

    int theme[];

    public boolean mouseUp(Component component, Button button, int x, int y) {
      theme = themes[getActiveThemeIndex(Window.getActiveWindow())];
      setComponentColor(component, "backgroundColor", theme[0]);
      return false;
    }

    public boolean mouseDown(Component component, Button button, int x, int y) {
      theme = themes[getActiveThemeIndex(Window.getActiveWindow())];
      setComponentColor(component, "backgroundColor", theme[6]);
      return false;
    }

    public boolean mouseClick(Component component, Button button, int x, int y, int count) {
      return false;
    }

  };
  /**
   * Listener for coloring buttons when MouseOver event occurs
   */
  ComponentMouseListener mouseMoveListener = new ComponentMouseListener() {
    int theme[];

    public void mouseOver(Component component) {
      theme = themes[getActiveThemeIndex(Window.getActiveWindow())];
      setComponentColor(component, "backgroundColor", theme[5]);
    }

    public void mouseOut(Component component) {
      theme = themes[getActiveThemeIndex(Window.getActiveWindow())];
      setComponentColor(component, "backgroundColor", theme[0]);
    }

    public boolean mouseMove(Component component, int x, int y) {
      theme = themes[getActiveThemeIndex(Window.getActiveWindow())];
      setComponentColor(component, "backgroundColor", theme[5]);
      return false;
    }
  };

  /**
   * Default constructor for populating analyzers and defining actions when themes are selected
   * 
   * @throws java.io.IOException
   * @throws ClassNotFoundException
   */
  public LukeWindow() throws IOException, ClassNotFoundException {
    populateAnalyzers();
    close();
    Action.getNamedActions().put("defaulttheme", new Action() {
      public void perform(Component source) {
        recColorChange(Window.getActiveWindow(), themes[0]);
      }
    });
    Action.getNamedActions().put("graytheme", new Action() {
      public void perform(Component source) {
        recColorChange(Window.getActiveWindow(), themes[1]);
      }
    });
    Action.getNamedActions().put("sandstonetheme", new Action() {
      public void perform(Component source) {
        recColorChange(Window.getActiveWindow(), themes[2]);
      }
    });
    Action.getNamedActions().put("skytheme", new Action() {
      public void perform(Component source) {
        recColorChange(Window.getActiveWindow(), themes[3]);
      }
    });
    Action.getNamedActions().put("navytheme", new Action() {
      public void perform(Component source) {
        recColorChange(Window.getActiveWindow(), themes[4]);
      }
    });
  }

  /**
   * Open the Luke main window, color to default theme and open dialog for index
   *
   * @param display
   */
  public void openLukeWindow(Display display) {
    open(display, null);
    recColorChange(Window.getActiveWindow(), themes[0]);
    onOpen();
  }

  /**
   * Opens the dialog for getting index and color the dialog to default theme
   */
  public void onOpen() {
    lukeInitWindow.open(this);
    recColorChange(Window.getActiveWindow(), themes[0]);
  }

  /**
   * Exits the Luke application
   */
  public void exitLuke() {
    DesktopApplicationContext.exit();
  }

  /**
   * Convenience method for getting currently active theme
   *
   * @param activeWindow
   *          Currently active window
   * @return Index of currently active theme, got from themes[][]
   */
  private int getActiveThemeIndex(Window activeWindow) {
    if (activeWindow == null)
      return 0;
    else {
      String colorHexString = Integer.toHexString(((Color) activeWindow.getStyles().get("backgroundColor")).getRGB()).substring(2);
      if (colorHexString.equals(Integer.toHexString(themes[1][0])))
        return 1;
      else if (colorHexString.equals(Integer.toHexString(themes[2][0])))
        return 2;
      else if (colorHexString.equals(Integer.toHexString(themes[3][0])))
        return 3;
      else if (colorHexString.equals(Integer.toHexString(themes[4][0])))
        return 4;
      else
        return 0;
    }
  }

  /**
   * Populate a combobox with the current list of analyzers.
   *
   * @throws ClassNotFoundException
   * @throws java.io.IOException
   */
  public void populateAnalyzers() throws IOException, ClassNotFoundException {
    Class[] an = ClassFinder.getInstantiableSubclasses(Analyzer.class);
    if (an == null || an.length == 0) {
      System.err.println("No analyzers???");
      analyzers = defaultAnalyzers;
    } else {
      HashSet<Class> uniq = new HashSet<Class>(Arrays.asList(an));
      analyzers = (Class[]) uniq.toArray(new Class[uniq.size()]);
    }
    analyzerNames = new String[analyzers.length];
    for (int i = 0; i < analyzers.length; i++) {
      analyzerNames[i] = analyzers[i].getName();
    }
    Arrays.sort(analyzerNames);
  }

  /**
   * Open indicated index and re-initialize all GUI and plugins.
   *
   * @param force
   *          if true, and the index is locked, unlock it first. If false, and the index is locked, an error will be reported.
   * @param readOnly
   *          open in read-only mode, and disallow modifications.
   */
  public void openIndex(boolean force, String dirImpl, boolean readOnly, boolean ramDir, boolean keepCommits, IndexCommit point) {
    String indexPath = lukeInitWindow.indexPath.getText();
    this.indexPath = indexPath;

    // removeAll();
    File baseFileDir = new File(indexPath);
    baseDir = baseFileDir.toString();
    // addComponent(this, "/xml/luke.xml", null, null);
    // statmsg = find("statmsg");
    if (this.directory != null) {
      try {
        if (indexReader != null)
          indexReader.close();
      } catch (Exception e) {}

      try {
        if (directory != null)
          directory.close();
      } catch (Exception e) {}
    }
    java.util.ArrayList<Directory> dirs = new java.util.ArrayList<Directory>();
    try {
      Directory d = openDirectory(dirImpl, this.indexPath, false);
      if (d == null) {
        return;
      }

      if (IndexWriter.isLocked(d)) {
        if (readOnly) {
          errorMsg("Index is locked and Read-Only. Open for read-write and 'Force unlock'.");
          d.close();
          d = null;
          return;
        }
        if (force) {
          d.obtainLock(IndexWriter.WRITE_LOCK_NAME).close();
        } else {
          errorMsg("Index is locked. Try 'Force unlock' when opening.");
          d.close();
          d = null;
          return;
        }
      }
      boolean existsSingle = false;
      try {
        new SegmentInfos().readLatestCommit(d);
        existsSingle = true;
      } catch (Throwable e) {
        e.printStackTrace();
      }

      if (!existsSingle) { // try multi
        File[] files = baseFileDir.listFiles();
        for (File f : files) {
          if (f.isFile()) {
            continue;
          }
          Directory d1 = openDirectory(dirImpl, f.toString(), false);
          if (IndexWriter.isLocked(d1)) {
            if (readOnly) {
              errorMsg("Index is locked and Read-Only. Open for read-write and 'Force unlock'.");
              d1.close();
              d1 = null;
              return;
            }
            if (force) {
              d1.obtainLock(IndexWriter.WRITE_LOCK_NAME).close();
            } else {
              errorMsg("Index is locked. Try 'Force unlock' when opening.");
              d1.close();
              d1 = null;
              return;
            }
          }
          existsSingle = false;
          try {
            new SegmentInfos().readLatestCommit(d1);
            existsSingle = true;
          } catch (Throwable e) {
            e.printStackTrace();
          }
          if (!existsSingle) {
            d1.close();
            continue;
          }
          dirs.add(d1);
        }
      } else {
        dirs.add(d);
      }

      if (dirs.size() == 0) {
        errorMsg("No valid directory at the location, try another location.");
        return;
      }

      if (ramDir) {
        // TODO:
        showStatus("Loading index into RAMDirectory ...");
        Directory dir1 = new RAMDirectory();
        IndexWriterConfig cfg = new IndexWriterConfig(new SimpleAnalyzer());
        IndexWriter iw1 = new IndexWriter(dir1, cfg);
        iw1.addIndexes((Directory[]) dirs.toArray(new Directory[dirs.size()]));
        iw1.close();
        // TODO:
        showStatus("RAMDirectory loading done!");
        directory.close();
        directory = dir1;
      }
      java.util.ArrayList<IndexReader> readers = new java.util.ArrayList<IndexReader>();
      for (Directory dd : dirs) {
        IndexReader reader;
        reader = DirectoryReader.open(dd);
        readers.add(reader);
      }
      if (readers.size() == 1) {
        indexReader = readers.get(0);
        IndexReader ir = readers.get(0);
        directory = ((DirectoryReader) ir).directory();
      } else {
        indexReader = new MultiReader((IndexReader[]) readers.toArray(new IndexReader[readers.size()]));
      }

      lukeMediator.indexInfo = new IndexInfo(indexReader, this.indexPath, readOnly, keepCommits);

      // TODO:
      // Collection<String> fieldNames =
      // indexReader.getFieldNames(IndexReader.FieldOption.ALL);
      //
      // if (fieldNames.size() == 0) {
      // // showStatus("Empty index.");
      // }

      // call onOpenIndex for all tabs
      overviewTab.onOpenIndex();
      documentsTab.onOpenIndex();
      searchTab.onOpenIndex();
      filesTab.onOpenIndex();

      // initPlugins();
      showStatus("Index successfully open.");
      indexName.setText("Index path: " + indexPath);
    } catch (Exception e) {
      e.printStackTrace();
      errorMsg(e.getMessage());
      return;
    }
  }

  public FSDirectory openDirectory(String dirImpl, String file, boolean create) throws Exception {
    File f = new File(file);
    if (!f.exists()) {
      errorMsg("Index directory doesn't exist.");
      return null;
    }
    FSDirectory res = null;
    if (dirImpl == null || dirImpl.equals(FSDirectory.class.getName()) || dirImpl.equals(FSDirectory.class.getSimpleName())) {
      return FSDirectory.open(FileSystems.getDefault().getPath(file));
    }
    try {
      Class implClass = Class.forName(dirImpl);
      Constructor<FSDirectory> constr = implClass.getConstructor(File.class);
      res = constr.newInstance(f);
    } catch (Exception e) {
      errorMsg("Invalid directory implementation class: " + dirImpl);
      throw new RuntimeException(e);
    }
    if (res != null)
      return res;
    // fall-back to FSDirectory.
    return FSDirectory.open(FileSystems.getDefault().getPath(file));
  }

  public void showCommitFiles(Object commitTable) throws Exception {
    // List commits = new ArrayList();
    // Object[] rows = getSelectedItems(commitTable);
    // if (rows == null || rows.length == 0) {
    // showFiles(directory, commits);
    // return;
    // }
    // for (int i = 0; i < rows.length; i++) {
    // IndexCommit commit = (IndexCommit)getProperty(rows[i], "commit");
    // if (commit != null) {
    // commits.add(commit);
    // }
    // }
    // showFiles(directory, commits);
  }

  /**
   * Shows an alert dialog with an error message
   *
   * @param error
   *          Error message
   */
  private void errorMsg(String error) {
    Alert.alert(MessageType.ERROR, error, this);
  }

  /**
   * Shows a message on status bar
   *
   * @param message
   *          Message to display on status bar
   * @throws org.apache.pivot.serialization.SerializationException
   * @throws java.io.IOException
   */
  protected void showStatus(String message) throws IOException, SerializationException {
    // BXMLSerializer bxmlSerializer = new BXMLSerializer();
    // Window window = (Window) bxmlSerializer.readObject(LukeWindow.class, "LukeWindow.bxml");
    // Label label = (Label) bxmlSerializer.getNamespace().get("statusLabel");
    // label.setTextOrEmpty(message);
  }

  /**
   * Convenience method for coloring component
   * 
   * @param component
   *          component to color
   * @param style
   *          style to apply (Example : color, backgroundColor)
   * @param colorRGB
   *          color code passed as hexadecimal integer
   * 
   */
  private void setComponentColor(Component component, String style, int colorRGB) {
    Color color = Color.decode(Integer.toString(colorRGB));
    component.getStyles().put(style, color);
  }

  /**
   * Method for recursively coloring all components. If a component is a container, all children are obtained and styles set to get that theme
   * 
   * @param component
   *          component to color and get children
   * @param theme
   *          theme, passed as array of hexadecimal represented integer color codes
   */
  private void recColorChange(Component component, int[] theme) {
    if (component instanceof Window) {
      setComponentColor(component, "backgroundColor", theme[0]);
      for (int i = 0; i < ((Container) component).getLength(); i++) {
        recColorChange(((Window) component).get(i), theme);
      }
    } else if (component instanceof TabPane) {
      setComponentColor(component, "activeTabColor", theme[0]);
      setComponentColor(component, "backgroundColor", theme[0]);
      setComponentColor(component, "inactiveTabColor", theme[0]);
      setComponentColor(component, "buttonColor", theme[1]);
      setComponentColor(component, "borderColor", theme[3]);
      setComponentColor(component, "inactiveBorderColor", theme[3]);
      for (Component tab : ((TabPane) component).getTabs()) {
        recColorChange(tab, theme);
      }
    } else if (component instanceof Border) {
      setComponentColor(component, "backgroundColor", theme[0]);
      component = ((Border) component).getContent();
      recColorChange(component, theme);
    } else if (component instanceof Label) {
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[0]);
    } else if (component instanceof MenuBar) {
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[0]);
      setComponentColor(component, "activeBackgroundColor", theme[6]);
    } else if (component instanceof TextInput || component instanceof TextArea) {
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[2]);
      setComponentColor(component, "borderColor", theme[3]);
      setComponentColor(component, "activeBackgroundColor", theme[6]);
      setComponentColor(component, "selectionBackgroundColor", theme[8]);
      setComponentColor(component, "inactiveSelectionBackgroundColor", theme[8]);
    } else if (component instanceof TableView) {
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[2]);
      setComponentColor(component, "alternateRowBackgroundColor", theme[2]);
      setComponentColor(component, "horizontalGridColor", theme[3]);
      setComponentColor(component, "verticalGridColor", theme[3]);
      setComponentColor(component, "inactiveSelectionBackgroundColor", theme[8]);
      setComponentColor(component, "selectionBackgroundColor", theme[8]);
      setComponentColor(component, "highlightBackgroundColor", theme[8]);
    } else if (component instanceof TablePane) {
      setComponentColor(component, "backgroundColor", theme[0]);
      setComponentColor(component, "horizontalGridColor", theme[3]);
      setComponentColor(component, "verticalGridColor", theme[3]);
      for (Row row : ((TablePane) component).getRows()) {
        for (int i = 0; i < row.getLength(); i++) {
          recColorChange(row.get(i), theme);
        }
      }
    } else if (component instanceof Separator) {
      setComponentColor(component, "color", theme[0]);
      setComponentColor(component, "headingColor", theme[1]);
    } else if (component instanceof ListView) {
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[2]);
      setComponentColor(component, "selectionBackgroundColor", theme[8]);
      setComponentColor(component, "inactiveSelectionBackgroundColor", theme[8]);
      setComponentColor(component, "highlightBackgroundColor", theme[8]);
    } else if (component.toString().startsWith("org.apache.lucene.luke.ui")) {
      if (component.toString().endsWith("OverviewTab"))
        component = (Container) new BXMLSerializer().getNamespace().get("OverviewTab");
      else if (component.toString().endsWith("AnalyzersTab"))
        component = (Container) new BXMLSerializer().getNamespace().get("AnalyzersTab");
      else if (component.toString().endsWith("DocumentsTab"))
        component = (Container) new BXMLSerializer().getNamespace().get("DocumentsTab");
      else if (component.toString().endsWith("SearchTab"))
        component = (Container) new BXMLSerializer().getNamespace().get("SearchTab");
      else if (component.toString().endsWith("FilesTab"))
        component = (Container) new BXMLSerializer().getNamespace().get("FilesTab");

    } else if (component instanceof SplitPane) {
      if (((SplitPane) component).getLeft() != null) {
        recColorChange(((SplitPane) component).getLeft(), theme);
        recColorChange(((SplitPane) component).getRight(), theme);
      } else {
        System.out.println("TOP of : " + component.getParent());
        recColorChange(((SplitPane) component).getTop(), theme);
        recColorChange(((SplitPane) component).getBottom(), theme);
      }
    } else if (component instanceof ScrollBar) {
      setComponentColor(component, "backgroundColor", theme[0]);
      setComponentColor(component, "scrollButtonBackgroundColor", theme[2]);
      setComponentColor(component, "borderColor", theme[3]);
    } else if (component instanceof PushButton || component instanceof ListButton) {
      // Listeners are added at start-up time only.
      if (component.getComponentMouseButtonListeners().isEmpty()) {
        component.getComponentMouseButtonListeners().add(mouseButtonPressedListener);
      }
      if (component.getComponentMouseListeners().isEmpty()) {
        component.getComponentMouseListeners().add(mouseMoveListener);
      }
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[0]);
      setComponentColor(component, "borderColor", theme[3]);
    } else if (component instanceof RadioButton || component instanceof Checkbox || component instanceof LinkButton) {
      setComponentColor(component, "color", theme[1]);
    } else if (component instanceof ScrollPane) {
      setComponentColor(component, "backgroundColor", theme[0]);
      for (int i = 0; i < ((ScrollPane) component).getLength(); i++) {
        recColorChange(((ScrollPane) component).get(i), theme);
      }
    } else if (component instanceof FlowPane) {
      setComponentColor(component, "backgroundColor", theme[0]);
      for (int i = 0; i < ((FlowPane) component).getLength(); i++) {
        recColorChange(((FlowPane) component).get(i), theme);
      }
    } else if (component instanceof BoxPane) {
      setComponentColor(component, "backgroundColor", theme[0]);
      for (int i = 0; i < ((BoxPane) component).getLength(); i++) {
        recColorChange(((BoxPane) component).get(i), theme);
      }
    } else if (component instanceof GridPane) {
      setComponentColor(component, "backgroundColor", theme[0]);
      for (int i = 0; i < ((GridPane) component).getLength(); i++) {
        recColorChange(((GridPane) component).get(i), theme);
      }
    } else if (component instanceof FillPane) {
      setComponentColor(component, "backgroundColor", theme[0]);
      for (int i = 0; i < ((FillPane) component).getLength(); i++) {
        recColorChange(((FillPane) component).get(i), theme);
      }
    } else if (component instanceof ScrollPane.Corner) {
      setComponentColor(component, "backgroundColor", theme[0]);
    } else if (!(component instanceof ImageView)) {
      setComponentColor(component, "color", theme[1]);
      setComponentColor(component, "backgroundColor", theme[0]);
    }
  }

  @Override
  public void initialize(Map<String,Object> namespace, URL location, Resources resources) {
    documentsTab.initLukeMediator(lukeMediator);
    overviewTab.initLukeMediator(lukeMediator);
    filesTab.initLukeMediator(lukeMediator);
    searchTab.initLukeMediator(lukeMediator);
    analyzersTab.initLukeMediator(lukeMediator);
    lukeInitWindow.initLukeMediator(lukeMediator);
  }

  private IndexReader indexReader;
  //private java.util.Map<String,Decoder> decoders = new HashMap<String, Decoder>();
  private Decoder defDecoder = new StringDecoder();
  private String indexPath;

  private Directory directory;

  public class LukeMediator {

    // populated by LukeWindow#openIndex
    private IndexInfo indexInfo;

    public IndexInfo getIndexInfo() {
      return indexInfo;
    }

    public java.util.Map<String,Decoder> getDecoders() {
      return indexInfo.getDecoders();
    }

    public Decoder getDefDecoder() {
      return defDecoder;
    }

    public OverviewTab getOverViewTab() {
      return overviewTab;
    }

    public DocumentsTab getDocumentsTab() {
      return documentsTab;
    }

    public TabPane getTabPane() {
      return tabPane;
    }

    public LukeWindow getLukeWindow() {
      return LukeWindow.this;
    }

    public String[] getAnalyzerNames() {
      return analyzerNames;
    }
  }

}
