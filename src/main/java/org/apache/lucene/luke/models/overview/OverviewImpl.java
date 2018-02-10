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

package org.apache.lucene.luke.models.overview;

import com.google.inject.Inject;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.luke.models.BaseModel;
import org.apache.lucene.luke.models.LukeException;
import org.apache.lucene.luke.util.IndexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OverviewImpl extends BaseModel implements Overview {

  private static Logger logger = LoggerFactory.getLogger(OverviewImpl.class);

  private String indexPath;

  private TermCounts termCounts;

  private TopTerms topTerms;

  @Inject
  OverviewImpl() {
    this.termCounts = new TermCountsImpl();
    this.topTerms = new TopTermsImpl();
  }

  @Override
  public void reset(@Nonnull IndexReader reader, @Nonnull String indexPath) throws LukeException {
    super.reset(reader);
    this.indexPath = indexPath;
    this.termCounts.reset(reader);
    this.topTerms.reset(reader);
  }

  @Override
  public String getIndexPath() {
    return indexPath;
  }

  @Override
  public Integer getNumFields() {
    return IndexUtils.getFieldInfos(reader).size();
  }

  @Override
  public Integer getNumDocuments() {
    return reader.numDocs();
  }

  @Override
  public Long getNumTerms() throws LukeException {
    try {
      return termCounts.numTerms();
    } catch (IOException e) {
      String msg = "Num terms not available";
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public Boolean hasDeletions() {
    return reader.hasDeletions();
  }

  @Override
  public Integer getNumDeletedDocs() {
    return reader.numDeletedDocs();
  }

  @Override
  public Optional<Boolean> isOptimized() {
    if (commit != null) {
      return Optional.of(commit.getSegmentCount() == 1);
    }
    return Optional.empty();
  }

  @Override
  public Optional<Long> getIndexVersion() {
    if (reader instanceof DirectoryReader) {
      return Optional.of(((DirectoryReader) reader).getVersion());
    }
    return Optional.empty();
  }

  @Override
  public String getIndexFormat() throws LukeException {
    if (dir == null) {
      return "Index format not available.";
    }
    try {
      return IndexUtils.getIndexFormat(dir);
    } catch (IOException e) {
      String msg = "Index format not available.";
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public String getDirImpl() {
    if (dir == null) {
      return "";
    }
    return dir.getClass().getName();
  }

  @Override
  public Optional<String> getCommitDescription() {
    if (commit != null) {
      return Optional.of(
          commit.getSegmentsFileName()
              + " (generation=" + commit.getGeneration()
              + ", segs=" + commit.getSegmentCount() + ")");
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> getCommitUserData() throws LukeException {
    if (commit != null) {
      try {
        return Optional.of(IndexUtils.getCommitUserData(commit));
      } catch (IOException e) {
        String msg = "Commit user data not available.";
        logger.error(msg, e);
        throw new LukeException(msg, e);
      }
    }
    return Optional.empty();
  }

  @Override
  public Map<String, Long> getSortedTermCounts(@Nullable TermCounts.Order order) throws LukeException {
    if (order == null) {
      order = TermCounts.Order.COUNT_DESC;
    }
    try {
      return termCounts.sortedTermCounts(order);
    } catch (IOException e) {
      String msg = "Term counts not available.";
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

  @Override
  public List<TermStats> getTopTerms(@Nonnull String field, int numTerms) throws LukeException {
    try {
      return topTerms.getTopTerms(field, numTerms);
    } catch (Exception e) {
      String msg = "Top terms not available.";
      logger.error(msg, e);
      throw new LukeException(msg, e);
    }
  }

}
