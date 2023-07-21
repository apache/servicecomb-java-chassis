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

import java.io.IOException;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import jakarta.servlet.http.Part;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path(value = "/FullSwaggerService")
public class FullSwaggerService {
  @Path("/fileUpload")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public String fileUpload(@FormParam("file1") Part file1, @FormParam("file2") Part file2) throws IOException {
    return null;
  }

  @Path("/queryListMULTI")
  @GET
  public String queryListMULTI(
      @Parameter(name = "queryList", in = ParameterIn.QUERY, style = ParameterStyle.FORM, explode = Explode.TRUE)
      @QueryParam("queryList") List<String> queryList) {
    return queryList == null ? "null" : queryList.size() + ":" + queryList;
  }

  @Path("/defaultValue")
  @GET
  public String defaultValue(@QueryParam("e") int e, @DefaultValue("20") @QueryParam("a") int a,
      @DefaultValue("bobo") @QueryParam("b") String b,
      @DefaultValue("40") @QueryParam("c") Integer c, @Min(value = 20) @Max(value = 30) @QueryParam("d") int d) {
    return "Hello " + a + b + c + d + e;
  }

  // TODO: should produces text/plain for string
//  @Path("/textPlain")
//  @GET
//  @Produces(MediaType.TEXT_PLAIN)
//  public String textPlain() {
//    return null;
//  }
}
