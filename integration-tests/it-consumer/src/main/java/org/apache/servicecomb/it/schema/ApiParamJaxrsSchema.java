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

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.provider.rest.common.RestSchema;

import io.swagger.annotations.ApiParam;

@RestSchema(schemaId = "apiParamJaxrs")
@Path("/apiParamJaxrs")
public class ApiParamJaxrsSchema {
  @POST
  @Path("/body")
  public void body(@ApiParam(value = "desc of body param") CommonModel model) {

  }

  @POST
  @Path("/query")
  public void query(@ApiParam(value = "desc of query param") @QueryParam("input") int input) {

  }

  @POST
  @Path("/queryArr")
  public void queryArr(@ApiParam(value = "desc of queryArr param")
  @QueryParam("input") String[] input) {

  }

  @POST
  @Path("/header")
  public void header(@ApiParam(value = "desc of header param") @HeaderParam("input") int input) {

  }

  @POST
  @Path("/cookie")
  public void cookie(@ApiParam(value = "desc of cookie param") @CookieParam("input") int input) {

  }

  @POST
  @Path("/form")
  public void form(@ApiParam(value = "desc of form param") @FormParam("input") int input) {

  }
}
