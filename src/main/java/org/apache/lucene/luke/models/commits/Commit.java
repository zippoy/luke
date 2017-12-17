package org.apache.lucene.luke.models.commits;

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.luke.util.IndexUtils;

import java.io.IOException;

public class Commit {
  private long generation;
  private boolean isDeleted;
  private int segCount;
  private String userData;

  static Commit of(IndexCommit ic) {
    Commit commit = new Commit();
    commit.generation = ic.getGeneration();
    commit.isDeleted = ic.isDeleted();
    commit.segCount = ic.getSegmentCount();
    try {
      commit.userData = IndexUtils.getCommitUserData(ic);
    } catch (IOException e) {
    }
    return commit;
  }

  public long getGeneration() {
    return generation;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public int getSegCount() {
    return segCount;
  }

  public String getUserData() {
    return userData;
  }

  private Commit() {
  }
}
