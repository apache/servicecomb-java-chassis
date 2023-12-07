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

import java.util.Map;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class HighwayClient {
  private static final String SSL_KEY = "highway.consumer";

  private ClientPoolManager<HighwayClientConnectionPool> clientMgr;

  public void init(Vertx vertx) throws Exception {
    TcpClientConfig normalConfig = createTcpClientConfig();
    normalConfig.setSsl(false);

    TcpClientConfig sslConfig = createTcpClientConfig();
    sslConfig.setSsl(true);

    clientMgr = new ClientPoolManager<>(vertx, new HighwayClientPoolFactory(normalConfig, sslConfig));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        HighwayConfig.getClientThreadCount());
    Map<String, Object> result = VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
    if (!(boolean) result.get("code")) {
      throw new IllegalStateException((String) result.get("message"));
    }
  }

  @VisibleForTesting
  TcpClientConfig createTcpClientConfig() {
    TcpClientConfig tcpClientConfig = new TcpClientConfig();
    // global request timeout to be login timeout
    tcpClientConfig.setMsLoginTimeout(
        LegacyPropertyFactory.getLongProperty("servicecomb.request.timeout", TcpClientConfig.DEFAULT_LOGIN_TIMEOUT));

    SSLOptionFactory factory =
        SSLOptionFactory.createSSLOptionFactory(SSL_KEY, LegacyPropertyFactory.getEnvironment());
    SSLOption sslOption;
    if (factory == null) {
      sslOption = SSLOption.build(SSL_KEY, LegacyPropertyFactory.getEnvironment());
    } else {
      sslOption = factory.createSSLOption();
    }
    SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
    VertxTLSBuilder.buildClientOptionsBase(sslOption, sslCustom, tcpClientConfig);

    return tcpClientConfig;
  }

  public HighwayClientPackage createClientPackage(Invocation invocation, OperationProtobuf operationProtobuf) {
    long msRequestTimeout = invocation.getOperationMeta().getConfig().getMsRequestTimeout();
    return new HighwayClientPackage(invocation, operationProtobuf, msRequestTimeout);
  }

  public HighwayClientConnection findClientPool(Invocation invocation) {
    return clientMgr.findClientPool(invocation.isSync())
        .findOrCreateClient(invocation.getEndpoint().getEndpoint());
  }
}
