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

import java.util.List;

import org.apache.servicecomb.provider.rest.common.RestSchema;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@RestSchema(schemaId = "QueryParamWithListSchema")
@Path("/queryList")
public class QueryParamWithListSchema {
  @Path("queryListCSV")
  @GET
  public String queryListCSV(
      @Parameter(name = "queryList", in = ParameterIn.QUERY, style = ParameterStyle.FORM, explode = Explode.FALSE)
      @QueryParam("queryList") List<String> queryList) {
    return queryList == null ? "null" : queryList.size() + ":" + queryList;
  }

  @Path("queryListSSV")
  @GET
  public String queryListSSV(
      @Parameter(name = "queryList", in = ParameterIn.QUERY, style = ParameterStyle.SPACEDELIMITED, explode = Explode.FALSE)
      @QueryParam("queryList") List<String> queryList) {
    return queryList == null ? "null" : queryList.size() + ":" + queryList;
  }

  // TODO: Open API 3.0 not support tsv
//  @Path("queryListTSV")
//  @GET
//  public String queryListTSV(
//      @Parameter(name = "queryList", in = ParameterIn.QUERY, style = ParameterStyle.SPACEDELIMITED, explode = Explode.FALSE)
//      @QueryParam("queryList") List<String> queryList) {
//    return queryList == null ? "null" : queryList.size() + ":" + queryList;
//  }

  @Path("queryListPIPES")
  @GET
  public String queryListPIPES(
      @Parameter(name = "queryList", in = ParameterIn.QUERY, style = ParameterStyle.PIPEDELIMITED, explode = Explode.FALSE)
      @QueryParam("queryList") List<String> queryList) {
    return queryList == null ? "null" : queryList.size() + ":" + queryList;
  }

  @Path("queryListMULTI")
  @GET
  public String queryListMULTI(
      @Parameter(name = "queryList", in = ParameterIn.QUERY, style = ParameterStyle.FORM, explode = Explode.TRUE)
      @QueryParam("queryList") List<String> queryList) {
    return queryList == null ? "null" : queryList.size() + ":" + queryList;
  }
}
