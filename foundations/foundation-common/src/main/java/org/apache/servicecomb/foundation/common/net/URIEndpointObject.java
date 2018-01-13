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

package org.apache.servicecomb.foundation.common.net;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * transport公共的Endpoint Object，当transport使用URI表示的时候，可以转化为这个对象。
 */
public class URIEndpointObject extends IpPort {
  private static final String SSL_ENABLED_KEY = "sslEnabled";

  private boolean sslEnabled;

  private Map<String, List<String>> querys;

  public URIEndpointObject(String endpoint) {
    URI uri = URI.create(endpoint);
    setHostOrIp(uri.getHost());
    if (uri.getPort() < 0) {
      // do not use default port
      throw new IllegalArgumentException("port not specified.");
    }
    setPort(uri.getPort());
    querys = splitQuery(uri);
    sslEnabled = Boolean.parseBoolean(getFirst(SSL_ENABLED_KEY));
  }

  public static Map<String, List<String>> splitQuery(URI uri) {
    final Map<String, List<String>> queryPairs = new LinkedHashMap<>();
    List<NameValuePair> pairs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.name());
    for (NameValuePair pair : pairs) {
      List<String> list = queryPairs.computeIfAbsent(pair.getName(), name -> {
        return new ArrayList<>();
      });
      list.add(pair.getValue());
    }
    return queryPairs;
  }

  public boolean isSslEnabled() {
    return sslEnabled;
  }

  public List<String> getQuery(String key) {
    return querys.get(key);
  }

  public String getFirst(String key) {
    List<String> values = querys.get(key);
    // it's impossible that values is not null and size is 0
    if (values == null) {
      return null;
    }

    return values.get(0);
  }
}
