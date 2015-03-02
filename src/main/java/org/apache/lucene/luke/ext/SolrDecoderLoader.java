package org.apache.lucene.luke.ext;

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

import org.apache.lucene.document.Field;
import org.apache.lucene.luke.core.ClassFinder;
import org.apache.lucene.luke.core.decoders.Decoder;
import org.apache.lucene.luke.core.decoders.DecoderLoader;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.solr.schema.FieldType;

import java.util.ArrayList;
import java.util.List;

public class SolrDecoderLoader implements DecoderLoader {
  private static final String solr_prefix = "org.apache.solr.schema.";

  @Override
  public List<Decoder> loadDecoders() {
    List<Decoder> decoders = new ArrayList<Decoder>();
    try {
      Class[] classes = ClassFinder.getInstantiableSubclasses(FieldType.class);
      if (classes == null || classes.length == 0) {
        throw new ClassNotFoundException("Missing Solr types???");
      }
      for (Class cls : classes) {
        FieldType ft = (FieldType) cls.newInstance();
        if (cls.getName().startsWith(solr_prefix)) {
          String name = "solr." + cls.getName().substring(solr_prefix.length());
          decoders.add(new SolrDecoder(name, ft));
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return decoders;
  }
}

class SolrDecoder implements Decoder {
  private String name;
  private FieldType fieldType;

  public SolrDecoder(String name, FieldType fieldType) {
    this.name = name;
    this.fieldType = fieldType;
  }

  @Override
  public String decodeTerm(String fieldName, Object value) throws Exception {
    return fieldType.indexedToReadable(value.toString());
  }

  public String decodeStored(String fieldName, Field value)
    throws Exception {
    return fieldType.storedToReadable(value);
  }

  public String toString() {
    return name;
  }

}

