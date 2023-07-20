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

package org.apache.servicecomb.swagger.generator.jaxrs;

import java.util.List;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import org.apache.servicecomb.swagger.generator.jaxrs.model.AggregatedParam;
import org.apache.servicecomb.swagger.generator.jaxrs.model.BeanParamComplexField;
import org.apache.servicecomb.swagger.generator.jaxrs.model.BeanParamComplexSetter;
import org.apache.servicecomb.swagger.generator.jaxrs.model.BeanParamDefaultBody;
import org.apache.servicecomb.swagger.generator.jaxrs.model.BeanParamWithJsonIgnoredTagged;
import org.apache.servicecomb.swagger.generator.jaxrs.model.BeanParamWithPart;
import org.apache.servicecomb.swagger.generator.jaxrs.model.enums.DynamicStatus;
import org.apache.servicecomb.swagger.generator.jaxrs.model.enums.DynamicStatusBeanParam;
import org.apache.servicecomb.swagger.generator.jaxrs.model.enums.DynamicStatusModel;
import org.apache.servicecomb.swagger.generator.jaxrs.model.enums.JdkStatus;
import org.apache.servicecomb.swagger.generator.jaxrs.model.enums.JdkStatusBeanParam;
import org.apache.servicecomb.swagger.generator.jaxrs.model.enums.JdkStatusModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path(value = "Echo")
public class Echo {
  @PATCH
  public void patch() {

  }

  @POST
  @ApiResponse(content = {
      @Content(schema = @Schema(type = "number", format = "int32"))}, responseCode = "200", description = "")
  @Path("response")
  public Response response() {
    return null;
  }

  @POST
  @Produces("")
  @Consumes("")
  @Operation(summary = "")
  @Path("emptyPath")
  public void emptyPath() {

  }

  @Path(value = "echo/{targetName}")
  @Consumes(value = {"json", "xml"})
  @Produces(value = {"json", "xml"})
  @POST
  public String echo(User srcUser, @HeaderParam(value = "header") String header,
      @PathParam(value = "targetName") String targetName,
      @QueryParam(value = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @Path(value = "cookie")
  @POST
  public String cookie(@CookieParam(value = "cookie") String cookie) {
    return String.format("%s", cookie);
  }

  @Path(value = "form")
  @POST
  public String form(@FormParam(value = "form") String form) {
    return String.format("%s", form);
  }

  @Path(value = "query")
  @GET
  public String query(@QueryParam(value = "query") String query) {
    return String.format("%s", query);
  }

  @Path(value = "queryComplex")
  @GET
  public String queryComplex(@QueryParam(value = "querys") List<User> querys) {
    return String.format("%s", querys);
  }

  @Operation(summary = "")
  public void ignoredNonRestful() {

  }

  @Path(value = "testRawJson")
  @POST
  public void rawJsonStringMethod(@RawJsonRequestBody String jsonInput) {
  }

  @Path(value = "enumBody")
  @POST
  public void enumBody(Color color) {
  }

  @Path("aggregatedParam")
  @POST
  public void aggregatedParam(@BeanParam AggregatedParam aggregatedParam) {

  }

  @Path("beanParamWithPart")
  @POST
  public void beanParamWithPart(@BeanParam BeanParamWithPart beanParamWithPart) {

  }

  @Path("beanParamComplexField")
  @POST
  public void beanParamComplexField(@BeanParam BeanParamComplexField beanParamComplexField) {

  }

  @Path("beanParamComplexSetter")
  @POST
  public void beanParamComplexSetter(@BeanParam BeanParamComplexSetter beanParamComplexSetter) {

  }

  @Path("beanParamDefaultBody")
  @POST
  public void beanParamDefaultBody(@BeanParam BeanParamDefaultBody beanParamDefaultBody) {

  }

  @Path("beanParamWithJsonIgnoredTaggedBody")
  @POST
  public void beanParamWithJsonIgnoredTagged(@BeanParam BeanParamWithJsonIgnoredTagged beanParamWithJsonIgnoredTagged) {

  }

  @Path("nestedListString")
  @POST
  public List<List<String>> nestedListString(List<List<String>> param) {
    return param;
  }

  @Path("/dynamicStatusEnum")
  @POST
  public DynamicStatus dynamicStatusEnum(@BeanParam DynamicStatusBeanParam statusBeanParam,
      @QueryParam("status") @Parameter(description = "dynamic desc direct") DynamicStatus status,
      DynamicStatusModel model) {
    return null;
  }

  @Path("/jdkStatusEnum")
  @POST
  public JdkStatus jdkStatusEnum(@BeanParam JdkStatusBeanParam statusBeanParam,
      @QueryParam("status") @Parameter(description = "jdk desc direct") JdkStatus status,
      JdkStatusModel model) {
    return null;
  }
}
