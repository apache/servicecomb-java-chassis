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

package org.apache.servicecomb.foundation.auth;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SignRequest {

  /**
   * the resource path being requested
   */
  private String resourcePath;

  /**
   *  queryParams parameters being sent as part of this request.
   */
  private Map<String, String[]> queryParams;

  /**
   * Map of the headers included in this request
   */
  private Map<String, String> headers = new HashMap<>();

  /**
   * The service endpoint to which this request should be sent
   */
  private URI endpoint;

  /**
   * The HTTP method to use when sending this request.
   */
  private String httpMethod = "GET";

  /**
   * An optional stream from which to read the request payload.
   */
  private InputStream content;

  /**
   * The datetime in milliseconds for which the signature needs to be
   * computed.
   */
  private long signingDateTimeMilli = System.currentTimeMillis();

  /**
   * The scope of the signature.
   */
  private String scope;

  /**
   * The region to be used for computing the signature.
   */
  private String regionName;

  /**
   * The name of the service.
   */
  private String serviceName;

  /**
   * UTC formatted version of the signing time stamp.
   */
  private String formattedSigningDateTime;

  /**
   * UTC Formatted Signing date with time stamp stripped
   */
  private String formattedSigningDate;

  public SignRequest() {

  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }


  public long getSigningDateTimeMilli() {
    return signingDateTimeMilli;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getRegionName() {
    if (regionName == null) {
      return "";
    }
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getServiceName() {
    if (serviceName == null) {
      return "";
    }
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getFormattedSigningDateTime() {
    return formattedSigningDateTime;
  }

  public void setFormattedSigningDateTime(String formattedSigningDateTime) {
    this.formattedSigningDateTime = formattedSigningDateTime;
  }

  public String getFormattedSigningDate() {
    return formattedSigningDate;
  }

  public void setFormattedSigningDate(String formattedSigningDate) {
    this.formattedSigningDate = formattedSigningDate;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getHttpMethod() {
    return httpMethod;
  }


  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }


  public void setEndpoint(URI endpoint) {
    this.endpoint = endpoint;
  }


  public URI getEndpoint() {
    return endpoint;
  }


  public InputStream getContent() {
    return content;
  }


  public void setContent(InputStream content) {
    this.content = content;
  }


  public void setHeaders(Map<String, String> headers) {
    this.headers.clear();
    this.headers.putAll(headers);
  }


  public Map<String, String[]> getQueryParams() {
    return queryParams;
  }

  public void setQueryParams(Map<String, String[]> queryParams) {
    this.queryParams = queryParams;
  }

}
