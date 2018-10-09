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

package org.apache.lucene.luke.models.commits;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import java.util.Map;

// Subclasses of CompressingCodec do not store any segment attributes. Ignore those.
@LuceneTestCase.SuppressCodecs({
    "DummyCompressingStoredFields", "HighCompressionCompressingStoredFields", "FastCompressingStoredFields", "FastDecompressionCompressingStoredFields"
})
public class CommitsImplSegAttsTest extends CommitsTestBase {

  @Test
  public void testGetSegmentAttributes() {
    CommitsImpl commits = new CommitsImpl(reader, indexDir.toString());
    Map<String, String> attributes = commits.getSegmentAttributes(1, "_0");
    assertTrue(attributes.size() > 0);
  }

  @Test
  public void testGetSegmentAttributes_generation_notfound() {
    CommitsImpl commits = new CommitsImpl(reader, indexDir.toString());
    Map<String, String> attributes = commits.getSegmentAttributes(3, "_0");
    assertTrue(attributes.isEmpty());
  }

  @Test
  public void testGetSegmentAttributes_invalid_name() {
    CommitsImpl commits = new CommitsImpl(reader, indexDir.toString());
    Map<String, String> attributes = commits.getSegmentAttributes(1, "xxx");
    assertTrue(attributes.isEmpty());
  }

}
