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

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.foundation.test.scaffolding.model.Media;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;

@RestSchema(schemaId = "paramCodec")
@Path("/v1/paramCodec")
public class ParamCodecSchema {
  /**
   * Test path param and query param encode&decode
   */
  @Path("spaceCharCodec/{pathVal}")
  @GET
  public String spaceCharCodec(@PathParam("pathVal") String pathVal, @QueryParam("q") String q) {
    String expectedPathParamString = "a%2B+%20b%% %20c";
    String expectedParamStringQuery = "a%2B %20b%% %20c";
    return pathVal + " +%20%% " + q + " " + (expectedPathParamString.equals(pathVal)
        && matchOr(q, expectedPathParamString, expectedParamStringQuery));
  }

  private boolean matchOr(String result, String expected1, String expected2) {
    // spring mvc & rpc handles "+' differently, both '+' or ' ' is correct according to HTTP SPEC. spring mvc changed from '+' to ' ' since spring 5.
    return result.equals(expected1) || result.equals(expected2);
  }

  /**
   * Test special enum name tagged by {@link com.fasterxml.jackson.annotation.JsonProperty}
   */
  @Path("enum/enumSpecialName")
  @POST
  public Media enumSpecialName(Media media) {
    return media;
  }

  @Path("invocationContext")
  @GET
  public Map<String, String> getInvocationContext() {
    return ContextUtils.getInvocationContext().getContext();
  }

  @Path("stringUrlencodedForm")
  @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
  @POST
  public Map<String, String> stringUrlencodedForm(Map<String, String> requestMap) {
    return requestMap;
  }
}
