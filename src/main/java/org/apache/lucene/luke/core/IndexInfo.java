package org.apache.lucene.luke.core;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexGate.FormatDetails;
import org.apache.lucene.luke.core.decoders.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.util.*;

public class IndexInfo {
  private IndexReader reader;
  private IndexSearcher indexSearcher;
  private Directory dir;
  private String indexPath;
  private long totalFileSize;
  private int numTerms = -1;
  private FormatDetails formatDetails;
  private TermStats[] topTerms = null;
  private List<String> fieldNames;
  private String lastModified;
  private String version;
  private String dirImpl;
  private HashMap<String, FieldTermCount> termCounts = null;
  private Map<String, Decoder> decoders;
  private Codec indexCodec;
  private int indexFormat;
  private boolean readOnly;
  private boolean keepCommits;

  public IndexInfo(IndexReader reader, String indexPath, boolean readOnly, boolean keepCommits) throws Exception {
    this.reader = reader;
    this.indexSearcher = new IndexSearcher(reader);
    this.dir = null;
    this.dirImpl = "N/A";
    if (reader instanceof DirectoryReader) {
      this.dir = ((DirectoryReader)reader).directory();
      this.dirImpl = dir.getClass().getName();
      this.version = Long.toString(((DirectoryReader)reader).getVersion());
    }
    this.indexPath = indexPath;
    lastModified = "N/A";
    totalFileSize = dir == null ? -1 : Util.calcTotalFileSize(indexPath, dir);
    fieldNames = new ArrayList<String>();
    fieldNames.addAll(Util.fieldNames(reader, false));
    Collections.sort(fieldNames);
    if (dir != null) {
      formatDetails = IndexGate.getIndexFormat(dir);
      indexCodec = IndexGate.getCodecOfFirstSegment(dir);
      indexFormat = IndexGate.getIndexFormatIntValue(dir);
      lastModified = IndexGate.getLastModified(dir);
    } else {
      formatDetails = new FormatDetails();
    }

    this.readOnly = readOnly;
    this.keepCommits = keepCommits;
  }

  private void countTerms() throws Exception {
    termCounts = new HashMap<String, FieldTermCount>();
    numTerms = 0;
    Fields fields = MultiFields.getFields(reader);

      // if there are no postings, throw an exception
      if (fields == null) {
          throw new Exception("There are no postings in the index reader.");
      }

    Iterator<String> fe = fields.iterator();
    TermsEnum te = null;

    while (fe.hasNext()) {
      String fld = fe.next();
      FieldTermCount ftc = new FieldTermCount();
      ftc.fieldname = fld;
      Terms terms = fields.terms(fld);
      if (terms != null) { // count terms
        te = terms.iterator();
        while (te.next() != null) {
          ftc.termCount++;
          numTerms++;
        }
      }
      termCounts.put(fld, ftc);
    }
  }

  /**
   * @return the reader
   */
  public IndexReader getReader() {
    return reader;
  }

  public Directory getDirectory() {
    return dir;
  }

  /**
   * @return the indexPath
   */
  public String getIndexPath() {
    return indexPath;
  }

  /**
   * @return the read only flag
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   *
   * @return the keep kommits flag
   */
  public boolean isKeepCommits() {
    return keepCommits;
  }

  /**
   * @return the index searcher
   */
  public IndexSearcher getIndexSearcher() {
    return indexSearcher;
  }


  /**
   * @return the totalFileSize
   */
  public long getTotalFileSize() {
    return totalFileSize;
  }

  /**
   * @return the numTerms
   */
  public int getNumTerms() throws Exception {
    if (numTerms == -1) {
      countTerms();
    }
    return numTerms;
  }

  /**
   * @return the formatDetails
   */
  public FormatDetails getIndexFormat() {
    return formatDetails;
  }

  /**
   * @return the Lucene index format version number
   */
  public int getIndexFormatVersion() {
    return indexFormat;
  }

  public Map<String,FieldTermCount> getFieldTermCounts() throws Exception {
    if (termCounts == null) {
      countTerms();
    }
    return termCounts;
  }

  /**
   * @return the topTerms
   */
  public TermStats[] getTopTerms() throws Exception {
    if (topTerms == null) {
      topTerms = HighFreqTerms.getHighFreqTerms(reader, 50, null);
    }
    return topTerms;
  }

  /**
   * @return the fieldNames
   */
  public List<String> getFieldNames() {
    return fieldNames;
  }

  /**
   * @return the lastModified
   */
  public String getLastModified() {
    return lastModified;
  }
  
  public String getVersion() {
    return version;
  }
  
  public String getDirImpl() {
    return dirImpl;
  }

  /**
   * @return the index codec of first segment
   */
  public Codec getIndexCodec() {
    return indexCodec;
  }

  /**
   * @return the pairs of the field name and Decoder
   */
  public Map<String, Decoder> getDecoders() {
    if (decoders == null) {
      try {
        guessDecoders();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return decoders;
  }

  private void guessDecoders() throws Exception {
    decoders = new HashMap<String, Decoder>();

    Fields fields = MultiFields.getFields(reader);

    // if there are no postings, throw an exception
    if (fields == null) {
      throw new Exception("There are no postings in the index reader.");
    }

    Iterator<String> fe = fields.iterator();
    while (fe.hasNext()) {
      String fld = fe.next();
      Terms terms = fields.terms(fld);
      TermsEnum te = null;
      PostingsEnum pe = null;
      if (terms != null) {
        te = terms.iterator();
        te.next();
        pe = MultiFields.getTermDocsEnum(reader, fld, te.term());
        IndexableField field = null;
        while (field == null && pe.nextDoc() != DocsEnum.NO_MORE_DOCS) {
          // look up first document which has this field value.
          int docId = pe.docID();
          Document doc = reader.document(docId);
          field = doc.getField(fld);
        }
        if (field == null) {
          // there is no document having this field value.
          continue;
        }

        // guess possible Decoder by field value of the first document
        // TODO should be better way ...
        if (field.numericValue() != null) {
          // if non-null, this field has a numeric value
          Number value = field.numericValue();
          if (value instanceof Integer) {
            decoders.put(fld, new NumIntDecoder());
          } else if (value instanceof Long) {
            // TODO could be DateDecoder
            decoders.put(fld, new NumLongDecoder());
          } else if (value instanceof Float) {
            decoders.put(fld, new NumFloatDecoder());
          } else if (value instanceof Double) {
            decoders.put(fld, new NumDoubleDecoder());
          }
        } else if (field.binaryValue() != null) {
          // if non-null, this field has a binary values
          decoders.put(fld, new BinaryDecoder());
        } else {
          // this field may have a string value
          decoders.put(fld, new StringDecoder());
        }
      }
    }
  }

}
