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

package org.apache.lucene.luke.models.documents;

import com.google.inject.Inject;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.luke.models.BaseModel;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.util.BytesRefUtils;
import org.apache.lucene.luke.util.IndexUtils;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DocumentsImpl extends BaseModel implements Documents {

  private static Logger logger = LoggerFactory.getLogger(DocumentsImpl.class);

  private TermVectorsAdapter tvAdapter;

  private DocValuesAdapter dvAdapter;

  private String curField;

  private TermsEnum tenum;

  private PostingsEnum penum;

  @Inject
  DocumentsImpl() {
    this.tvAdapter = new TermVectorsAdapterImpl();
    this.dvAdapter = new DocValuesAdapterImpl();
  }

  @Override
  public void reset(@Nonnull IndexReader reader) throws LukeException {
    super.reset(reader);
    tvAdapter.reset(reader);
    dvAdapter.reset(reader);
    this.curField = null;
    this.tenum = null;
    this.penum = null;
  }

  @Override
  public int getMaxDoc() {
    return reader.maxDoc();
  }

  @Override
  public boolean isLive(int docid) {
    return liveDocs == null || liveDocs.get(docid);
  }

  @Override
  public Optional<List<DocumentField>> getDocumentFields(int docid) throws LukeException {
    if (!isLive(docid)) {
      logger.info("Doc #{} was deleted", docid);
      return Optional.empty();
    }

    try {
      Document doc = reader.document(docid);
      List<DocumentField> res = new ArrayList<>();
      for (FieldInfo finfo : IndexUtils.getFieldInfos(reader)) {
        IndexableField[] fields = doc.getFields(finfo.name);
        if (fields.length == 0) {
          res.add(DocumentField.of(finfo, reader, docid));
        } else {
          for (IndexableField field : fields) {
            res.add(DocumentField.of(finfo, field, reader, docid));
          }
        }
      }
      return Optional.of(res);
    } catch (IOException e) {
      String msg = String.format("Fields information not available for doc %d.", docid);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public String getCurrentField() {
    return curField;
  }

  @Override
  public Optional<Term> firstTerm(@Nonnull String field) throws LukeException {
    try {
      Terms terms = IndexUtils.getTerms(reader, field);
      if (terms == null) {
        // no such field?
        this.curField = null;
        this.tenum = null;
        logger.warn("Terms not available for field: {}.", field);
        return Optional.empty();
      } else {
        this.curField = field;
        this.tenum = terms.iterator();
        if (tenum.next() == null) {
          // no term available for this field
          tenum = null;
          logger.warn("No term available for field: {}.", field);
          return Optional.empty();
        } else {
          return Optional.of(new Term(curField, tenum.term()));
        }
      }
    } catch (IOException e) {
      tenum = null;
      String msg = String.format("Terms not available for field: %s.", field);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    } finally {
      // discard current postings enum
      this.penum = null;
    }
  }

  @Override
  public Optional<Term> nextTerm() throws LukeException {
    if (tenum == null) {
      // terms enum not initialized
      logger.warn("Terms enum un-positioned.");
      return Optional.empty();
    }
    try {
      if (tenum.next() == null) {
        // end of the iterator
        tenum = null;
        logger.info("Reached the end of the term iterator for field: {}.", curField);
        return Optional.empty();
      } else {
        return Optional.of(new Term(curField, tenum.term()));
      }
    } catch (IOException e) {
      tenum = null;
      String msg = String.format("Terms not available for field: %s.", curField);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    } finally {
      // discard current postings enum
      this.penum = null;
    }
  }

  @Override
  public Optional<Term> seekTerm(@Nonnull String termText) throws LukeException {
    if (curField == null) {
      // field is not selected
      logger.warn("Field not selected.");
      return Optional.empty();
    }
    try {
      Terms terms = IndexUtils.getTerms(reader, curField);
      this.tenum = terms.iterator();
      if (tenum.seekCeil(new BytesRef(termText)) == TermsEnum.SeekStatus.END) {
        // end of the iterator
        tenum = null;
        logger.info("Reached the end of the term iterator for field: {}.", curField);
        return Optional.empty();
      } else {
        return Optional.of(new Term(curField, tenum.term()));
      }
    } catch (IOException e) {
      tenum = null;
      String msg = String.format("Terms not available for field: %s.", curField);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    } finally {
      // discard current postings enum
      this.penum = null;
    }
  }

  @Override
  public Optional<Integer> firstTermDoc() throws LukeException {
    if (tenum == null) {
      // terms enum is not set
      logger.warn("Terms enum un-positioned.");
      return Optional.empty();
    }
    try {
      this.penum = tenum.postings(penum, PostingsEnum.ALL);
      if (penum.nextDoc() == PostingsEnum.NO_MORE_DOCS) {
        // no docs available for this term
        penum = null;
        logger.warn("No docs available for term: {} in field: {}.", BytesRefUtils.decode(tenum.term()), curField);
        return Optional.empty();
      } else {
        return Optional.of(penum.docID());
      }
    } catch (IOException e) {
      penum = null;
      String msg = String.format("Term docs not available for field: %s.", curField);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Optional<Integer> nextTermDoc() throws LukeException {
    if (penum == null) {
      // postings enum is not initialized
      logger.warn("Postings enum un-positioned for field: {}.", curField);
      return Optional.empty();
    }
    try {
      if (penum.nextDoc() == PostingsEnum.NO_MORE_DOCS) {
        // end of the iterator
        penum = null;
        logger.info("Reached the end of the postings iterator for term: {} in field: {}", BytesRefUtils.decode(tenum.term()), curField);
        return Optional.empty();
      } else {
        return Optional.of(penum.docID());
      }
    } catch (IOException e) {
      penum = null;
      String msg = String.format("Term docs not available for field: %s.", curField);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public List<TermPosting> getTermPositions() throws LukeException {
    if (penum == null) {
      // postings enum is not initialized
      logger.warn("Postings enum un-positioned for field: {}.", curField);
      return Collections.emptyList();
    }
    try {
      List<TermPosting> res = new ArrayList<>();
      int freq = penum.freq();
      for (int i = 0; i < freq; i++) {
        int position = penum.nextPosition();
        if (position < 0) {
          // no position information available
          continue;
        }
        TermPosting posting = TermPosting.of(position, penum);
        res.add(posting);
      }
      return res;
    } catch (IOException e) {
      String msg = String.format("Postings not available for field %s.", curField);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }


  @Override
  public Optional<Integer> getDocFreq() throws LukeException {
    if (tenum == null) {
      // terms enum is not initialized
      logger.warn("Terms enum un-positioned for field: {}.", curField);
      return Optional.empty();
    }
    try {
      return Optional.of(tenum.docFreq());
    } catch (IOException e) {
      String msg = String.format("Doc frequency not available for field: %s.", curField);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public List<TermVectorEntry> getTermVectors(int docid, String field) throws LukeException {
    try {
      return tvAdapter.getTermVector(docid, field);
    } catch (IOException e) {
      String msg = String.format("Term vector not available for doc: #%d and field: %s", docid, field);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Optional<DocValues> getDocValues(int docid, String field) throws LukeException {
    try {
      return dvAdapter.getDocValues(docid, field);
    } catch (IOException e) {
      String msg = String.format("Doc values not available for doc: #%d and field: %s", docid, field);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }
}
