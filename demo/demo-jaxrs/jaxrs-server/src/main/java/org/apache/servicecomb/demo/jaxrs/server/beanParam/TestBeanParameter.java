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

package org.apache.servicecomb.demo.jaxrs.server.beanParam;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestBeanParameter {
  @DefaultValue("defaultQueryValue")
  @QueryParam("querySwaggerStr")
  private String queryStr;

  private Integer headerInt;

  private String pathStr;

  @CookieParam("cookieSwaggerLong")
  private long cookieLong;

  @JsonIgnore
  private String ignoredField;

  public String getQueryStr() {
    return queryStr;
  }

  public void setQueryStr(String queryStr) {
    this.queryStr = queryStr;
  }

  public Integer getHeaderInt() {
    return headerInt;
  }

  @DefaultValue("12")
  @HeaderParam("headerSwaggerInt")
  public void setHeaderInt(Integer headerInt) {
    this.headerInt = headerInt;
  }

  public String getPathStr() {
    return pathStr;
  }

  @PathParam("pathSwaggerStr")
  public void setPathStr(String pathStr) {
    this.pathStr = pathStr;
  }

  public long getCookieLong() {
    return cookieLong;
  }

  public void
  setCookieLong(long cookieLong) {
    this.cookieLong = cookieLong;
  }

  public String getIgnoredField() {
    return ignoredField;
  }

  public void setIgnoredField(String ignoredField) {
    this.ignoredField = ignoredField;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TestBeanParameter{");
    sb.append("queryStr='").append(queryStr).append('\'');
    sb.append(", headerInt=").append(headerInt);
    sb.append(", pathStr='").append(pathStr).append('\'');
    sb.append(", cookieLong=").append(cookieLong);
    sb.append('}');
    return sb.toString();
  }
}
