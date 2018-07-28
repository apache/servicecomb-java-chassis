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
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.utils.json.JavaxServletPartDeserializer;
import org.apache.servicecomb.foundation.common.utils.json.JavaxServletPartSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class JsonUtils {
  public static final ObjectMapper OBJ_MAPPER;

  static {
    OBJ_MAPPER = new ObjectMapper();
    OBJ_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    OBJ_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    SimpleModule partDeserializeModule = new SimpleModule("partDeserializeModule",
        new Version(0, 0, 1, null, "javax.servlet", "javax.servlet-api")
    );
    partDeserializeModule.addSerializer(Part.class, new JavaxServletPartSerializer());
    partDeserializeModule.addDeserializer(Part.class, new JavaxServletPartDeserializer());
    OBJ_MAPPER.registerModule(partDeserializeModule);
  }

  private JsonUtils() {
  }

  public static <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
    return OBJ_MAPPER.readValue(src, valueType);
  }

  public static <T> T readValue(InputStream is, Class<T> valueType) throws IOException {
    return OBJ_MAPPER.readValue(is, valueType);
  }

  public static <T> T readValue(InputStream is, JavaType valueType) throws IOException {
    return OBJ_MAPPER.readValue(is, valueType);
  }

  public static byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
    return OBJ_MAPPER.writeValueAsBytes(value);
  }

  public static String writeValueAsString(Object value) throws JsonProcessingException {
    return OBJ_MAPPER.writeValueAsString(value);
  }

  public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
    return OBJ_MAPPER.convertValue(fromValue, toValueType);
  }

  public static void writeValue(OutputStream out, Object value) throws IOException {
    OBJ_MAPPER.writeValue(out, value);
  }
}
