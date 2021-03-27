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

import static org.apache.servicecomb.registry.lightweight.DiscoveryEndpoint.SCHEMA_ID;

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@RestSchema(schemaId = SCHEMA_ID)
@Path("/v1/discovery")
public class DiscoveryEndpoint {
  public static final String SCHEMA_ID = "scb-discovery";

  private final Self self;

  public DiscoveryEndpoint(Self self) {
    this.self = self;
  }

  @ApiImplicitParams(
      {
          @ApiImplicitParam(name = "service-id", paramType = "query", dataType = "string",
              value = "just make it possible to mock many instances by one real instance for performance test")
      }
  )
  @Path("/info")
  @GET
  public CompletableFuture<MicroserviceInfo> getInfo() {
    return CompletableFuture.completedFuture(self.getMicroserviceInfo());
  }

  @ApiImplicitParams(
      {
          @ApiImplicitParam(name = "service-id", paramType = "query", dataType = "string",
              value = "just make it possible to mock many instances by one real instance for performance test")
      }
  )
  @Path("/microservice")
  @GET
  public CompletableFuture<Microservice> getMicroservice() {
    return CompletableFuture.completedFuture(self.getMicroservice());
  }

  @ApiImplicitParams(
      {
          @ApiImplicitParam(name = "service-id", paramType = "query", dataType = "string",
              value = "just make it possible to mock many instances by one real instance for performance test")
      }
  )
  @Path("/instance")
  @GET
  public CompletableFuture<MicroserviceInstance> getInstance() {
    return CompletableFuture.completedFuture(self.getInstance());
  }

  @ApiImplicitParams(
      {
          @ApiImplicitParam(name = "service-id", paramType = "query", dataType = "string",
              value = "just make it possible to mock many instances by one real instance for performance test")
      }
  )
  @Path("/schemas/{schema-id}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public CompletableFuture<String> getSchema(@PathParam("schema-id") String schemaId) {
    return CompletableFuture.completedFuture(self.getMicroservice().getSchemaMap().get(schemaId));
  }
}
