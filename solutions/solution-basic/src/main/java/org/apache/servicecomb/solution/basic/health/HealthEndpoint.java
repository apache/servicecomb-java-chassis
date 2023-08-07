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
package org.apache.servicecomb.solution.basic.health;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/scb")
public interface HealthEndpoint {
  /**
   * Health of this instance. If the instanceId match this instance, and this service is ready
   * to service return true. Otherwise, return false.
   *
   * This api is for internal instance status usage. Load balancer will call this api to check if
   * the target instance is alive.
   */
  @GET
  boolean health(@QueryParam("instanceId") String instanceId, @QueryParam("registryName") String registryName);
}
