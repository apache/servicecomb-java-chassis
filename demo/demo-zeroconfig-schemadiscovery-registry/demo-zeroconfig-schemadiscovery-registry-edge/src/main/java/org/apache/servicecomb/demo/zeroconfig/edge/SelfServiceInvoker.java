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

package org.apache.servicecomb.demo.zeroconfig.edge;

import java.util.concurrent.CountDownLatch;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component("SelfServiceInvoker")
public class SelfServiceInvoker implements BootListener {
  interface IServerEndpoint {
    String getName(String name);
  }

  @RpcReference(microserviceName = "demo-zeroconfig-schemadiscovery-registry-edge", schemaId = "ServerEndpoint")
  IServerEndpoint endpoint;

  public CountDownLatch latch = new CountDownLatch(1);

  public String result = "";

  public void onAfterRegistry(BootEvent event) {
    result = endpoint.getName("hello");
    latch.countDown();
  }
}
