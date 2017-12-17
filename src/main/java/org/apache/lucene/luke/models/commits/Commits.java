package org.apache.lucene.luke.models.commits;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.store.Directory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Commits {

  void reset(Directory dir, String indexPath);

  void reset(IndexReader reader, String indexPath) throws LukeException;

  Optional<List<Commit>> listCommits() throws LukeException;

  Optional<Commit> getCommit(long commitGen) throws LukeException;

  Optional<List<File>> getFiles(long commitGen) throws LukeException;

  Optional<List<Segment>> getSegments(long commitGen) throws LukeException;

  Optional<Map<String, String>> getSegmentAttributes(long commitGen, String name) throws LukeException;

  Optional<Map<String, String>> getSegmentDiagnostics(long commitGen, String name) throws LukeException;

  Optional<Codec> getSegmentCodec(long commitGen, String name) throws LukeException;
}
