/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
  public static final ObjectMapper OBJ_MAPPER;

  static {
    //如果待转换的字符串比较随机，这个缓存很容易就填满，会导致同步清空缓存，对性能有影响
    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false);
    OBJ_MAPPER = new ObjectMapper(jsonFactory);
  }

  //    static
  //    {
  //        //设置反序列化时忽略json字符串中存在而Java对象实际没有的属性
  //        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  //    }

  private JsonUtils() {
  }

  public static <T> T readValue(byte[] src,
      Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
    return OBJ_MAPPER.readValue(src, valueType);
  }

  public static <T> T readValue(InputStream is,
      Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
    return OBJ_MAPPER.readValue(is, valueType);
  }

  public static <T> T readValue(InputStream is,
      JavaType valueType) throws JsonParseException, JsonMappingException, IOException {
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

  public static void writeValue(OutputStream out,
      Object value) throws JsonGenerationException, JsonMappingException, IOException {
    OBJ_MAPPER.writeValue(out, value);
  }
}
