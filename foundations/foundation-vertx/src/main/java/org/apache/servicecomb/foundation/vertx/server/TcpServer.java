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

package org.apache.servicecomb.foundation.vertx.server;

import java.net.InetSocketAddress;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.ExceptionUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.metrics.DefaultTcpServerMetrics;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultServerEndpointMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.impl.NetSocketImpl;

public class TcpServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);

  private final URIEndpointObject endpointObject;

  public TcpServer(URIEndpointObject endpointObject) {
    this.endpointObject = endpointObject;
  }

  public void init(Vertx vertx, String sslKey, AsyncResultCallback<InetSocketAddress> callback) {
    NetServer netServer;
    if (endpointObject.isSslEnabled()) {
      SSLOptionFactory factory =
          SSLOptionFactory.createSSLOptionFactory(sslKey, null);
      SSLOption sslOption;
      if (factory == null) {
        sslOption = SSLOption.buildFromYaml(sslKey);
      } else {
        sslOption = factory.createSSLOption();
      }
      SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
      NetServerOptions serverOptions = new NetServerOptions();
      VertxTLSBuilder.buildNetServerOptions(sslOption, sslCustom, serverOptions);
      netServer = vertx.createNetServer(serverOptions);
    } else {
      netServer = vertx.createNetServer();
    }

    netServer.connectHandler(netSocket -> {
      DefaultTcpServerMetrics serverMetrics = (DefaultTcpServerMetrics) ((NetSocketImpl) netSocket).metrics();
      DefaultServerEndpointMetric endpointMetric = serverMetrics.getEndpointMetric();
      long connectedCount = endpointMetric.getCurrentConnectionCount();
      int connectionLimit = getConnectionLimit();
      if (connectedCount > connectionLimit) {
        netSocket.close();
        endpointMetric.onRejectByConnectionLimit();
        return;
      }

      TcpServerConnection connection = createTcpServerConnection();
      connection.init(netSocket);
    });
    netServer.exceptionHandler(e -> LOGGER.error("Unexpected error in server.{}", ExceptionUtils.getExceptionMessageWithoutTrace(e)));
    InetSocketAddress socketAddress = endpointObject.getSocketAddress();
    netServer.listen(socketAddress.getPort(), socketAddress.getHostString(), ar -> {
      if (ar.succeeded()) {
        callback.success(socketAddress);
        return;
      }

      // 监听失败
      String msg = String.format("listen failed, address=%s", socketAddress);
      callback.fail(new Exception(msg, ar.cause()));
    });
  }

  protected int getConnectionLimit() {
    return Integer.MAX_VALUE;
  }

  protected TcpServerConnection createTcpServerConnection() {
    return new TcpServerConnection();
  }
}
