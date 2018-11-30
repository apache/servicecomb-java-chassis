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

package org.apache.servicecomb.common.rest.codec;

import java.io.IOException;
import java.text.FieldPosition;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.base.DoSFix;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.vertx.core.json.JsonObject;

public class RestObjectMapper extends AbstractRestObjectMapper {
  static {
    DoSFix.init();
  }

  private static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
    @Override
    public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeObject(value.getMap());
    }
  }

  private static final long serialVersionUID = -8158869347066287575L;

  private static final JavaType STRING_JAVA_TYPE = TypeFactory.defaultInstance().constructType(String.class);

  @SuppressWarnings("deprecation")
  public RestObjectMapper() {
    super(DoSFix.createJsonFactory());

    // swagger中要求date使用ISO8601格式传递，这里与之做了功能绑定，这在cse中是没有问题的
    setDateFormat(new com.fasterxml.jackson.databind.util.ISO8601DateFormat() {
      private static final long serialVersionUID = 7798938088541203312L;

      // to support millis
      @Override
      public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        String value = com.fasterxml.jackson.databind.util.ISO8601Utils.format(date, true);
        toAppendTo.append(value);
        return toAppendTo;
      }
    });

    getFactory().disable(Feature.AUTO_CLOSE_SOURCE);
    // Enable features that can tolerance errors and not enable those make more constraints for compatible reasons.
    // Developers can use validation api to do more checks.
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    SimpleModule module = new SimpleModule();
    // custom types
    module.addSerializer(JsonObject.class, new JsonObjectSerializer());
    registerModule(module);
  }

  @Override
  public String convertToString(Object value) throws Exception {
    return convertValue(value, STRING_JAVA_TYPE);
  }

  @Override
  public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
    return super.convertValue(fromValue, toValueType);
  }
}
