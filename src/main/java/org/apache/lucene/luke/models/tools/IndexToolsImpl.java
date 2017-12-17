package org.apache.lucene.luke.models.tools;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.luke.models.BaseModel;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.util.IndexUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

public class IndexToolsImpl extends BaseModel implements IndexTools {

  private String indexPath;

  private boolean useCompound;

  private boolean keepAllCommits;

  private static final Class[] presetFieldClasses = new Class[]{
      TextField.class, StringField.class,
      IntPoint.class, LongPoint.class, FloatPoint.class, DoublePoint.class,
      SortedDocValuesField.class, SortedSetDocValuesField.class,
      NumericDocValuesField.class, SortedNumericDocValuesField.class,
      StoredField.class
  };

  @Override
  public void reset(Directory dir, String indexPath, boolean useCompound, boolean keepAllCommits) {
    super.reset(dir);
    this.indexPath = indexPath;
    this.useCompound = useCompound;
    this.keepAllCommits = keepAllCommits;
  }

  @Override
  public void reset(IndexReader reader, String indexPath, boolean useCompound, boolean keepAllCommits) throws LukeException {
    super.reset(reader);
    this.indexPath = indexPath;
    this.useCompound = useCompound;
    this.keepAllCommits = keepAllCommits;
  }

  @Override
  public void optimize(boolean expunge, int maxNumSegments, PrintStream ps) throws LukeException {
    if (reader instanceof DirectoryReader) {
      Directory dir = ((DirectoryReader) reader).directory();
      try (IndexWriter writer = IndexUtils.createWriter(dir, null, useCompound, keepAllCommits, ps)) {
        IndexUtils.optimizeIndex(writer, expunge, maxNumSegments);
      } catch (IOException e) {
        throw new LukeException("Failed to optimize index", e);
      }
    } else {
      throw new LukeException("Current reader is not an instance of DirectoryReader.");
    }
  }

  @Override
  public CheckIndex.Status checkIndex(PrintStream ps) throws LukeException {
    try {
      if (dir != null) {
        return IndexUtils.checkIndex(dir, ps);
      } else if (reader instanceof DirectoryReader) {
        Directory dir = ((DirectoryReader) reader).directory();
        return IndexUtils.checkIndex(dir, ps);
      } else {
        throw new IllegalStateException("Directory is not set.");
      }
    } catch (Exception e) {
      throw new LukeException("Failed to check index.", e);
    }
  }

  @Override
  public void repairIndex(CheckIndex.Status st, PrintStream ps) throws LukeException {
    try {
      if (dir != null) {
        IndexUtils.tryRepairIndex(dir, st, ps);
      } else {
        throw new IllegalStateException("Directory is not set.");
      }
    } catch (Exception e) {
      throw new LukeException("Failed to repair index.", e);
    }
  }

  @Override
  public void addDocument(Document doc, @Nullable Analyzer analyzer) throws LukeException {
    if (reader instanceof DirectoryReader) {
      Directory dir = ((DirectoryReader) reader).directory();
      try (IndexWriter writer = IndexUtils.createWriter(dir, analyzer, useCompound, keepAllCommits)) {
        writer.addDocument(doc);
        writer.commit();
      } catch (IOException e) {
        throw new LukeException("Failed to add document", e);
      }
    } else {
      throw new LukeException("Current reader is not an instance of DirectoryReader.");
    }
  }

  @Override
  public void deleteDocuments(@Nonnull Query query) throws LukeException {
    if (reader instanceof DirectoryReader) {
      Directory dir = ((DirectoryReader) reader).directory();
      try (IndexWriter writer = IndexUtils.createWriter(dir, null, useCompound, keepAllCommits)) {
        writer.deleteDocuments(query);
        writer.commit();
      } catch (IOException e) {
        throw new LukeException("Failed to add document", e);
      }
    } else {
      throw new LukeException("Current reader is not an instance of DirectoryReader.");
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Class<? extends Field>> getPresetFields() {
    return Arrays.asList(presetFieldClasses);
  }
}
