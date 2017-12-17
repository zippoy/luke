package org.apache.lucene.luke.models;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.util.IndexUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.Collection;

public abstract class BaseModel {

  protected Directory dir;

  protected IndexReader reader;

  protected Bits liveDocs;

  protected IndexCommit commit;

  protected void reset(Directory dir) {
    this.dir = dir;
    this.reader = null;
    this.commit = null;
  }

  protected void reset(IndexReader reader) throws LukeException {
    this.reader = reader;

    if (reader instanceof DirectoryReader) {
      DirectoryReader dr = (DirectoryReader) reader;
      this.dir = dr.directory();
      try {
        this.commit = dr.getIndexCommit();
      } catch (IOException e) {
        throw new LukeException(e.getMessage(), e);
      }
    } else {
      this.dir = null;
      this.commit = null;
    }

    try {
      this.liveDocs = IndexUtils.getLiveDocs(reader);
    } catch (IOException e) {
      throw new LukeException(e.getMessage(), e);
    }
  }

  public Collection<String> getFieldNames() {
    return IndexUtils.getFieldNames(reader);
  }
}
