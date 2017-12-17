package org.apache.lucene.luke.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.NoDeletionPolicy;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IndexUtils {

  private static final Logger logger = LoggerFactory.getLogger(IndexUtils.class);

  public static IndexReader openIndex(@Nonnull String indexPath, @Nullable String dirImpl)
      throws Exception {
    final Path root = FileSystems.getDefault().getPath(indexPath);
    final List<DirectoryReader> readers = new ArrayList<>();

    // try multi
    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        Directory dir = openDirectory(path, dirImpl);
        try {
          DirectoryReader dr = DirectoryReader.open(dir);
          readers.add(dr);
        } catch (IOException e) {
          // skip
        }
        return FileVisitResult.CONTINUE;
      }
    });

    if (readers.isEmpty()) {
      throw new RuntimeException("No valid directory at the location: " + indexPath);
    }

    logger.info(String.format("IndexReaders (%d leaf readers) successfully opened. Index path=%s", readers.size(), indexPath));

    if (readers.size() == 1) {
      return readers.get(0);
    } else {
      return new MultiReader(readers.toArray(new IndexReader[readers.size()]));
    }
  }

  public static Directory openDirectory(@Nonnull String dirPath, @Nullable String dirImpl) throws IOException {
    final Path path = FileSystems.getDefault().getPath(dirPath);
    Directory dir = openDirectory(path, dirImpl);
    logger.info(String.format("DirectoryReader successfully opened. Directory path=%s", dirPath));
    return dir;
  }

  private static Directory openDirectory(@Nonnull Path path, String dirImpl) throws IOException {
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("Index directory doesn't exist.");
    }

    Directory dir;
    if (dirImpl == null || dirImpl.equalsIgnoreCase("org.apache.lucene.store.FSDirectory")) {
      dir = FSDirectory.open(path);
    } else {
      try {
        Class<?> implClazz = Class.forName(dirImpl);
        Constructor<?> constr = implClazz.getConstructor(Path.class);
        if (constr != null) {
          dir = (Directory) constr.newInstance(path);
        } else {
          constr = implClazz.getConstructor(Path.class, LockFactory.class);
          dir = (Directory) constr.newInstance(path, null);
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid directory implementation class: " + dirImpl);
      }
    }
    return dir;
  }

  public static void close(Directory dir) {
    try {
      if (dir != null) {
        dir.close();
        logger.info("Directory successfully closed.");
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static void close(IndexReader reader) {
    try {
      if (reader != null) {
        reader.close();
        logger.info("IndexReader successfully closed.");
        if (reader instanceof DirectoryReader) {
          Directory dir = ((DirectoryReader) reader).directory();
          dir.close();
          logger.info("Directory successfully closed.");
        }
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static IndexWriter createWriter(@Nonnull Directory dir, Analyzer analyzer, boolean useCompound, boolean keepAllCommits) throws IOException {
    return createWriter(dir, analyzer, useCompound, keepAllCommits, null);
  }

  public static IndexWriter createWriter(@Nonnull Directory dir, Analyzer analyzer, boolean useCompound, boolean keepAllCommits,
                                         @Nullable PrintStream ps) throws IOException {
    if (analyzer == null) {
      analyzer = new WhitespaceAnalyzer();
    }
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setUseCompoundFile(useCompound);
    if (ps != null) {
      config.setInfoStream(ps);
    }
    if (keepAllCommits) {
      config.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);
    } else {
      config.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
    }
    return new IndexWriter(dir, config);
  }

  public static void optimizeIndex(@Nonnull IndexWriter writer, boolean expunge, int maxNumSegments) throws IOException {
    if (expunge) {
      writer.forceMergeDeletes(true);
    } else {
      writer.forceMerge(maxNumSegments, true);
    }
  }

  public static CheckIndex.Status checkIndex(@Nonnull Directory dir, @Nullable PrintStream ps) throws IOException {
    try (CheckIndex ci = new CheckIndex(dir)) {
      if (ps != null) {
        ci.setInfoStream(ps);
      }
      return ci.checkIndex();
    }
  }

  public static void tryRepairIndex(@Nonnull Directory dir, @Nonnull CheckIndex.Status st, @Nullable PrintStream ps) throws IOException {
    try (CheckIndex ci = new CheckIndex(dir)) {
      if (ps != null) {
        ci.setInfoStream(ps);
      }
      ci.exorciseIndex(st);
    }
  }

  public static String getIndexFormat(@Nonnull Directory dir) throws IOException {
    return new SegmentInfos.FindSegmentsFile<String>(dir) {
      @Override
      protected String doBody(String segmentFileName) throws IOException {
        String format = "unknown";
        try (IndexInput in = dir.openInput(segmentFileName, IOContext.READ)) {
          if (CodecUtil.CODEC_MAGIC == in.readInt()) {
            int actualVersion = CodecUtil.checkHeaderNoMagic(in, "segments", SegmentInfos.VERSION_53, Integer.MAX_VALUE);
            if (actualVersion == SegmentInfos.VERSION_53) {
              format = "Lucene 5.3 or later";
            } else if (actualVersion == SegmentInfos.VERSION_70) {
              format = "Lucene 7.0 or later";
            } else if (actualVersion > SegmentInfos.VERSION_70) {
              format = "Lucene 7.0 or later (UNSUPPORTED)";
            }
          } else {
            format = "Lucene 5.x or prior (UNSUPPORTED)";
          }
        }
        return format;
      }
    }.run();
  }

  public static String getCommitUserData(@Nonnull IndexCommit ic) throws IOException {
    Map<String, String> userDataMap = ic.getUserData();
    if (userDataMap != null) {
      return userDataMap.toString();
    } else {
      return "--";
    }
  }

  public static Map<String, Long> countTerms(IndexReader reader, Collection<String> fields) throws IOException {
    Map<String, Long> res = new HashMap<>();
    for (String field : fields) {
      if (!res.containsKey(field)) {
        res.put(field, 0L);
      }
      Terms terms = MultiFields.getTerms(reader, field);
      if (terms != null) {
        TermsEnum te = terms.iterator();
        while (te.next() != null) {
          res.put(field, res.get(field) + 1);
        }
      }
    }
    return res;
  }

  public static Bits getLiveDocs(IndexReader reader) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getLiveDocs();
    } else {
      return MultiFields.getLiveDocs(reader);
    }
  }

  public static FieldInfos getFieldInfos(IndexReader reader) {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getFieldInfos();
    } else {
      return MultiFields.getMergedFieldInfos(reader);
    }
  }

  public static FieldInfo getFieldInfo(IndexReader reader, String fieldName) {
    return getFieldInfos(reader).fieldInfo(fieldName);
  }

  public static Collection<String> getFieldNames(IndexReader reader) {
    return StreamSupport.stream(getFieldInfos(reader).spliterator(), false)
        .map(f -> f.name)
        .collect(Collectors.toList());
  }

  public static Terms getTerms(IndexReader reader, String field) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).terms(field);
    } else {
      return MultiFields.getTerms(reader, field);
    }
  }

  public static BinaryDocValues getBinaryDocValues(IndexReader reader, String field) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getBinaryDocValues(field);
    } else {
      return MultiDocValues.getBinaryValues(reader, field);
    }
  }

  public static NumericDocValues getNumericDocValues(IndexReader reader, String field) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getNumericDocValues(field);
    } else {
      return MultiDocValues.getNumericValues(reader, field);
    }
  }

  public static SortedNumericDocValues getSortedNumericDocValues(IndexReader reader, String field) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getSortedNumericDocValues(field);
    } else {
      return MultiDocValues.getSortedNumericValues(reader, field);
    }
  }

  public static SortedDocValues getSortedDocValues(IndexReader reader, String field) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getSortedDocValues(field);
    } else {
      return MultiDocValues.getSortedValues(reader, field);
    }
  }

  public static SortedSetDocValues getSortedSetDocvalues(IndexReader reader, String field) throws IOException {
    if (reader instanceof LeafReader) {
      return ((LeafReader) reader).getSortedSetDocValues(field);
    } else {
      return MultiDocValues.getSortedSetValues(reader, field);
    }
  }

  private IndexUtils() {
  }
}
