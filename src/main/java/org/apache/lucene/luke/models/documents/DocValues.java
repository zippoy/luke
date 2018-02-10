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

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.util.BytesRef;

import java.util.List;

public class DocValues {
  private DocValuesType dvType;
  private List<BytesRef> values;
  private List<Long> numericValues;

  static DocValues of(DocValuesType dvType, List<BytesRef> values, List<Long> numericValues) {
    DocValues dv = new DocValues();
    dv.dvType = dvType;
    dv.values = values;
    dv.numericValues = numericValues;
    return dv;
  }

  public DocValuesType getDvType() {
    return dvType;
  }

  public List<BytesRef> getValues() {
    return values;
  }

  public List<Long> getNumericValues() {
    return numericValues;
  }

  private DocValues() {
  }
}
