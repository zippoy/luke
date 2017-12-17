package org.apache.lucene.luke.models.documents;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.models.LukeException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Documents {

  void reset(IndexReader reader) throws LukeException;

  int getMaxDoc();

  Collection<String> getFieldNames();

  boolean isLive(int docid);

  Optional<List<DocumentField>> getDocumentFields(int docid) throws LukeException;

  String getCurrentField();

  Optional<Term> firstTerm(String field) throws LukeException;

  Optional<Term> nextTerm() throws LukeException;

  Optional<Term> seekTerm(String termText) throws LukeException;

  Optional<Integer> firstTermDoc() throws LukeException;

  Optional<Integer> nextTermDoc() throws LukeException;

  List<TermPosting> getTermPositions() throws LukeException;

  Optional<Integer> getDocFreq() throws LukeException;

  List<TermVectorEntry> getTermVectors(int docid, String field) throws LukeException;

  Optional<DocValues> getDocValues(int docid, String field) throws LukeException;
}
