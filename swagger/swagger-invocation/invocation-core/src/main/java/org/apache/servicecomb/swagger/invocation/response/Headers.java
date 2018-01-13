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
package org.apache.servicecomb.swagger.invocation.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Headers {
  private Map<String, List<Object>> headerMap;

  public Map<String, List<Object>> getHeaderMap() {
    return headerMap;
  }

  public void setHeaderMap(Map<String, List<Object>> headerMap) {
    this.headerMap = headerMap;
  }

  public Object getFirst(String name) {
    if (headerMap == null) {
      return null;
    }

    List<Object> values = headerMap.get(name);
    if (values == null || values.isEmpty()) {
      return null;
    }

    return values.get(0);
  }

  public List<Object> getHeader(String name) {
    if (headerMap == null) {
      return null;
    }

    return headerMap.get(name);
  }

  public Headers addHeader(String name, Object value) {
    if (headerMap == null) {
      headerMap = new HashMap<>();
    }

    List<Object> values = headerMap.computeIfAbsent(name, k -> new ArrayList<>());
    values.add(value);

    return this;
  }

  public Headers addHeader(String name, List<Object> value) {
    if (headerMap == null) {
      headerMap = new HashMap<>();
    }

    List<Object> values = headerMap.computeIfAbsent(name, k -> new ArrayList<>());
    values.addAll(value);

    return this;
  }
}
