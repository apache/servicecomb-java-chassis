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

package org.apache.servicecomb.foundation.vertx.client.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.server.TcpParser;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConst;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketImpl;

public class TcpClientConnection extends TcpConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(TcpClientConnection.class);

  enum Status {
    CONNECTING,
    DISCONNECTED,
    TRY_LOGIN,
    WORKING
  }

  private NetClientWrapper netClientWrapper;

  private TcpClientConfig clientConfig;

  private URIEndpointObject endpoint;

  private InetSocketAddress socketAddress;

  private boolean localSupportLogin = false;

  private boolean remoteSupportLogin;

  private volatile Status status = Status.DISCONNECTED;

  // save msg before login success.
  // before login, we can not know parameters, like: zip/codec compatible, and so on
  // so can only save package, can not save byteBuf
  private Queue<AbstractTcpClientPackage> packageQueue = new ConcurrentLinkedQueue<>();

  private volatile Map<Long, TcpRequest> requestMap = new ConcurrentHashMap<>();

  public TcpClientConnection(Context context, NetClientWrapper netClientWrapper, String strEndpoint) {
    this.setContext(context);

    this.netClientWrapper = netClientWrapper;
    endpoint = new URIEndpointObject(strEndpoint);
    this.socketAddress = endpoint.getSocketAddress();
    this.remoteSupportLogin = Boolean.parseBoolean(endpoint.getFirst(TcpConst.LOGIN));
    this.clientConfig = netClientWrapper.getClientConfig(endpoint.isSslEnabled());
  }

  public boolean isLocalSupportLogin() {
    return localSupportLogin;
  }

  public void setLocalSupportLogin(boolean localSupportLogin) {
    this.localSupportLogin = localSupportLogin;
  }

  protected TcpOutputStream createLogin() {
    return null;
  }

  protected boolean onLoginResponse(Buffer bodyBuffer) {
    return true;
  }

  public void send(AbstractTcpClientPackage tcpClientPackage, TcpResponseCallback callback) {
    requestMap.put(tcpClientPackage.getMsgId(), new TcpRequest(clientConfig.getRequestTimeoutMillis(), callback));

    if (writeToBufferQueue(tcpClientPackage)) {
      return;
    }

    // before login success, no optimize, just make sure do not lost data
    context.runOnContext(v -> {
      if (!writeToBufferQueue(tcpClientPackage)) {
        packageQueue.add(tcpClientPackage);
      }

      // connect must call in eventloop thread
      // otherwise vertx will create a new eventloop thread for it if count
      //   of eventloop thread is not up to the limit.
      if (Status.DISCONNECTED.equals(status)) {
        connect();
      }
    });
  }

  private boolean writeToBufferQueue(AbstractTcpClientPackage tcpClientPackage) {
    // read status maybe out of eventloop thread, it's not exact
    // just optimize for main scenes
    if (Status.WORKING.equals(status)) {
      // encode in sender thread
      try (TcpOutputStream os = tcpClientPackage.createStream()) {
        write(os.getByteBuf());
      }
      return true;
    }

    return false;
  }

  @Override
  protected void writeInContext() {
    writePackageInContext();

    super.writeInContext();
  }

  private void writePackageInContext() {
    for (;;) {
      AbstractTcpClientPackage pkg = packageQueue.poll();
      if (pkg == null) {
        break;
      }

      try (TcpOutputStream os = pkg.createStream()) {
        Buffer buf = os.getBuffer();
        netSocket.write(buf);
      }
    }
  }

  @VisibleForTesting
  protected void connect() {
    this.status = Status.CONNECTING;
    LOGGER.info("connecting to address {}", socketAddress.toString());

    netClientWrapper.connect(endpoint.isSslEnabled(),
        socketAddress.getPort(),
        socketAddress.getHostString(),
        ar -> {
          if (ar.succeeded()) {
            onConnectSuccess(ar.result());
            return;
          }

          onConnectFailed(ar.cause());
        });
  }

  private void onConnectSuccess(NetSocket socket) {
    LOGGER.info("connected to address {} success in thread {}.",
        socketAddress.toString(),
        Thread.currentThread().getName());
    // currently, socket always be NetSocketImpl
    this.initNetSocket((NetSocketImpl) socket);
    socket.handler(new TcpParser(this::onReply));

    socket.exceptionHandler(this::onException);
    socket.closeHandler(this::onClosed);

    // 开始登录
    tryLogin();
  }

  private void onClosed(Void v) {
    onDisconnected(new IOException("socket closed"));
  }

  // 异常断连时，先触发onException，再触发onClosed
  // 正常断连时，只触发onClosed
  private void onException(Throwable e) {
    LOGGER.error("{} disconnected from {}, in thread {}, cause {}",
        netSocket.localAddress().toString(),
        socketAddress.toString(),
        Thread.currentThread().getName(),
        e.getMessage());
  }

  private void onDisconnected(Throwable e) {
    this.status = Status.DISCONNECTED;
    LOGGER.error("{} disconnected from {}, in thread {}, cause {}",
        netSocket.localAddress().toString(),
        socketAddress.toString(),
        Thread.currentThread().getName(),
        e.getMessage());

    clearCachedRequest(e);
  }

  protected void tryLogin() {
    if (!localSupportLogin || !remoteSupportLogin) {
      LOGGER.error(
          "local or remote not support login, address={}, localSupportLogin={}, remoteSupportLogin={}.",
          socketAddress.toString(),
          localSupportLogin,
          remoteSupportLogin);
      onLoginSuccess();
      return;
    }

    this.status = Status.TRY_LOGIN;
    LOGGER.info("try login to address {}", socketAddress.toString());

    try (TcpOutputStream os = createLogin()) {
      requestMap.put(os.getMsgId(),
          new TcpRequest(clientConfig.getRequestTimeoutMillis(), this::onLoginResponse));
      netSocket.write(os.getBuffer());
    }
  }

  private void onLoginResponse(AsyncResult<TcpData> asyncResult) {
    if (asyncResult.failed()) {
      LOGGER.error("login failed, address {}", socketAddress.toString(), asyncResult.cause());
      // 在相应回调中设置状态
      netSocket.close();
      return;
    }

    if (!onLoginResponse(asyncResult.result().getBodyBuffer())) {
      LOGGER.error("login failed, address {}", socketAddress.toString());
      // 在相应回调中设置状态
      netSocket.close();
      return;
    }

    LOGGER.info("login success, address {}", socketAddress.toString());
    onLoginSuccess();
  }

  private void onLoginSuccess() {
    this.status = Status.WORKING;
    writeInContext();
  }

  private void onConnectFailed(Throwable cause) {
    // 连接失败
    this.status = Status.DISCONNECTED;
    String msg = String.format("connect to address %s failed.",
        socketAddress.toString());
    LOGGER.error(msg, cause);

    clearCachedRequest(cause);
  }

  protected void clearCachedRequest(Throwable cause) {
    // 在onSendError，用户可能发起一次新的调用，需要避免作多余的清理
    Map<Long, TcpRequest> oldMap = requestMap;
    requestMap = new ConcurrentHashMap<>();

    for (TcpRequest request : oldMap.values()) {
      request.onSendError(cause);
    }
    oldMap.clear();
  }

  protected void onReply(long msgId, Buffer headerBuffer, Buffer bodyBuffer) {
    TcpRequest request = requestMap.remove(msgId);
    if (request == null) {
      LOGGER.error("Unknown reply msgId {}, waiting count {}", msgId, requestMap.size());
      return;
    }

    request.onReply(headerBuffer, bodyBuffer);
  }

  public void checkTimeout() {
    for (Entry<Long, TcpRequest> entry : requestMap.entrySet()) {
      TcpRequest request = entry.getValue();
      if (request.isTimeout()) {
        // 可能正好收到reply，且被处理了，所以这里的remove不一定有效
        // 是否有效，根据remove的结果来决定
        request = requestMap.remove(entry.getKey());
        if (request != null) {
          String msg =
              String.format("request timeout, msgId=%d, address=%s", entry.getKey(), socketAddress);
          LOGGER.error(msg);

          request.onTimeout(new TimeoutException(msg));
        }
      }
    }
  }
}
