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

package org.apache.servicecomb.demo.api;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;

@Path("/headerList")
public interface IHeaderParamWithListSchema {
  @Path("headerListDefault")
  @GET
  String headerListDefault(
      @HeaderParam("headerList") List<String> headerList);

  @Path("headerListCSV")
  @GET
  String headerListCSV(
      @Parameter(name = "headerList", in = ParameterIn.HEADER, style = ParameterStyle.FORM, explode = Explode.FALSE)
          List<String> headerList);

  @Path("headerListMULTI")
  @GET
  String headerListMULTI(
      @Parameter(name = "headerList", in = ParameterIn.HEADER, style = ParameterStyle.FORM, explode = Explode.TRUE)
          List<String> headerList);

  @Path("headerListSSV")
  @GET
  String headerListSSV(
      @Parameter(name = "headerList", in = ParameterIn.HEADER, style = ParameterStyle.SPACEDELIMITED, explode = Explode.FALSE)
          List<String> headerList);

  @Path("headerListPIPES")
  @GET
  String headerListPIPES(
      @Parameter(name = "headerList", in = ParameterIn.HEADER, style = ParameterStyle.PIPEDELIMITED, explode = Explode.FALSE)
          List<String> headerList);
}
