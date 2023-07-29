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

package org.apache.servicecomb.registry.lightweight;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.registry.lightweight.model.Microservice;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstance;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/discovery")
public interface DiscoveryClient {
  @Path("/info")
  @GET
  @Operation(summary = "", operationId = "getInfo")
  CompletableFuture<MicroserviceInfo> getInfoAsync(Endpoint endpoint, @QueryParam("service-id") String serviceId);


  default MicroserviceInfo getInfo(Endpoint endpoint, String serviceId) {
    return AsyncUtils.toSync(getInfoAsync(endpoint, serviceId));
  }

  @Path("/microservice")
  @GET
  @Operation(summary = "", operationId = "getMicroservice")
  CompletableFuture<Microservice> getMicroserviceAsync(Endpoint endpoint, @QueryParam("service-id") String serviceId);

  default Microservice getMicroservice(Endpoint endpoint, String serviceId) {
    return AsyncUtils.toSync(getMicroserviceAsync(endpoint, serviceId));
  }

  @Path("/instance")
  @GET
  @Operation(summary = "", operationId = "getInstance")
  CompletableFuture<MicroserviceInstance> getInstanceAsync(Endpoint endpoint,
      @QueryParam("service-id") String serviceId);

  default MicroserviceInstance getInstance(Endpoint endpoint, String serviceId) {
    return AsyncUtils.toSync(getInstanceAsync(endpoint, serviceId));
  }

  @Path("/schemas/{schema-id}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "", operationId = "getSchema")
  CompletableFuture<String> getSchemaAsync(Endpoint endpoint, @QueryParam("service-id") String serviceId,
      @PathParam("schema-id") String schemaId);

  default String getSchema(Endpoint endpoint, String serviceId, String schemaId) {
    return AsyncUtils.toSync(getSchemaAsync(endpoint, serviceId, schemaId));
  }
}
