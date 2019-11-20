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
package org.apache.servicecomb.router.model;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class Matcher {

  private String source;

  private Map<String, String> sourceTags;

  private Map<String, HeaderRule> headers;

  private String refer;

  public Matcher() {
  }

  public boolean match(Map<String, String> realHeaders) {
    if (headers == null) {
      return true;
    }
    for (Map.Entry<String, HeaderRule> entry : headers.entrySet()) {
      if (!realHeaders.containsKey(entry.getKey()) || !entry.getValue()
          .match(realHeaders.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public Map<String, String> getSourceTags() {
    return sourceTags;
  }

  public void setSourceTags(Map<String, String> sourceTags) {
    this.sourceTags = sourceTags;
  }

  public Map<String, HeaderRule> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, HeaderRule> headers) {
    this.headers = headers;
  }

  public String getRefer() {
    return refer;
  }

  public void setRefer(String refer) {
    this.refer = refer;
  }

  @Override
  public String toString() {
    return "Matcher{" +
        "source='" + source + '\'' +
        ", sourceTags=" + sourceTags +
        ", headers=" + headers +
        ", refer='" + refer + '\'' +
        '}';
  }
}
