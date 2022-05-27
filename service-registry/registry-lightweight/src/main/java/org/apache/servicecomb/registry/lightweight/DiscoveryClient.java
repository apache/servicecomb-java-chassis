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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

import io.swagger.annotations.ApiOperation;

@Path("/v1/discovery")
public interface DiscoveryClient {
  @Path("/info")
  @GET
  @ApiOperation(value = "", nickname = "getInfo")
  CompletableFuture<MicroserviceInfo> getInfoAsync(Endpoint endpoint, @QueryParam("service-id") String serviceId);


  default MicroserviceInfo getInfo(Endpoint endpoint, String serviceId) {
    return AsyncUtils.toSync(getInfoAsync(endpoint, serviceId));
  }

  @Path("/microservice")
  @GET
  @ApiOperation(value = "", nickname = "getMicroservice")
  CompletableFuture<Microservice> getMicroserviceAsync(Endpoint endpoint, @QueryParam("service-id") String serviceId);

  default Microservice getMicroservice(Endpoint endpoint, String serviceId) {
    return AsyncUtils.toSync(getMicroserviceAsync(endpoint, serviceId));
  }

  @Path("/instance")
  @GET
  @ApiOperation(value = "", nickname = "getInstance")
  CompletableFuture<MicroserviceInstance> getInstanceAsync(Endpoint endpoint,
      @QueryParam("service-id") String serviceId);

  default MicroserviceInstance getInstance(Endpoint endpoint, String serviceId) {
    return AsyncUtils.toSync(getInstanceAsync(endpoint, serviceId));
  }

  @Path("/schemas/{schema-id}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "", nickname = "getSchema")
  CompletableFuture<String> getSchemaAsync(Endpoint endpoint, @QueryParam("service-id") String serviceId,
      @PathParam("schema-id") String schemaId);

  default String getSchema(Endpoint endpoint, String serviceId, String schemaId) {
    return AsyncUtils.toSync(getSchemaAsync(endpoint, serviceId, schemaId));
  }
}
