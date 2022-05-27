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

package org.apache.servicecomb.demo.multiServiceCenterClient;

import java.util.Arrays;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class ServerBTest implements CategorizedTestCase {
  @RpcReference(microserviceName = "demo-multi-service-center-serverB", schemaId = "ServerEndpoint")
  private IServerEndpoint serverEndpoint;

  @RpcReference(microserviceName = "demo-multi-service-center-serverB", schemaId = "ConfigurationEndpoint")
  private IConfigurationEndpoint configurationEndpoint;

  @Override
  public void testRestTransport() throws Exception {
    TestMgr.check("hello", serverEndpoint.getName("hello"));

    TestMgr.check("key1-boot", configurationEndpoint.getValue("demo.multi.service.center.serverB.key1", 1));
    TestMgr.check("key1-boot", configurationEndpoint.getValue("demo.multi.service.center.serverB.key1", 2));
    TestMgr.check("key1-boot", configurationEndpoint.getValue("demo.multi.service.center.serverB.key1", 3));
    TestMgr.check("key2-override", configurationEndpoint.getValue("demo.multi.service.center.serverB.key2", 1));
    TestMgr.check("key2-override", configurationEndpoint.getValue("demo.multi.service.center.serverB.key2", 2));
    TestMgr.check("key2-override", configurationEndpoint.getValue("demo.multi.service.center.serverB.key2", 3));
    TestMgr.check("key3", configurationEndpoint.getValue("demo.multi.service.center.serverB.key3", 1));
    TestMgr.check("key3", configurationEndpoint.getValue("demo.multi.service.center.serverB.key3", 2));
    TestMgr.check("key3", configurationEndpoint.getValue("demo.multi.service.center.serverB.key3", 3));
    TestMgr.check("key4-boot", configurationEndpoint.getValue("demo.multi.service.center.serverB.key4", 1));
    TestMgr.check("key4-boot", configurationEndpoint.getValue("demo.multi.service.center.serverB.key4", 2));
    TestMgr.check("key4-boot", configurationEndpoint.getValue("demo.multi.service.center.serverB.key4", 3));
    TestMgr.check("key5-high", configurationEndpoint.getValue("demo.multi.service.center.serverB.key5", 1));
    TestMgr.check("key5-high", configurationEndpoint.getValue("demo.multi.service.center.serverB.key5", 2));
    TestMgr.check("key5-high", configurationEndpoint.getValue("demo.multi.service.center.serverB.key5", 3));
    TestMgr.check("key6", configurationEndpoint.getValue("demo.multi.service.center.serverB.key6", 1));
    TestMgr.check("key6", configurationEndpoint.getValue("demo.multi.service.center.serverB.key6", 2));
    TestMgr.check("key6", configurationEndpoint.getValue("demo.multi.service.center.serverB.key6", 3));
    TestMgr.check(Arrays.asList("key71", "key72"),
        configurationEndpoint.getValueList("demo.multi.service.center.serverB.key7", 1));
    TestMgr.check(Arrays.asList("key71", "key72"),
        configurationEndpoint.getValueList("demo.multi.service.center.serverB.key7", 2));
  }

  @Override
  public String getMicroserviceName() {
    return "demo-multi-service-center-serverB";
  }
}