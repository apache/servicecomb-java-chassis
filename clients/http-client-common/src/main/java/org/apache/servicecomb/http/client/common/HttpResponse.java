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

import org.apache.http.Header;

public class HttpResponse {

  private int statusCode;

  private String message;

  private String content;

  private Header[] headers;

  public HttpResponse() {

  }

  HttpResponse(int statusCode, String message, String content, Header[] headers) {
    this.statusCode = statusCode;
    this.content = content;
    this.message = message;
    this.headers = headers;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getHeader(String key) {
    if (headers == null) {
      return null;
    }
    for (Header header : headers) {
      if (header.getName().equals(key)) {
        return header.getValue();
      }
    }
    return null;
  }
}
