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

import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response.Status;

import com.google.common.annotations.VisibleForTesting;
import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class HighwayClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayClient.class);

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
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
  }

  @VisibleForTesting
  TcpClientConfig createTcpClientConfig() {
    TcpClientConfig tcpClientConfig = new TcpClientConfig();
    // global request timeout to be login timeout
    tcpClientConfig.setMsLoginTimeout(DynamicPropertyFactory.getInstance()
        .getLongProperty("servicecomb.request.timeout", TcpClientConfig.DEFAULT_LOGIN_TIMEOUT).get());

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

    return tcpClientConfig;
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    invocation.getInvocationStageTrace().startGetConnection();
    HighwayClientConnection tcpClient = findClientPool(invocation);

    invocation.getInvocationStageTrace().startClientFiltersRequest();
    OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(invocation);
    HighwayClientPackage clientPackage = createClientPackage(invocation, operationProtobuf);

    invocation.onStartSendRequest();
    tcpClient.send(clientPackage, ar -> {
      invocation.getInvocationStageTrace().finishWriteToBuffer(clientPackage.getFinishWriteToBuffer());
      invocation.getInvocationStageTrace().finishReceiveResponse();
      // 此时是在网络线程中，转换线程
      invocation.getResponseExecutor().execute(() -> {
        invocation.getInvocationStageTrace().startClientFiltersResponse();
        if (ar.failed()) {
          // 只会是本地异常
          invocation.getInvocationStageTrace().finishClientFiltersResponse();
          if (ar.cause() instanceof TimeoutException) {
            // give an accurate cause for timeout exception
            //   The timeout period of 30000ms has been exceeded while executing GET /xxx for server 1.1.1.1:8080
            // should not copy the message to invocationException to avoid leak server ip address
            LOGGER.info("Request timeout, Details: {}.", ar.cause().getMessage());

            asyncResp.consumerFail(new InvocationException(Status.REQUEST_TIMEOUT,
                new CommonExceptionData("Request Timeout.")));
            return;
          }
          asyncResp.consumerFail(ar.cause());
          return;
        }

        // 处理应答
        try {
          Response response =
              HighwayCodec.decodeResponse(invocation,
                  operationProtobuf,
                  ar.result());
          invocation.getInvocationStageTrace().finishClientFiltersResponse();
          asyncResp.complete(response);
        } catch (Throwable e) {
          invocation.getInvocationStageTrace().finishClientFiltersResponse();
          asyncResp.consumerFail(e);
        }
      });
    });
  }

  public HighwayClientPackage createClientPackage(Invocation invocation, OperationProtobuf operationProtobuf) {
    long msRequestTimeout = invocation.getOperationMeta().getConfig().getMsRequestTimeout();
    return new HighwayClientPackage(invocation, operationProtobuf, msRequestTimeout);
  }

  public HighwayClientConnection findClientPool(Invocation invocation) {
    HighwayClientConnection tcpClient = clientMgr.findClientPool(invocation.isSync())
        .findOrCreateClient(invocation.getEndpoint().getEndpoint());

    invocation.getInvocationStageTrace().finishGetConnection();

    return tcpClient;
  }
}
