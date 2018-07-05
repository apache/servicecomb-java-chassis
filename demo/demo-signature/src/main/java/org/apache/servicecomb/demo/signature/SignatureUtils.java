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

package org.apache.servicecomb.demo.signature;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.netflix.config.DynamicStringListProperty;

public class SignatureUtils {
  private static final DynamicStringListProperty PARAM_NAMES_PROPERTY =
      new DynamicStringListProperty("servicecomb.demo.signature.param-names", Arrays.asList("userId"));

  private static List<String> paramNames = PARAM_NAMES_PROPERTY.get();

  static {
    PARAM_NAMES_PROPERTY.addCallback(() -> {
      List<String> tmpNames = PARAM_NAMES_PROPERTY.get();
      tmpNames.sort(Comparator.naturalOrder());
      paramNames = tmpNames;
    });
  }

  public static String genSignature(HttpServletRequestEx requestEx) {
    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putString(requestEx.getRequestURI(), StandardCharsets.UTF_8);
    for (String paramName : paramNames) {
      String paramValue = requestEx.getHeader(paramName);
      if (paramValue != null) {
        hasher.putString(paramName, StandardCharsets.UTF_8);
        hasher.putString(paramValue, StandardCharsets.UTF_8);
        System.out.printf("%s %s\n", paramName, paramValue);
      }
    }

    if (!StringUtils.startsWithIgnoreCase(requestEx.getContentType(), MediaType.APPLICATION_FORM_URLENCODED)) {
      byte[] bytes = requestEx.getBodyBytes();
      if (bytes != null) {
        hasher.putBytes(bytes, 0, requestEx.getBodyBytesLength());
      }
    }

    return hasher.hash().toString();
  }

  public static String genSignature(HttpServletResponseEx responseEx) {
    Hasher hasher = Hashing.sha256().newHasher();
    byte[] bytes = responseEx.getBodyBytes();
    if (bytes != null) {
      hasher.putBytes(bytes, 0, responseEx.getBodyBytesLength());
    }

    return hasher.hash().toString();
  }
}
