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

package org.apache.servicecomb.foundation.common.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.vertx.core.json.JsonObject;

public class RestObjectMapper extends AbstractRestObjectMapper {

  private static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
    @Override
    public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeObject(value.getMap());
    }
  }

  private static final long serialVersionUID = -8158869347066287575L;

  private static final JavaType STRING_JAVA_TYPE = TypeFactory.defaultInstance().constructType(String.class);

  public RestObjectMapper() {
    getFactory().disable(Feature.AUTO_CLOSE_SOURCE);
    // Enable features that can tolerance errors and not enable those make more constraints for compatible reasons.
    // Developers can use validation api to do more checks.
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    // no view annotations shouldn't be included in JSON
    disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    SimpleModule module = new SimpleModule();
    // custom types
    module.addSerializer(JsonObject.class, new JsonObjectSerializer());
    registerModule(module);
    registerModule(new JavaTimeModule());
  }

  @Override
  public String convertToString(Object value) throws Exception {
    return convertValue(value, STRING_JAVA_TYPE);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
    // After jackson 2.10.*, will by pass the following check when convert value. But this is useful
    // for java chassis applications and do not need to convert to keep performance. So add the check here.(conversion is
    // not necessary and will cause some trouble in some user applications that depend on this)
    if (fromValue == null) {
      return null;
    } else {
      Class<?> targetType = toValueType.getRawClass();
      if (targetType != Object.class
          && !toValueType.hasGenericTypes()
          && targetType.isAssignableFrom(fromValue.getClass())) {
        return (T) fromValue;
      }
    }

    return super.convertValue(fromValue, toValueType);
  }
}
