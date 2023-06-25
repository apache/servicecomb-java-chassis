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
package org.apache.servicecomb.demo.jaxrs.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "JaxRSDefaultValues")
@Path("/JaxRSDefaultValues")
@Produces(MediaType.APPLICATION_JSON)
public class JaxRSDefaultValues {

  @Path("/form")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String form(@DefaultValue("20") @FormParam("a") int a, @DefaultValue("bobo") @FormParam("b") String b) {
    return "Hello " + a + b;
  }

  @Path("/header")
  @POST
  public String header(@DefaultValue("20") @HeaderParam("a") int a, @DefaultValue("bobo") @HeaderParam("b") String b,
      @DefaultValue("30") @HeaderParam("c") Integer c) {
    return "Hello " + a + b + c;
  }

  @Path("/query")
  @GET
  public String query(@DefaultValue("20") @QueryParam("a") int a, @DefaultValue("bobo") @QueryParam("b") String b,
      @DefaultValue("40") @QueryParam("c") Integer c, @QueryParam("d") int d) {
    return "Hello " + a + b + c + d;
  }

  @Path("/query2")
  @GET
  public String query2(@QueryParam("e") int e, @DefaultValue("20") @QueryParam("a") int a,
      @DefaultValue("bobo") @QueryParam("b") String b,
      @DefaultValue("40") @QueryParam("c") Integer c, @Min(value = 20) @Max(value = 30) @QueryParam("d") int d) {
    return "Hello " + a + b + c + d + e;
  }

  @Path("/query3")
  @GET
  public String query3(@QueryParam("a") @Min(value = 20) int a, @QueryParam("b") String b) {
    return "Hello " + a + b;
  }

  @Path("/packages")
  @GET
  public String queryPackages(HttpServletRequest httpRequest,
      @Max(value = 2147483647L) @Min(value = -1L) @NotNull @QueryParam("pageNo") Integer pageNo,
      @Max(value = 2147483647L) @Min(value = -1L) @NotNull @QueryParam("pageSize") Integer pageSize,
      @Size(max = 64, min = 0) @QueryParam("packageName") String packageName,
      @Max(value = 127L) @Min(value = 0L) @QueryParam("packageType") Integer packageType,
      @Max(value = 2147483647L) @Min(value = 1L) @QueryParam("roleID") Integer roleID,
      @Max(value = 2147483647L) @Min(value = 1L) @QueryParam("categoryID") Integer categoryID,
      @Max(value = 127L) @Min(value = 0L) @QueryParam("appType") @DefaultValue("1") Integer appType,
      @Max(value = 2L) @Min(value = 1L) @QueryParam("packageScope") Integer packageScope) {
    return "" + appType;
  }

  @Path("/javaprimitiveint")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String jaxRsJavaPrimitiveInt(@FormParam("a") int a, @DefaultValue("bobo") @FormParam("b") String b) {
    return "Hello " + a + b;
  }

  @Path("/javaprimitivenumber")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String jaxRsJavaPrimitiveNumber(@QueryParam("a") float a, @QueryParam("b") boolean b) {
    return "Hello " + a + b;
  }

  @Path("/javaprimitivestr")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String jaxRsJavaPrimitiveStr(@FormParam("b") int b, @FormParam("a") String a) {
    if (a == null || a.equals("")) {
      return "Hello";
    }
    return "Hello " + b + a;
  }

  @Path("/javaprimitivecomb")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String jaxRsJavaPrimitiveCombnation(@QueryParam("a") Integer a, @QueryParam("b") Float b) {
    return "Hello " + a + b;
  }

  @Path("/allprimitivetypes")
  @POST
  public String allprimitivetypes(@QueryParam("pBoolean") boolean pBoolean,
      @QueryParam("pChar") char pChar,
      @QueryParam("pByte") byte pByte,
      @QueryParam("pShort") short pShort,
      @QueryParam("pInt") int pInt,
      @QueryParam("pLong") long pLong,
      @QueryParam("pFloat") float pFloat,
      @QueryParam("pDouble") double pDouble,
      @QueryParam("pDoubleWrap") Double pDoubleWrap) {
    return "Hello " + pBoolean + ","
        + pChar + ","
        + pByte + ","
        + pShort + ","
        + pInt + ","
        + pLong + ","
        + pFloat + ","
        + pDouble + ","
        + pDoubleWrap;
  }
}
