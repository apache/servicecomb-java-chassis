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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "defaultValueJaxrs")
@Path("/v1/defaultValueJaxrs")
public class DefaultValueJaxrsSchema {
  @Path("intQuery")
  @GET
  public int intQuery(@QueryParam("input") @DefaultValue("13") int input) {
    return input;
  }

  @Path("intHeader")
  @GET
  public int intHeader(@HeaderParam(value = "input") @DefaultValue("13") int input) {
    return input;
  }

  @Path("intForm")
  @POST
  public int intForm(@FormParam("input") @DefaultValue("13") int input) {
    return input;
  }

  @Path("stringQuery")
  @GET
  public String stringQuery(@QueryParam("input") @DefaultValue("string") String input) {
    return input;
  }

  @Path("stringHeader")
  @GET
  public String stringHeader(@HeaderParam(value = "input") @DefaultValue("string") String input) {
    return input;
  }

  @Path("stringForm")
  @POST
  public String stringForm(@FormParam("input") @DefaultValue("string") String input) {
    return input;
  }

  @Path("doubleQuery")
  @GET
  public double doubleQuery(@QueryParam("input") @DefaultValue("10.2") double input) {
    return input;
  }

  @Path("doubleHeader")
  @GET
  public double doubleHeader(@HeaderParam(value = "input") @DefaultValue("10.2") double input) {
    return input;
  }

  @Path("doubleForm")
  @POST
  public double doubleForm(@FormParam("input") @DefaultValue("10.2") double input) {
    return input;
  }

  // float
  @Path("floatQuery")
  @GET
  public float floatQuery(@QueryParam("input") @DefaultValue("10.2") float input) {
    return input;
  }

  @Path("floatHeader")
  @GET
  public float floatHeader(@HeaderParam(value = "input") @DefaultValue("10.2") float input) {
    return input;
  }

  @Path("floatForm")
  @POST
  public float floatForm(@FormParam("input") @DefaultValue("10.2") float input) {
    return input;
  }
}
