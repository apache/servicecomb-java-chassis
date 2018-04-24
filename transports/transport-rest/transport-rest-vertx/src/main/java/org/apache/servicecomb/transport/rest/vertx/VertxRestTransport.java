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

package org.apache.servicecomb.transport.rest.vertx;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.SimpleJsonObject;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.transport.rest.client.RestTransportClient;
import org.apache.servicecomb.transport.rest.client.RestTransportClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.DeploymentOptions;

@Component
public class VertxRestTransport extends AbstractTransport {
  private static final Logger log = LoggerFactory.getLogger(VertxRestTransport.class);

  private RestTransportClient restClient;

  @Override
  public String getName() {
    return Const.RESTFUL;
  }

  @Override
  public int getOrder() {
    return -1000;
  }

  @Override
  public boolean canInit() {
    setListenAddressWithoutSchema(TransportConfig.getAddress());

    URIEndpointObject ep = (URIEndpointObject) getEndpoint().getAddress();
    if (ep == null) {
      return true;
    }

    if (!NetUtils.canTcpListen(ep.getSocketAddress().getAddress(), ep.getPort())) {
      log.info("can not listen {}, skip {}.", ep.getSocketAddress(), this.getClass().getName());
      return false;
    }

    return true;
  }

  @Override
  public boolean init() throws Exception {
    restClient = RestTransportClientManager.INSTANCE.getRestClient();

    // 部署transport server
    DeploymentOptions options = new DeploymentOptions().setInstances(TransportConfig.getThreadCount());
    SimpleJsonObject json = new SimpleJsonObject();
    json.put(ENDPOINT_KEY, getEndpoint());
    json.put(RestTransportClient.class.getName(), restClient);
    options.setConfig(json);
    return VertxUtils.blockDeploy(transportVertx, TransportConfig.getRestServerVerticle(), options);
  }

  @Override
  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    restClient.send(invocation, asyncResp);
  }
}
