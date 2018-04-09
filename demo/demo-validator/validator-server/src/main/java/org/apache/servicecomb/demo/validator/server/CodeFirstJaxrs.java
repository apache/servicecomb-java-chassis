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

package org.apache.servicecomb.demo.validator.server;

import javax.validation.constraints.Min;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.hibernate.validator.constraints.Length;

@RestSchema(schemaId = "codeFirst")
@Path("/codeFirstJaxrs")
@Produces(MediaType.APPLICATION_JSON)
public class CodeFirstJaxrs {

  @GET
  public int defaultPath() {
    return 100;
  }

  @Path("/add")
  @POST
  public int add(@FormParam("a") int a, @Min(20) @FormParam("b") int b) {
    return a + b;
  }

  @Path("/sayhi/{name}")
  @PUT
  public String sayHi(@Length(min = 3) @PathParam("name") String name) {
    ContextUtils.getInvocationContext().setStatus(202);
    return name + " sayhi";
  }

  @Path("/traceId")
  @GET
  public String getTraceId() {
    return ContextUtils.getInvocationContext().getContext(Const.TRACE_ID_NAME);
  }
}
