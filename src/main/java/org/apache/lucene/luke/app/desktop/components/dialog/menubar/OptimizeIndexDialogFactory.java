package org.apache.lucene.luke.app.desktop.components.dialog.menubar;

import com.google.inject.Inject;
import org.apache.lucene.luke.app.IndexHandler;
import org.apache.lucene.luke.app.IndexObserver;
import org.apache.lucene.luke.app.LukeState;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.ImageUtils;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.app.desktop.util.TextAreaPrintStream;
import org.apache.lucene.luke.models.tools.IndexTools;
import org.apache.lucene.luke.models.tools.IndexToolsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
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

public class OptimizeIndexDialogFactory implements DialogOpener.DialogFactory {

  private static final Logger logger = LoggerFactory.getLogger(OptimizeIndexDialogFactory.class);

  private final IndexToolsFactory indexToolsFactory;

  private final IndexHandler indexHandler;

  private final JCheckBox expungeCB = new JCheckBox();

  private final JSpinner maxSegSpnr = new JSpinner();

  private final JLabel statusLbl = new JLabel();

  private final JLabel indicatorLbl = new JLabel();

  private final JTextArea logArea = new JTextArea();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private JDialog dialog;

  private IndexTools toolsModel;

  class Observer implements IndexObserver {

    @Override
    public void openIndex(LukeState state) {
      toolsModel = indexToolsFactory.newInstance(state.getIndexReader(), state.useCompound(), state.keepAllCommits());
    }

    @Override
    public void closeIndex() {
      toolsModel = null;
    }

  }

  class ListenerFunctions {

    void optimize(ActionEvent e) {
      ExecutorService executor = Executors.newSingleThreadExecutor();

      SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {

        @Override
        protected Void doInBackground() {
          setProgress(0);
          statusLbl.setText("Running...");
          indicatorLbl.setVisible(true);
          TextAreaPrintStream ps = new TextAreaPrintStream(logArea, new ByteArrayOutputStream(), logger);
          try {
            toolsModel.optimize(expungeCB.isSelected(), (int)maxSegSpnr.getValue(), ps);
            statusLbl.setText("Done");
            indexHandler.reOpen();
          } catch (Exception e) {
            statusLbl.setText(MessageUtils.getLocalizedMessage("message.error.unknown"));
            throw e;
          } finally {
            ps.flush();
            setProgress(100);
          }
          return null;
        }

        @Override
        protected void done() {
          indicatorLbl.setVisible(false);
        }
      };

      executor.submit(task);
      executor.shutdown();
    }

  }

  @Inject
  public OptimizeIndexDialogFactory(IndexToolsFactory indexToolsFactory, IndexHandler indexHandler) {
    this.indexToolsFactory = indexToolsFactory;
    this.indexHandler = indexHandler;
    indexHandler.addObserver(new Observer());
  }

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
    JPanel panel = new JPanel(new GridLayout(4, 1));

    JPanel idxPath = new JPanel(new FlowLayout(FlowLayout.LEADING));
    idxPath.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.index_path")));
    JLabel idxPathLbl = new JLabel(indexHandler.getState().getIndexPath());
    idxPathLbl.setToolTipText(indexHandler.getState().getIndexPath());
    idxPath.add(idxPathLbl);
    panel.add(idxPath);

    JPanel expunge = new JPanel(new FlowLayout(FlowLayout.LEADING));
    expungeCB.setText(MessageUtils.getLocalizedMessage("optimize.checkbox.expunge"));
    expunge.add(expungeCB);
    panel.add(expunge);

    JPanel maxSegs = new JPanel(new FlowLayout(FlowLayout.LEADING));
    maxSegs.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.max_segments")));
    maxSegSpnr.setModel(new SpinnerNumberModel(1, 1, 100, 1));
    maxSegSpnr.setPreferredSize(new Dimension(100, 30));
    maxSegs.add(maxSegSpnr);
    panel.add(maxSegs);

    JPanel execButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    JButton optimizeBtn = new JButton(MessageUtils.getLocalizedMessage("optimize.button.optimize"),
        ImageUtils.createImageIcon("/img/icon_balance.png", 20, 20));
    optimizeBtn.setFont(new Font(optimizeBtn.getFont().getFontName(), Font.PLAIN, 15));
    optimizeBtn.setMargin(new Insets(3, 3, 3, 3));
    optimizeBtn.addActionListener(listeners::optimize);
    execButtons.add(optimizeBtn);
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

    JPanel header = new JPanel(new GridLayout(2,1));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.note")));
    JPanel status = new JPanel(new FlowLayout(FlowLayout.LEADING));
    status.add(new JLabel(MessageUtils.getLocalizedMessage("optimize.label.status")));
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
