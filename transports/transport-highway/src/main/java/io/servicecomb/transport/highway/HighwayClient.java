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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicLongProperty;

import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.transport.AbstractTransport;
import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLOption;
import io.servicecomb.foundation.ssl.SSLOptionFactory;
import io.servicecomb.foundation.vertx.VertxTLSBuilder;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class HighwayClient {
  private static final Logger log = LoggerFactory.getLogger(HighwayClient.class);

  private static final String SSL_KEY = "highway.consumer";

  private ClientPoolManager<HighwayClientConnectionPool> clientMgr = new ClientPoolManager<>();

  private final boolean sslEnabled;

  public HighwayClient(boolean sslEnabled) {
    this.sslEnabled = sslEnabled;
  }

  public void init(Vertx vertx) throws Exception {
    TcpClientConfig config = createTcpClientConfig();

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        HighwayConfig.getClientThreadCount(),
        HighwayConfig.getClientConnectionPoolPerThread(),
        config);

    VertxUtils.blockDeploy(vertx, HighwayClientVerticle.class, deployOptions);
  }

  private TcpClientConfig createTcpClientConfig() {
    TcpClientConfig tcpClientConfig = new TcpClientConfig();
    DynamicLongProperty prop = AbstractTransport.getRequestTimeoutProperty();
    prop.addCallback(new Runnable() {
      public void run() {
        tcpClientConfig.setRequestTimeoutMillis(prop.get());
      }
    });
    tcpClientConfig.setRequestTimeoutMillis(prop.get());

    if (this.sslEnabled) {
      SSLOptionFactory factory =
          SSLOptionFactory.createSSLOptionFactory(SSL_KEY, null);
      SSLOption sslOption;
      if (factory == null) {
        sslOption = SSLOption.buildFromYaml(SSL_KEY);
      } else {
        sslOption = factory.createSSLOption();
      }
      SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
      VertxTLSBuilder.buildClientOptionsBase(sslOption, sslCustom, tcpClientConfig);
    }
    return tcpClientConfig;
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    HighwayClientConnectionPool tcpClientPool = clientMgr.findThreadBindClientPool();

    OperationMeta operationMeta = invocation.getOperationMeta();
    OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);

    HighwayClientConnection tcpClient = tcpClientPool.findOrCreateClient(invocation.getEndpoint().getEndpoint());
    HighwayClientPackage clientPackage = new HighwayClientPackage(invocation, operationProtobuf, tcpClient);
    log.debug("Calling method {} of {} by highway", operationMeta.getMethod(), invocation.getMicroserviceName());
    tcpClientPool.send(tcpClient, clientPackage, ar -> {
      // 此时是在网络线程中，转换线程
      invocation.getResponseExecutor().execute(() -> {
        if (ar.failed()) {
          // 只会是本地异常
          asyncResp.consumerFail(ar.cause());
          return;
        }

        // 处理应答
        try {
          Response response =
              HighwayCodec.decodeResponse(invocation,
                  operationProtobuf,
                  ar.result(),
                  tcpClient.getProtobufFeature());
          asyncResp.complete(response);
        } catch (Throwable e) {
          asyncResp.consumerFail(e);
        }
      });
    });
  }
}
