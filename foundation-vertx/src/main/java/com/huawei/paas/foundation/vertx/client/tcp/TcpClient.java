/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.vertx.client.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.foundation.common.net.URIEndpointObject;
import com.huawei.paas.foundation.vertx.server.TcpParser;
import com.huawei.paas.foundation.vertx.tcp.TcpConnection;
import com.huawei.paas.foundation.vertx.tcp.TcpConst;
import com.huawei.paas.foundation.vertx.tcp.TcpOutputStream;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TcpClient extends TcpConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);

    private static AtomicLong reqId = new AtomicLong();

    public static long getAndIncRequestId() {
        return reqId.getAndIncrement();
    }

    enum Status {
        CONNECTING,
        DISCONNECTED,
        TRY_LOGIN,
        WORKING
    }

    // 是在哪个context中创建的
    private Context context;

    private NetClient netClient;

    private TcpClientConfig clientConfig;

    private InetSocketAddress socketAddress;

    private boolean remoteSupportLogin;

    private volatile Status status = Status.DISCONNECTED;

    private NetSocket netSocket;

    // 连接未建立时，临时保存发送消息的队列
    private Buffer tmpWriteBuffer = Buffer.buffer();

    // 所有的访问，都在锁的保护中，是线程安全的
    private volatile Map<Long, TcpRequest> requestMap = new ConcurrentHashMap<>();

    public TcpClient(Context context, NetClient netClient, String endpoint, TcpClientConfig clientConfig) {
        this.context = context;
        this.netClient = netClient;
        URIEndpointObject ipPort = new URIEndpointObject(endpoint);
        this.socketAddress = ipPort.getSocketAddress();
        this.remoteSupportLogin = Boolean.parseBoolean(ipPort.getFirst(TcpConst.LOGIN));
        this.clientConfig = clientConfig;
    }

    /**
     * 获取context的值
     * @return 返回 context
     */
    public Context getContext() {
        return context;
    }

    /**
     * 回调在tcp client verticle线程执行
     * send没有锁优化的意义，因为netSocket.write内部本身会加锁
     * @param msgId   msgId
     * @param buffer  buffer
     * @param callback callback
     */
    public synchronized void send(TcpOutputStream os, long msTimeout, TcpResonseCallback callback) {
        requestMap.put(os.getMsgId(), new TcpRequest(msTimeout, callback));

        if (Status.WORKING.equals(status)) {
            netSocket.write(os.getBuffer());
            return;
        }

        tmpWriteBuffer.appendBuffer(os.getBuffer());
        if (Status.DISCONNECTED.equals(status)) {
            connect();
        }
    }

    private void connect() {
        this.status = Status.CONNECTING;
        LOGGER.info("connecting to address {}", socketAddress.toString());

        netClient.connect(socketAddress.getPort(), socketAddress.getHostString(), ar -> {
            if (ar.succeeded()) {
                onConnectSuccess(ar.result());
                return;
            }

            onConnectFailed(ar.cause());
        });
    }

    private synchronized void onConnectSuccess(NetSocket socket) {
        LOGGER.info("connect to address {} success", socketAddress.toString());
        this.netSocket = socket;
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

    private synchronized void onDisconnected(Throwable e) {
        this.status = Status.DISCONNECTED;
        LOGGER.error("{} disconnected from {}, in thread {}, cause {}",
                netSocket.localAddress().toString(),
                socketAddress.toString(),
                Thread.currentThread().getName(),
                e.getMessage());

        clearCachedRequest(e);
    }

    protected synchronized void tryLogin() {
        if (!remoteSupportLogin) {
            LOGGER.error("remote not support login, address={}.",
                    socketAddress.toString());
            onLoginSuccess();
            return;
        }
        if (clientConfig.getTcpLogin() == null) {
            LOGGER.error("local not support login, address={}.",
                    socketAddress.toString());
            onLoginSuccess();
            return;
        }

        this.status = Status.TRY_LOGIN;
        LOGGER.info("try login to address {}", socketAddress.toString());

        try (TcpOutputStream os = clientConfig.getTcpLogin().createLogin()) {
            requestMap.put(os.getMsgId(),
                    new TcpRequest(clientConfig.getRequestTimeoutMillis(), this::onLoginResponse));
            netSocket.write(os.getBuffer());
        }
    }

    private synchronized void onLoginResponse(AsyncResult<TcpData> asyncResult) {
        if (asyncResult.failed()) {
            LOGGER.error("login failed, address {}", socketAddress.toString(), asyncResult.cause());
            // 在相应回调中设置状态
            netSocket.close();
            return;
        }

        if (!clientConfig.getTcpLogin().onLoginResponse(asyncResult.result().getBodyBuffer())) {
            LOGGER.error("login failed, address {}", socketAddress.toString());
            // 在相应回调中设置状态
            netSocket.close();
            return;
        }

        LOGGER.info("login success, address {}", socketAddress.toString());
        onLoginSuccess();
    }

    private synchronized void onLoginSuccess() {
        if (tmpWriteBuffer != null) {
            LOGGER.info("writting cached buffer to address {}", socketAddress.toString());
            netSocket.write(tmpWriteBuffer);
            tmpWriteBuffer = Buffer.buffer();
        }

        this.status = Status.WORKING;
    }

    private synchronized void onConnectFailed(Throwable cause) {
        // 连接失败
        this.status = Status.DISCONNECTED;
        String msg = String.format("connect to address %s failed.",
                socketAddress.toString());
        LOGGER.error(msg, cause);

        clearCachedRequest(cause);
    }

    protected synchronized void clearCachedRequest(Throwable cause) {
        // 在onSendError，用户可能发起一次新的调用，需要避免作多余的清理
        Map<Long, TcpRequest> oldMap = requestMap;
        requestMap = new ConcurrentHashMap<>();

        for (TcpRequest request : oldMap.values()) {
            request.onSendError(cause);
        }
        oldMap.clear();
    }

    /**
     * onReply
     * @param msgId   msgId
     * @param buffer  buffer
     */
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
