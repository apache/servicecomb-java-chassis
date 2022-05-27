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

package org.apache.servicecomb.http.client.common;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class HttpUtils {
  private static final String ALGORITHM_HMACSHA256 = "HmacSHA256";

  private static final ObjectMapper MAPPER = new MessageObjectMapper();

  public static <T> T deserialize(String content, Class<T> clazz) throws IOException {
    return MAPPER.readValue(content, clazz);
  }

  public static <T> T deserialize(String content, TypeReference<T> clazz) throws IOException {
    return MAPPER.readValue(content, clazz);
  }

  public static String serialize(Object value) throws IOException {
    return MAPPER.writeValueAsString(value);
  }

  public static JsonNode readTree(String content) throws IOException {
    return MAPPER.readTree(content);
  }

  public static String encodeURLParam(String value) throws IOException {
    if (value == null) {
      return "";
    }
    return URLEncoder.encode(value, "UTF-8");
  }

  public static String sha256Encode(String key, String data) throws Exception {
    Mac mac = Mac.getInstance(ALGORITHM_HMACSHA256);
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
        ALGORITHM_HMACSHA256);
    mac.init(secretKey);
    return Hex.encodeHexString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
  }
}
