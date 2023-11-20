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

import java.util.Collections;
import java.util.Map;

import org.springframework.util.LinkedCaseInsensitiveMap;

public class GovernanceRequest implements GovernanceRequestExtractor {
  /**
   * headers with this request, maybe null.
   * For provider: headers indicates the request headers to me.
   * For consumer: headers indicates the request headers to the target.
   */
  private Map<String, String> headers = Collections.emptyMap();

  /**
   * Queries with this request, maybe null.
   * For provider: Queries indicates the request params to me.
   * For consumer: Queries indicates the request params to the target.
   */
  private Map<String, String> queries = Collections.emptyMap();

  /**
   * api path with this request, maybe null. For REST, e.g. /foo/bar; For RPC, e.g. MySchema.sayHello
   * For provider: uri indicates the request uri to me.
   * For consumer: uri indicates the request uri to the target.
   */
  private String apiPath;

  /**
   * method with this request, maybe null.
   * For provider: method indicates the request method to me.
   * For consumer: method indicates the request method to the target.
   */
  private String method;

  /**
   * instance id with this request, maybe null.
   * For provider: instanceId indicates who calls me.
   * For consumer: instanceId indicates the target instance.
   */
  private String instanceId;

  /**
   * microservice id (microservice name or application name + microservice name) with this request, maybe null.
   * For provider: serviceName indicates who calls me.
   * For consumer: serviceName indicates the target service.
   */
  private String serviceName;

  /**
   * sourceRequest the source request for creating this governanceRequest
   * For provider: uri indicates the request to me.
   * For consumer: uri indicates the request to the target.
   * the type of sourceRequest could be ClientRequest, ServerWebExchange, HttpRequest, HttpServletRequest and so on,
   * User will use this request to extract the information he need
   */
  private Object sourceRequest;

  @Override
  public String header(String key) {
    return headers.get(key);
  }

  @Override
  public String query(String key) {
    return queries.get(key);
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Map<String, String> getQueries() {
    return queries;
  }

  public void setHeaders(Map<String, String> headers) {
    Map<String, String> temp = new LinkedCaseInsensitiveMap<>();
    temp.putAll(headers);
    this.headers = temp;
  }

  public void setQueries(Map<String, String> queries) {
    Map<String, String> temp = new LinkedCaseInsensitiveMap<>();
    temp.putAll(queries);
    this.queries = temp;
  }

  @Override
  public String apiPath() {
    return apiPath;
  }

  public void setApiPath(String apiPath) {
    this.apiPath = apiPath;
  }

  @Override
  public String method() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public String instanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  @Override
  public String serviceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public Object sourceRequest() {
    return sourceRequest;
  }

  public void setSourceRequest(Object sourceRequest) {
    this.sourceRequest = sourceRequest;
  }
}
