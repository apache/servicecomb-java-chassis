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
package org.apache.servicecomb.it.schema;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "dataTypeJaxrs")
@Path("/v1/dataTypeJaxrs")
public class DataTypeJaxrs {
  private DataTypePojo pojo = new DataTypePojo();

  public void test(HttpServletRequest request, HttpServletResponse response) {

  }

  @Path("intPath/{input}")
  @GET
  public int intPath(@PathParam("input") int input) {
    return pojo.intBody(input);
  }

  @Path("intQuery")
  @GET
  public int intQuery(@QueryParam("input") int input) {
    return pojo.intBody(input);
  }

  @Path("intHeader")
  @GET
  public int intHeader(@HeaderParam("input") int input) {
    return pojo.intBody(input);
  }

  @Path("intCookie")
  @GET
  public int intCookie(@CookieParam("input") int input) {
    return pojo.intBody(input);
  }

  @Path("intForm")
  @POST
  public int intForm(@FormParam("a") int a) {
    return pojo.intBody(a);
  }


  @Path("intBody")
  @POST
  public int intBody(int input) {
    return pojo.intBody(input);
  }

  //这个是伪契约,不支持 highway
  @Path("request")
  @GET
  public int request(@Context HttpServletRequest request) {
    int a = Integer.parseInt(request.getParameter("a"));
    int b = Integer.parseInt(request.getParameter("b"));
    return a - b;
  }
}
