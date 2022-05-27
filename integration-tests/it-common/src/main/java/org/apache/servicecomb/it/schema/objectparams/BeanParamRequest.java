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

  @QueryParam("query_array")
  private String[] queryArray;

  @QueryParam("query_list")
  private List<String> queryList;

  @JsonIgnore
  private List<FlattenObjectRequest> ignored;

  public BeanParamRequest() {
  }

  public BeanParamRequest(String path, int query, String header, String[] queryArray, List<String> queryList) {
    this.path = path;
    this.query = query;
    this.header = header;
    this.queryArray = queryArray;
    this.queryList = queryList;
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

  public String[] getQueryArray() {
    return queryArray;
  }

  public BeanParamRequest setQueryArray(String[] queryArray) {
    this.queryArray = queryArray;
    return this;
  }

  public List<String> getQueryList() {
    return queryList;
  }

  public BeanParamRequest setQueryList(List<String> queryList) {
    this.queryList = queryList;
    return this;
  }

  public List<FlattenObjectRequest> getIgnored() {
    return ignored;
  }

  public void setIgnored(List<FlattenObjectRequest> ignored) {
    this.ignored = ignored;
  }
}
