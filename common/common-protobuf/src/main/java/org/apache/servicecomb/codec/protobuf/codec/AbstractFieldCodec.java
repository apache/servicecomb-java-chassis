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

package org.apache.servicecomb.codec.protobuf.codec;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.jackson.CseObjectReader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufField;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;

public class AbstractFieldCodec extends AbstractCodec {
  public static class ReaderHelpData {
    // 在reader返回的Object[]中的下标
    private int index;

    private JsonDeserializer<Object> deser;

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public JsonDeserializer<Object> getDeser() {
      return deser;
    }

    public void setDeser(JsonDeserializer<Object> deser) {
      this.deser = deser;
    }
  }

  // key为field name
  protected Map<String, ReaderHelpData> readerHelpDataMap = new HashMap<>();

  @Override
  public void init(ProtobufSchema schema, Type... types) {
    initFieldMap(schema, types);
  }

  private void initFieldMap(ProtobufSchema schema, Type[] types) {
    Iterator<ProtobufField> fieldIter = schema.getRootType().fields().iterator();
    for (int idx = 0; idx < schema.getRootType().getFieldCount(); idx++) {
      JavaType type = TypeFactory.defaultInstance().constructType(types[idx]);
      ProtobufField field = fieldIter.next();

      ReaderHelpData helpData = new ReaderHelpData();
      helpData.index = idx;
      helpData.deser = ((CseObjectReader) reader).findDeserializer(type);

      readerHelpDataMap.put(field.name, helpData);
    }
  }

  public ReaderHelpData findInfo(String name) {
    return readerHelpDataMap.get(name);
  }
}
