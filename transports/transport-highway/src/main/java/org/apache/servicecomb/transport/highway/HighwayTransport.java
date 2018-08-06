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

package org.apache.servicecomb.transport.highway;

import java.util.Collections;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.vertx.SimpleJsonObject;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConst;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.springframework.stereotype.Component;

import io.vertx.core.DeploymentOptions;

@Component
public class HighwayTransport extends AbstractTransport {

  private HighwayClient highwayClient = new HighwayClient();

  @Override
  public String getName() {
    return Const.HIGHWAY;
  }

  public boolean init() throws Exception {
    highwayClient.init(transportVertx);

    DeploymentOptions deployOptions = new DeploymentOptions().setInstances(HighwayConfig.getServerThreadCount());
    setListenAddressWithoutSchema(HighwayConfig.getAddress(), Collections.singletonMap(TcpConst.LOGIN, "true"));
    SimpleJsonObject json = new SimpleJsonObject();
    json.put(ENDPOINT_KEY, getEndpoint());
    deployOptions.setConfig(json);
    return VertxUtils.blockDeploy(transportVertx, HighwayServerVerticle.class, deployOptions);
  }

  @Override
  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    highwayClient.send(invocation, asyncResp);
  }
}
