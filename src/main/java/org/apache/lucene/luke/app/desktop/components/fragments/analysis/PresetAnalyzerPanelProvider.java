package org.apache.lucene.luke.app.desktop.components.fragments.analysis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.luke.app.desktop.components.AnalysisPanelProvider;
import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class PresetAnalyzerPanelProvider implements Provider<JPanel> {

  private final ComponentOperatorRegistry operatorRegistry;

  private final JComboBox<String> analyzersCB = new JComboBox<>();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private

  class ListenerFunctions {

    void setAnalyzer(ActionEvent e) {
      operatorRegistry.get(AnalysisPanelProvider.AnalysisPanelOperator.class).ifPresent(operator ->
          operator.setAnalyzerByType((String)analyzersCB.getSelectedItem())
      );
    }

  }

  class PresetAnalyzerPaneOperatorImpl implements PresetAnalyzerPaneOperator {

    @Override
    public void setPresetAnalyzers(Collection<Class<? extends Analyzer>> presetAnalyzers) {
      String[] analyzerNames = presetAnalyzers.stream().map(Class::getName).toArray(String[]::new);
      ComboBoxModel<String> model = new DefaultComboBoxModel<>(analyzerNames);
      analyzersCB.setModel(model);
    }

    @Override
    public void setSelectedAnalyzer(Class<? extends Analyzer> analyzer) {
      analyzersCB.setSelectedItem(analyzer.getName());
    }
  }

  @Inject
  public PresetAnalyzerPanelProvider(ComponentOperatorRegistry operatorRegistry) {
    this.operatorRegistry = operatorRegistry;
    operatorRegistry.register(PresetAnalyzerPaneOperator.class, new PresetAnalyzerPaneOperatorImpl());
  }

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JLabel header = new JLabel(MessageUtils.getLocalizedMessage("analysis_preset.label.preset"));
    panel.add(header, BorderLayout.PAGE_START);

    JPanel center = new JPanel(new FlowLayout(FlowLayout.LEADING));
    center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    center.setPreferredSize(new Dimension(400, 40));
    analyzersCB.addActionListener(listeners::setAnalyzer);
    center.add(analyzersCB);
    panel.add(center, BorderLayout.CENTER);

    return panel;
  }

  public interface PresetAnalyzerPaneOperator extends ComponentOperatorRegistry.ComponentOperator {
    void setPresetAnalyzers(Collection<Class<? extends Analyzer>> presetAnalyzers);
    void setSelectedAnalyzer(Class<? extends Analyzer> analyzer);

  }

}
