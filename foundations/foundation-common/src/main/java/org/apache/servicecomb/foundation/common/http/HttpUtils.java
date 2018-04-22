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
package org.apache.servicecomb.foundation.common.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.util.StringUtils;

public final class HttpUtils {
  private HttpUtils() {
  }

  /**
   * paramName is not case sensitive
   * @param headerValue example: attachment;filename=a.txt
   * 
   */
  // 
  public static String parseParamFromHeaderValue(String headerValue, String paramName) {
    if (StringUtils.isEmpty(headerValue)) {
      return null;
    }

    for (String value : headerValue.split(";")) {
      int idx = value.indexOf('=');
      if (idx == -1) {
        continue;
      }

      if (paramName.equalsIgnoreCase(value.substring(0, idx))) {
        return value.substring(idx + 1);
      }
    }
    return null;
  }

  public static String uriEncode(String value) {
    return uriEncode(value, StandardCharsets.UTF_8.name());
  }

  public static String uriEncode(String value, String enc) {
    try {
      return URLEncoder.encode(value, enc).replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(String.format("uriEncode failed, value=\"%s\", enc=\"%s\".", value, enc), e);
    }
  }

  public static String uriDecode(String value) {
    return uriDecode(value, StandardCharsets.UTF_8.name());
  }

  public static String uriDecode(String value, String enc) {
    try {
      return URLDecoder.decode(value, enc);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(String.format("uriDecode failed, value=\"%s\", enc=\"%s\".", value, enc), e);
    }
  }

  public static String parseFileNameFromHeaderValue(String headerValue) {
    String fileName = parseParamFromHeaderValue(headerValue, "filename");
    fileName = StringUtils.isEmpty(fileName) ? "default" : fileName;
    fileName = uriDecode(fileName);
    return new File(fileName).getName();
  }
}
