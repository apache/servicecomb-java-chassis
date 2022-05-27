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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class URLEndPoint {
  private static final String SSL_ENABLED_KEY = "sslEnabled";

  private static final String HTTP_KEY = "http://";

  private static final String HTTPS_KEY = "https://";

  private final boolean sslEnabled;

  private final Map<String, List<String>> querys;

  private final String hostOrIp;

  private final int port;

  public URLEndPoint(String endpoint) {
    URI uri = URI.create(endpoint);
    hostOrIp = uri.getHost();
    if (uri.getPort() < 0) {
      throw new IllegalArgumentException("port not specified.");
    }
    port = uri.getPort();
    querys = splitQuery(uri);
    if (endpoint.contains(HTTPS_KEY)) {
      sslEnabled = true;
    } else {
      sslEnabled = Boolean.parseBoolean(getFirst(SSL_ENABLED_KEY));
    }
  }

  public static Map<String, List<String>> splitQuery(URI uri) {
    final Map<String, List<String>> queryPairs = new LinkedHashMap<>();
    List<NameValuePair> pairs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
    for (NameValuePair pair : pairs) {
      List<String> list = queryPairs.computeIfAbsent(pair.getName(), name -> new ArrayList<>());
      list.add(pair.getValue());
    }
    return queryPairs;
  }

  public String getFirst(String key) {
    List<String> values = querys.get(key);
    if (values == null) {
      return null;
    }
    return values.get(0);
  }

  @Override
  public String toString() {
    if (sslEnabled) {
      return HTTPS_KEY + hostOrIp + ":" + port;
    }
    return HTTP_KEY + hostOrIp + ":" + port;
  }
}
