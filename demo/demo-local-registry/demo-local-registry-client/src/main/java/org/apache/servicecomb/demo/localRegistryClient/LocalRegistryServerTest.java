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

package org.apache.servicecomb.demo.localRegistryClient;

import java.util.List;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

@Component
public class LocalRegistryServerTest implements CategorizedTestCase {
  // demo-local-registry-server-bean use yaml to register service, and register schema by
  // local file loading
  @RpcReference(microserviceName = "demo-local-registry-server", schemaId = "CodeFirstEndpoint")
  private CodeFirstService codeFirstService;

  @RpcReference(microserviceName = "demo-local-registry-server", schemaId = "ServerEndpoint")
  private ServerService serverService;

  // demo-local-registry-server-bean use bean to register service, and register schema part by
  // local file loading, part by bean class
  @RpcReference(microserviceName = "demo-local-registry-server-bean", schemaId = "CodeFirstEndpoint")
  private CodeFirstService codeFirstServiceBean;

  @RpcReference(microserviceName = "demo-local-registry-server-bean", schemaId = "ServerEndpoint")
  private ServerService serverServiceBean;

  // demo-local-registry-server-bean2 use bean to register service and schema
  @RpcReference(microserviceName = "demo-local-registry-server-bean2", schemaId = "CodeFirstEndpoint2")
  private CodeFirstService codeFirstServiceBean2;

  @RpcReference(microserviceName = "demo-local-registry-server-bean2", schemaId = "ServerEndpoint")
  private ServerService serverServiceBean2;

  private DiscoveryManager discoveryManager;

  @Autowired
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Override
  public void testRestTransport() throws Exception {
    testServerGetName();
    testCodeFirstGetName();
    testGetAllMicroservice();
  }

  private void testGetAllMicroservice() {
    List<? extends DiscoveryInstance> microserviceList = discoveryManager
        .findServiceInstances("demo-local-registry", "demo-local-registry-client");
    TestMgr.check(1, microserviceList.size());
    microserviceList = discoveryManager
        .findServiceInstances("demo-local-registry", "demo-local-registry-server");
    TestMgr.check(1, microserviceList.size());
    microserviceList = discoveryManager
        .findServiceInstances("demo-local-registry", "demo-local-registry-server-bean");
    TestMgr.check(1, microserviceList.size());
    microserviceList = discoveryManager
        .findServiceInstances("demo-local-registry", "demo-local-registry-server-bean2");
    TestMgr.check(1, microserviceList.size());
  }

  private void testCodeFirstGetName() {
    TestMgr.check("2", codeFirstService.getName("2"));
    TestMgr.check("2", codeFirstServiceBean.getName("2"));
    TestMgr.check("2", codeFirstServiceBean2.getName("2"));
  }

  private void testServerGetName() {
    RestOperations template = RestTemplateBuilder.create();
    TestMgr.check("2", template
        .getForObject("cse://demo-local-registry-server/register/url/prefix/getName?name=2",
            String.class));
    TestMgr.check("2", template
        .getForObject("cse://demo-local-registry-server-bean/register/url/prefix/getName?name=2",
            String.class));
    TestMgr.check("2", template
        .getForObject("cse://demo-local-registry-server-bean2/register/url/prefix/getName?name=2",
            String.class));
    TestMgr.check("2", serverService.getName("2"));
    TestMgr.check("2", serverServiceBean.getName("2"));
    TestMgr.check("2", serverServiceBean2.getName("2"));
  }
}
