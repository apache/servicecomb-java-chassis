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

import javax.servlet.http.Part;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;

public class TestBeanParameterWithUpload {
  @QueryParam("query")
  private String queryStr;

  @FormParam("up1")
  private Part up1;

  private Part up2;

  public String getQueryStr() {
    return queryStr;
  }

  public void setQueryStr(String queryStr) {
    this.queryStr = queryStr;
  }

  public Part getUp1() {
    return up1;
  }

  public void setUp1(Part up1) {
    this.up1 = up1;
  }

  public Part getUp2() {
    return up2;
  }

  @FormParam("up2")
  public void setUp2(Part up2) {
    this.up2 = up2;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TestBeanParameterWithUpload{");
    sb.append("queryStr='").append(queryStr).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
