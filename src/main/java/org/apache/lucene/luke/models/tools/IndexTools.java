package org.apache.lucene.luke.models.tools;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.PrintStream;
import java.util.Collection;

public interface IndexTools {

  void reset(Directory dir, String indexPath, boolean useCompound, boolean keepAllCommits);

  void reset(IndexReader reader, String indexPath, boolean useCompound, boolean keepAllCommits) throws LukeException;

  void optimize(boolean expunge, int maxNumSegments, PrintStream ps) throws LukeException;

  CheckIndex.Status checkIndex(PrintStream ps) throws LukeException;

  void repairIndex(CheckIndex.Status st, PrintStream ps) throws LukeException;

  void addDocument(Document doc, Analyzer analyzer) throws LukeException;

  void deleteDocuments(Query query) throws LukeException;

  Collection<Class<? extends Field>> getPresetFields();
}
