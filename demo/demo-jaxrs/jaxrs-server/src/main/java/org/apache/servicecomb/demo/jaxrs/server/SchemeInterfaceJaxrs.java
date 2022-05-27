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

import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

@Path("/jaxrs/schemaInterface")
@Produces(MediaType.APPLICATION_JSON)
public interface SchemeInterfaceJaxrs {
  @Path("/add")
  @GET
  int add(@Min(-100) @RequestParam("a") int a, @Min(1) @RequestParam("b") int b);

  @Path("/interfaceModel")
  @GET
  Page<String> interfaceModel(Page<String> model);
}
