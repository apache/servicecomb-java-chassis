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
package org.apache.servicecomb.swagger.generator.jaxrs.model;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public class AggregatedParam {
  @DefaultValue("pa")
  @PathParam("path0")
  private String strVal;

  @QueryParam("query1")
  private int intVal;

  private long longVal;

  private long cookieVal;

  @HeaderParam("header2")
  private String headerVal;

  public String getStrVal() {
    return strVal;
  }

  public void setStrVal(String strVal) {
    this.strVal = strVal;
  }

  public int getIntVal() {
    return intVal;
  }

  public void setIntVal(int intVal) {
    this.intVal = intVal;
  }

  public long getLongVal() {
    return longVal;
  }

  @DefaultValue("12")
  @FormParam("form3")
  public void setLongVal(long longVal) {
    this.longVal = longVal;
  }

  public long getCookieVal() {
    return cookieVal;
  }

  @CookieParam("cookie4")
  public void setCookieVal(long cookieVal) {
    this.cookieVal = cookieVal;
  }

  public String getHeaderVal() {
    return headerVal;
  }

  public void setHeaderVal(String headerVal) {
    this.headerVal = headerVal;
  }
}
