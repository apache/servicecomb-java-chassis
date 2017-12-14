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

package io.servicecomb.transport.highway;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.transport.AbstractTransport;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.foundation.vertx.SimpleJsonObject;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.tcp.TcpConst;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.vertx.core.DeploymentOptions;

@Component
public class HighwayTransport extends AbstractTransport {

  private static final Logger log = LoggerFactory.getLogger(HighwayTransport.class);

  public static final String NAME = "highway";

  @Override
  public String getName() {
    return NAME;
  }

  public boolean init() throws Exception {
    HighwayCodec.setHighwayTransport(this);

    DeploymentOptions deployOptions = new DeploymentOptions().setInstances(HighwayConfig.getServerThreadCount());
    setListenAddressWithoutSchema(HighwayConfig.getAddress(), Collections.singletonMap(TcpConst.LOGIN, "true"));
    SimpleJsonObject json = new SimpleJsonObject();
    json.put(ENDPOINT_KEY, getEndpoint());
    deployOptions.setConfig(json);
    return VertxUtils.blockDeploy(transportVertx, HighwayServerVerticle.class, deployOptions) && deployClient();
  }

  private boolean deployClient() {
    return HighwayClientManager.INSTANCE.getHighwayClient(true) != null &&
        HighwayClientManager.INSTANCE.getHighwayClient(false) != null;
  }

  @Override
  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    HighwayClient client =
        HighwayClientManager.INSTANCE.getHighwayClient(endpoint.isSslEnabled());
    log.debug("Sending request by highway to endpoint {}:{}", endpoint.getHostOrIp(), endpoint.getPort());
    client.send(invocation, asyncResp);
  }
}
