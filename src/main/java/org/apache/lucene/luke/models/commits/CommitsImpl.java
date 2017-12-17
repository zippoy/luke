package org.apache.lucene.luke.models.commits;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.luke.models.BaseModel;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class CommitsImpl extends BaseModel implements Commits {

  private static final Logger logger = LoggerFactory.getLogger(CommitsImpl.class);

  private String indexPath;

  private Map<Long, IndexCommit> commitMap;

  @Override
  public void reset(Directory dir, String indexPath) {
    super.reset(dir);
    this.indexPath = indexPath;
    this.commitMap = null;
  }

  @Override
  public void reset(IndexReader reader, String indexPath) throws LukeException {
    super.reset(reader);
    this.indexPath = indexPath;
    this.commitMap = null;
  }

  @Override
  public Optional<List<Commit>> listCommits() throws LukeException {
    List<Commit> commits = getCommitMap().values().stream()
        .map(Commit::of)
        .collect(Collectors.toList());
    Collections.reverse(commits);
    return Optional.of(commits);
  }

  @Override
  public Optional<Commit> getCommit(long commitGen) throws LukeException {
    IndexCommit ic = getCommitMap().get(commitGen);
    if (ic == null) {
      String msg = String.format("Commit generation %d not exists.", commitGen);
      logger.warn(msg);
      return Optional.empty();
    }

    return Optional.of(Commit.of(ic));
  }

  @Override
  public Optional<List<File>> getFiles(long commitGen) throws LukeException {
    IndexCommit ic = getCommitMap().get(commitGen);
    if (ic == null) {
      String msg = String.format("Commit generation %d not exists.", commitGen);
      logger.warn(msg);
      return Optional.empty();
    }

    try {
      List<File> files = ic.getFileNames().stream()
          .map(name -> File.of(indexPath, name))
          .sorted(Comparator.comparing(File::getFileName))
          .collect(Collectors.toList());
      return Optional.of(files);
    } catch (IOException e) {
      String msg = String.format("Failed to load files for commit generation %d", commitGen);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Optional<List<Segment>> getSegments(long commitGen) throws LukeException {
    try {
      SegmentInfos infos = findSegmentInfos(commitGen);
      if (infos == null) {
        return Optional.empty();
      }
      List<Segment> segments = infos.asList().stream()
          .map(Segment::of)
          .sorted(Comparator.comparing(Segment::getName))
          .collect(Collectors.toList());
      return Optional.of(segments);
    } catch (IOException e) {
      String msg = String.format("Failed to load segment infos for commit generation %d", commitGen);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Optional<Map<String, String>> getSegmentAttributes(long commitGen, String name) throws LukeException {
    try {
      SegmentInfos infos = findSegmentInfos(commitGen);
      if (infos == null) {
        return Optional.empty();
      }

      return infos.asList().stream()
          .filter(seg -> seg.info.name.equals(name))
          .findAny()
          .map(seg -> seg.info.getAttributes());
    } catch (IOException e) {
      String msg = String.format("Failed to load segment infos for commit generation %d", commitGen);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Optional<Map<String, String>> getSegmentDiagnostics(long commitGen, String name) throws LukeException {
    try {
      SegmentInfos infos = findSegmentInfos(commitGen);
      if (infos == null) {
        return Optional.empty();
      }

      return infos.asList().stream()
          .filter(seg -> seg.info.name.equals(name))
          .findAny()
          .map(seg -> seg.info.getDiagnostics());
    } catch (IOException e) {
      String msg = String.format("Failed to load segment infos for commit generation %d", commitGen);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Optional<Codec> getSegmentCodec(long commitGen, String name) throws LukeException {
    try {
      SegmentInfos infos = findSegmentInfos(commitGen);
      if (infos == null) {
        return Optional.empty();
      }

      return infos.asList().stream()
          .filter(seg -> seg.info.name.equals(name))
          .findAny()
          .map(seg -> seg.info.getCodec());
    } catch (IOException e) {
      String msg = String.format("Failed to load segment infos for commit generation %d", commitGen);
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  private Map<Long, IndexCommit> getCommitMap() throws LukeException {
    if (dir == null) {
      return Collections.emptyMap();
    }
    if (commitMap == null) {
      try {
        List<IndexCommit> indexCommits = DirectoryReader.listCommits(dir);
        commitMap = indexCommits.stream()
            .collect(Collectors.toMap(IndexCommit::getGeneration, UnaryOperator.identity()));
      } catch (IOException e) {
        String msg = "Failed to get commits list.";
        logger.error(msg, e);
        throw new LukeException(msg, e);
      }
    }
    return commitMap;
  }

  private SegmentInfos findSegmentInfos(long commitGen) throws LukeException, IOException {
    IndexCommit ic = getCommitMap().get(commitGen);
    if (ic == null) {
      return null;
    }
    String segmentFile = ic.getSegmentsFileName();
    return SegmentInfos.readCommit(dir, segmentFile);
  }

  static String toDisplaySize(long size) {
    if (size < 1024) {
      return String.valueOf(size) + " B";
    } else if (size < 1048576) {
      return String.valueOf(size / 1024) + " KB";
    } else {
      return String.valueOf(size / 1048576) + " MB";
    }
  }
}
