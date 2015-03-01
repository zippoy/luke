package org.apache.lucene.luke.ui;

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

import org.apache.lucene.luke.core.decoders.Decoder;

public class FieldsTableRow {
  private String name;
  private String termCount;
  private String percent;
  private Decoder decoder;

  private LukeWindow.LukeMediator lukeMediator;

  public FieldsTableRow(LukeWindow.LukeMediator lukeMediator) {
    this.lukeMediator = lukeMediator;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getTermCount() {
    return termCount;
  }
  public void setTermCount(String termCount) {
    this.termCount = termCount;
  }
  public String getPercent() {
    return percent;
  }
  public void setPercent(String percent) {
    this.percent = percent;
  }
  public Decoder getDecoder() {
    return decoder;
  }
  public void setDecoder(Decoder decoder) {
    this.decoder = decoder;
    this.lukeMediator.getDecoders().put(name, decoder);
  }

}
