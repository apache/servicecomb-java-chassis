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

package org.apache.servicecomb.demo.zeroconfig.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "ClientServerEndpoint")
@RequestMapping(path = "/register/url/prefix", produces = MediaType.APPLICATION_JSON)
public class ClientServerEndpoint {
  private static final Logger LOGGER
      = LoggerFactory.getLogger(ClientServerEndpoint.class);

  @RpcReference(microserviceName = "demo-zeroconfig-schemadiscovery-registry-server", schemaId = "ServerEndpoint")
  private IServerEndpoint serverEndpoint;

  @GetMapping(path = "/getName")
  public String getName(@RequestParam(name = "name") String name) {
    return serverEndpoint.getName(name);
  }

  @GetMapping(path = "/getRegisteredMicroservice")
  public Set<String> getRegisteredMicroservice() {
    List<Microservice> microserviceList = DiscoveryManager.INSTANCE.getAllMicroservices();
    Set<String> names = new HashSet<>();

    for (Microservice m : microserviceList) {
      if (m.getServiceName().equals("demo-zeroconfig-schemadiscovery-registry-client")
          || m.getServiceName().equals("demo-zeroconfig-schemadiscovery-registry-server")) {
        names.add(m.getServiceName());
      }
    }
    return names;
  }
}