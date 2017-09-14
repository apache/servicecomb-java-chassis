/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.signature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.netflix.config.DynamicStringListProperty;

import io.servicecomb.foundation.vertx.VertxUtils;
import io.vertx.core.buffer.Buffer;

public class SignatureUtils {
  private static final DynamicStringListProperty PARAM_NAMES_PROPERTY =
      new DynamicStringListProperty("servicecomb.signature.param-names", Collections.emptyList());

  private static List<String> paramNames = PARAM_NAMES_PROPERTY.get();
  static {
    PARAM_NAMES_PROPERTY.addCallback(() -> {
      List<String> tmpNames = PARAM_NAMES_PROPERTY.get();;
      tmpNames.sort((n1, n2) -> {
        return n1.compareTo(n2);
      });
      paramNames = tmpNames;
    });
  }

  public static String genSignature(String path, Map<String, String> params, Buffer bodyBuffer) {
    byte[] bodyBytes = null;
    int length = 0;
    if (bodyBuffer != null) {
      bodyBytes = VertxUtils.getBytesFast(bodyBuffer);
      length = bodyBuffer.length();
    }
    return genSignature(path, params, bodyBytes, length);
  }

  public static String genSignature(String path, Map<String, String> params, HttpServletRequest request)
      throws IOException {
    byte[] bodyBytes = VertxUtils.getBytesFast(request.getInputStream());
    return genSignature(path, params, bodyBytes, bodyBytes.length);
  }

  public static String genSignature(String path, Map<String, String> params, byte[] bodyBytes, int bodyByteLength) {
    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putString(path, StandardCharsets.UTF_8);
    for (String paramName : paramNames) {
      String paramValue = params.get(paramName);
      if (paramValue != null) {
        hasher.putString(paramName, StandardCharsets.UTF_8);
        hasher.putString(paramValue, StandardCharsets.UTF_8);
        System.out.printf("%s %s\n", paramName, paramValue);
      }
    }
    if (bodyBytes != null) {
      hasher.putBytes(bodyBytes, 0, bodyByteLength);
    }

    return hasher.hash().toString();
  }

  public static String genSignature(Buffer bodyBuffer) {
    byte[] bodyBytes = null;
    int bodyByteLength = 0;
    if (bodyBuffer != null) {
      bodyBytes = VertxUtils.getBytesFast(bodyBuffer);
      bodyByteLength = bodyBuffer.length();
    }

    return genSignature(bodyBytes, bodyByteLength);
  }

  public static String genSignature(byte[] bodyBytes, int bodyByteLength) {
    Hasher hasher = Hashing.sha256().newHasher();
    if (bodyBytes != null) {
      hasher.putBytes(bodyBytes, 0, bodyByteLength);
    }
    return hasher.hash().toString();
  }
}
