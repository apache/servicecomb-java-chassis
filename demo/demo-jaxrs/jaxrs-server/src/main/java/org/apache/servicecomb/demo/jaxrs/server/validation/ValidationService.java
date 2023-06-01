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

package org.apache.servicecomb.demo.jaxrs.server.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.apache.servicecomb.demo.validator.Teacher;
import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "ValidationService")
@Path("ValidationService")
public class ValidationService {
  @Path("/validate")
  @POST
  public ValidationModel errorCode(@NotNull @Valid ValidationModel request) {
    return request;
  }

  @Path("/validateQuery")
  @GET
  public String queryValidate(@NotEmpty @QueryParam("name") String name) {
    return name;
  }

  @Path("/sayTeacherInfo")
  @POST
  public Teacher sayTeacherInfo(@Valid Teacher teacher) {
    return teacher;
  }
}
