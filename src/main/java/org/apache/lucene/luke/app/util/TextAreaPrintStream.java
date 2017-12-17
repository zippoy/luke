package org.apache.lucene.luke.app.util;

import javafx.scene.control.TextArea;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class TextAreaPrintStream extends PrintStream {


  private Logger logger;

  private ByteArrayOutputStream baos;

  private TextArea textArea;

  public TextAreaPrintStream(TextArea textArea, ByteArrayOutputStream baos, Logger logger) {
    super(baos, false);
    this.baos = baos;
    this.textArea = textArea;
    this.logger = logger;
    baos.reset();
    textArea.selectEnd();
  }

  @Override
  public void println(String s) {
    try {
      baos.write(s.getBytes());
      baos.write('\n');
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Override
  public void flush() {
    try {
      textArea.selectEnd();
      textArea.appendText(baos.toString(StandardCharsets.UTF_8.name()));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    } finally {
      baos.reset();
    }
  }

}

