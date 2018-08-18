package org.apache.lucene.luke.app.desktop.components.fragments.analysis;

import com.google.inject.Provider;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.*;
import java.awt.*;

public class PresetAnalyzerPaneProvider implements Provider<JPanel> {

  @Override
  public JPanel get() {
    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 250, 3));

    JLabel header = new JLabel(MessageUtils.getLocalizedMessage("analysis_preset.label.preset"));
    panel.add(header);

    JComboBox<String> analyzersCB = new JComboBox<>();
    panel.add(analyzersCB);

    return panel;
  }

}
