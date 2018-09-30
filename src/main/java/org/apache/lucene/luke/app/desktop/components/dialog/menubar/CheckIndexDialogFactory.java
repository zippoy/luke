package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Inject;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.luke.app.DirectoryHandler;
import org.apache.lucene.luke.app.DirectoryObserver;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.app.desktop.util.TextAreaPrintStream;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.models.tools.IndexToolsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckIndexDialogFactory implements DialogOpener.DialogFactory {

  private static final Logger logger = LoggerFactory.getLogger(CheckIndexDialogFactory.class);

  private final IndexToolsFactory indexToolsFactory;

  private final IndexHandler indexHandler;

  private JLabel resultLbl = new JLabel();

  private final JLabel statusLbl = new JLabel();

  private final JLabel indicatorLbl = new JLabel();

  private final JButton repairBtn = new JButton();

  private final JTextArea logArea = new JTextArea();

  private LukeState lukeState;

  private CheckIndex.Status status;

  private final ListenerFunctions listeners = new ListenerFunctions();

  class Observer implements IndexObserver, DirectoryObserver {

    @Override
    public void openIndex(LukeState state) {
      lukeState = state;
      toolsModel = indexToolsFactory.newInstance(state.getIndexReader(), state.useCompound(), state.keepAllCommits());
    }

    @Override
    public void closeIndex() {
      close();
    }

    @Override
    public void openDirectory(LukeState state) {
      lukeState = state;
      toolsModel = indexToolsFactory.newInstance(state.getDirectory());
    }

    @Override
    public void closeDirectory() {
      close();
    }

    private void close() {
      toolsModel = null;
    }
  }

  class ListenerFunctions {

    void checkIndex(ActionEvent e) {
      ExecutorService executor = Executors.newSingleThreadExecutor();

      SwingWorker<CheckIndex.Status, Void> task = new SwingWorker<CheckIndex.Status, Void>() {

        @Override
        protected CheckIndex.Status doInBackground() {
          setProgress(0);
          statusLbl.setText("Running...");
          indicatorLbl.setVisible(true);
          TextAreaPrintStream ps = new TextAreaPrintStream(logArea, new ByteArrayOutputStream(), logger);
          try {
            CheckIndex.Status status = toolsModel.checkIndex(ps);
            statusLbl.setText("Done");
            return status;
          } catch (Exception e) {
            statusLbl.setText(MessageUtils.getLocalizedMessage("message.error.unknown"));
            throw e;
          } finally {
            ps.flush();
            setProgress(100);
          }
        }

        @Override
        protected void done() {
          try {
            CheckIndex.Status st = get();
            resultLbl.setText(createResultsMessage(st));
            indicatorLbl.setVisible(false);
            if (!st.clean) {
              repairBtn.setEnabled(true);
            }
            status = st;
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            statusLbl.setText(MessageUtils.getLocalizedMessage("message.error.unknown"));
          }
        }
      };

      executor.submit(task);
      executor.shutdown();
    }

    private String createResultsMessage(@Nullable CheckIndex.Status status) {
      String msg;
      if (status == null) {
        msg = "?";
      } else if (status.clean) {
        msg = "OK";
      } else if (status.toolOutOfDate) {
        msg = "ERROR: Can't check - tool out-of-date";
      } else {
        StringBuilder sb = new StringBuilder("BAD:");
        if (status.cantOpenSegments) {
          sb.append(" Can't open segmengs.");
        }
        if (status.missingSegments) {
          sb.append(" Missing segments.");
        }
        if (status.missingSegmentVersion) {
          sb.append(" Missing segment version.");
        }
        if (status.numBadSegments > 0) {
          sb.append(" numBadSegments=");
          sb.append(status.numBadSegments);
        }
        if (status.totLoseDocCount > 0) {
          sb.append(" totLoseDocCount=");
          sb.append(status.totLoseDocCount);
        }
        msg = sb.toString();
      }
      return msg;
    }

    void repairIndex(ActionEvent e) {
      if (status == null) {
        return;
      }

      ExecutorService executor = Executors.newSingleThreadExecutor();

      SwingWorker<CheckIndex.Status, Void> task = new SwingWorker<CheckIndex.Status, Void>() {

        @Override
        protected CheckIndex.Status doInBackground() {
          setProgress(0);
          statusLbl.setText("Running...");
          indicatorLbl.setVisible(true);
          logArea.setText("");
          TextAreaPrintStream ps = new TextAreaPrintStream(logArea, new ByteArrayOutputStream(), logger);
          try {
            toolsModel.repairIndex(status, ps);
            statusLbl.setText("Done");
            return status;
          } catch (Exception e) {
            statusLbl.setText(MessageUtils.getLocalizedMessage("message.error.unknown"));
            throw e;
          } finally {
            ps.flush();
            setProgress(100);
          }
        }

        @Override
        protected void done() {
          indexHandler.open(lukeState.getIndexPath(), lukeState.getDirImpl());
          logArea.append("Repairing index done.");
          resultLbl.setText("");
          indicatorLbl.setVisible(false);
          repairBtn.setEnabled(false);
        }
      };

      executor.submit(task);
      executor.shutdown();
    }
  }


  @Inject
  public CheckIndexDialogFactory(IndexToolsFactory indexToolsFactory, IndexHandler indexHandler, DirectoryHandler directoryHandler) {
    this.indexToolsFactory = indexToolsFactory;
    this.indexHandler = indexHandler;

    indexHandler.addObserver(new Observer());
    directoryHandler.addObserver(new Observer());
  }

  private JDialog dialog;

  private IndexTools toolsModel;

  @Override
  public JDialog create(Window owner, String title, int width, int height) {
    dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.add(content());
    dialog.setSize(new Dimension(width, height));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  private JPanel content() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(15,15, 15, 15));

    panel.add(controller());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(logs());

    return panel;
  }

  private JPanel controller() {
    JPanel panel = new JPanel(new GridLayout(3, 1));

    JPanel idxPath = new JPanel(new FlowLayout(FlowLayout.LEADING));
    idxPath.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.index_path")));
    JLabel idxPathLbl = new JLabel(lukeState.getIndexPath());
    idxPathLbl.setToolTipText(lukeState.getIndexPath());
    idxPath.add(idxPathLbl);
    panel.add(idxPath);

    JPanel results = new JPanel(new GridLayout(2,1));
    results.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    results.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.results")));
    results.add(resultLbl);
    panel.add(results);

    JPanel execButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton checkBtn = new JButton(MessageUtils.getLocalizedMessage("checkidx.button.check"), ImageUtils.createImageIcon("/img/icon_search_alt.png", 20, 20));
    checkBtn.setFont(new Font(checkBtn.getFont().getFontName(), Font.PLAIN, 15));
    checkBtn.setMargin(new Insets(3, 3, 3, 3));
    checkBtn.addActionListener(listeners::checkIndex);
    execButtons.add(checkBtn);

    JButton closeBtn = new JButton(MessageUtils.getLocalizedMessage("button.close"));
    closeBtn.setFont(new Font(closeBtn.getFont().getFontName(), Font.PLAIN, 15));
    closeBtn.setMargin(new Insets(3, 3, 3, 3));
    closeBtn.addActionListener(e -> dialog.dispose());
    execButtons.add(closeBtn);
    panel.add(execButtons);

    return panel;
  }

  private JPanel logs() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));

    JPanel repair = new JPanel(new FlowLayout(FlowLayout.LEADING));
    repairBtn.setText(MessageUtils.getLocalizedMessage("checkidx.button.fix"));
    repairBtn.setIcon(ImageUtils.createImageIcon("/img/icon_tool.png", 20, 20));
    repairBtn.setFont(new Font(repairBtn.getFont().getFontName(), Font.PLAIN, 15));
    repairBtn.setMargin(new Insets(3, 3, 3, 3));
    repairBtn.setEnabled(false);
    repairBtn.addActionListener(listeners::repairIndex);
    repair.add(repairBtn);

    JTextArea warnArea = new JTextArea(MessageUtils.getLocalizedMessage("checkidx.label.warn"), 3, 30);
    warnArea.setLineWrap(true);
    warnArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    repair.add(warnArea);
    header.add(repair);

    JPanel note = new JPanel(new FlowLayout(FlowLayout.LEADING));
    note.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.note")));
    header.add(note);

    JPanel status = new JPanel(new FlowLayout(FlowLayout.LEADING));
    status.add(new JLabel(MessageUtils.getLocalizedMessage("checkidx.label.status")));
    statusLbl.setText("Idle");
    status.add(statusLbl);
    indicatorLbl.setIcon(ImageUtils.createImageIcon("/img/indicator.gif", 20, 20));
    indicatorLbl.setVisible(false);
    status.add(indicatorLbl);
    header.add(status);

    panel.add(header, BorderLayout.PAGE_START);

    logArea.setText("");
    logArea.setEditable(false);
    panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

    return panel;
  }
}
