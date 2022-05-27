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

package org.apache.servicecomb.demo.registry;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class SchemaDiscoveryTestCase implements CategorizedTestCase {
  @RpcReference(microserviceName = "thirdParty-no-schema-server", schemaId = "ServerEndpoint")
  IServerEndpoint serverEndpoint;

  @Override
  public void testRestTransport() throws Exception {
    // invoke thirdParty-no-schema-server(mocked by demo-multi-registries-server)
    TestMgr.check("hello", serverEndpoint.getName("hello"));
  }

  @Override
  public void testHighwayTransport() throws Exception {

  }

  @Override
  public void testAllTransport() throws Exception {

  }

  @Override
  public String getMicroserviceName() {
    return "thirdParty-service-center";
  }
}
