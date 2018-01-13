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

package org.apache.servicecomb.serviceregistry.client.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by   on 2016/12/25.
 */
public class RequestParam {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestParam.class);

  private Map<String, String[]> queryParams;

  private Map<String, Object> formFields;

  private byte[] body = null;

  private Map<String, String> headers;

  private Map<String, String> cookies;

  private long timeout;

  public Map<String, String> getCookies() {
    return cookies;
  }

  public RequestParam setCookies(Map<String, String> cookies) {
    this.cookies = cookies;
    return this;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public RequestParam setHeaders(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public byte[] getBody() {
    return body;
  }

  public RequestParam setBody(byte[] body) {
    this.body = body;
    return this;
  }

  public Map<String, Object> getFormFields() {
    return formFields;
  }

  public RequestParam setFormFields(Map<String, Object> formFields) {
    this.formFields = formFields;
    return this;
  }

  public Map<String, String[]> getQueryParamsMap() {
    return queryParams;
  }

  public String getQueryParams() {
    if (queryParams == null || queryParams.size() == 0) {
      return "";
    }
    StringBuilder stringBuilder = new StringBuilder();
    try {
      for (Map.Entry<String, String[]> query : queryParams.entrySet()) {
        for (String val : query.getValue()) {
          stringBuilder.append("&")
              .append(URLEncoder.encode(query.getKey(), "UTF-8"));

          if (val != null && !val.isEmpty()) {
            stringBuilder.append("=")
                .append(URLEncoder.encode(val, "UTF-8"));
          }
        }
      }
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("get query params failed.", e);
      return "";
    }
    return stringBuilder.substring(1);
  }

  public RequestParam setQueryParams(Map<String, String[]> queryParams) {
    this.queryParams = queryParams;
    return this;
  }

  public RequestParam addQueryParam(String key, String value) {
    if (queryParams == null) {
      queryParams = new HashMap<>();
    }
    if (!queryParams.containsKey(key)) {
      queryParams.put(key, new String[] {value});
    } else {
      queryParams.put(key, (String[]) Arrays.asList(queryParams.get(key), value).toArray());
    }
    return this;
  }

  public RequestParam addHeader(String key, String value) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(key, value);
    return this;
  }

  public long getTimeout() {
    return timeout;
  }

  public RequestParam setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

}
