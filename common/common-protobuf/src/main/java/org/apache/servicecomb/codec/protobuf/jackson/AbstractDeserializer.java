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

package org.apache.servicecomb.codec.protobuf.jackson;

import java.io.IOException;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.codec.AbstractFieldCodec.ReaderHelpData;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public abstract class AbstractDeserializer extends JsonDeserializer<Object> {
  protected Map<String, ReaderHelpData> readerHelpDataMap;

  public AbstractDeserializer(Map<String, ReaderHelpData> readerHelpDataMap) {
    this.readerHelpDataMap = readerHelpDataMap;
  }

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    Object result = createResult();
    for (String fieldName = p.nextFieldName(); fieldName != null; fieldName = p.nextFieldName()) {
      // p实际是ProtobufParser，其内部是可以直接取到proto field的，理论上可以根据id来索引
      // 可是field默认没暴露出来，所以，直接用name索引了
      ReaderHelpData helpData = readerHelpDataMap.get(fieldName);
      if (helpData == null) {
        continue;
      }

      JsonToken t = p.nextToken();
      // Note: must handle null explicitly here; value deserializers won't
      Object value = null;
      if (t == JsonToken.VALUE_NULL) {
        value = helpData.getDeser().getNullValue(ctxt);
      } else {
        value = helpData.getDeser().deserialize(p, ctxt);
      }

      result = updateResult(result, value, helpData);
    }

    return result;
  }

  protected abstract Object createResult();

  protected abstract Object updateResult(Object result, Object value, ReaderHelpData helpData);
}
