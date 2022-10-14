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
package org.apache.servicecomb.governance.marker;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.governance.marker.operator.RawOperator;

public class Matcher {
  private String name;

  private Map<String, RawOperator> headers;

  private RawOperator apiPath;

  private List<String> method;

  private String serviceName;

  private CustomMatcher customMatcher;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, RawOperator> getHeaders() {
    return headers;
  }

  public void setHeaders(
      Map<String, RawOperator> headers) {
    this.headers = headers;
  }

  public RawOperator getApiPath() {
    return apiPath;
  }

  public void setApiPath(RawOperator apiPath) {
    this.apiPath = apiPath;
  }

  public List<String> getMethod() {
    return method;
  }

  public void setMethod(List<String> method) {
    this.method = method;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public CustomMatcher getCustomMatcher() {
    return customMatcher;
  }

  public void setCustomMatcher(CustomMatcher customMatcher) {
    this.customMatcher = customMatcher;
  }
}
