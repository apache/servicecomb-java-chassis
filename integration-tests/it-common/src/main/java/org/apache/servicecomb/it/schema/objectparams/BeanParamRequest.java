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

package org.apache.servicecomb.it.schema.objectparams;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BeanParamRequest {
  private String path;

  @QueryParam("query")
  private int query;

  @HeaderParam("header")
  private String header;

  @JsonIgnore
  private List<FlattenObjectRequest> ignored;

  public BeanParamRequest() {
  }

  public BeanParamRequest(String path, int query, String header) {
    this.path = path;
    this.query = query;
    this.header = header;
  }

  public String getPath() {
    return path;
  }

  @PathParam("path")
  public void setPath(String path) {
    this.path = path;
  }

  public int getQuery() {
    return query;
  }

  public void setQuery(int query) {
    this.query = query;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public List<FlattenObjectRequest> getIgnored() {
    return ignored;
  }

  public void setIgnored(List<FlattenObjectRequest> ignored) {
    this.ignored = ignored;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BeanParamRequest{");
    sb.append("path='").append(path).append('\'');
    sb.append(", query=").append(query);
    sb.append(", header='").append(header).append('\'');
    sb.append(", ignored=").append(ignored);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BeanParamRequest that = (BeanParamRequest) o;
    return query == that.query &&
        Objects.equals(path, that.path) &&
        Objects.equals(header, that.header) &&
        Objects.equals(ignored, that.ignored);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, query, header, ignored);
  }
}
