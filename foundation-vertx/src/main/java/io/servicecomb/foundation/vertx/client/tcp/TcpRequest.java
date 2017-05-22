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

package io.servicecomb.foundation.vertx.client.tcp;

import java.util.concurrent.TimeoutException;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TcpRequest {
    private long begin;

    private long msTimeout;

    private Context callContext;

    private long threadId;

    private TcpResonseCallback responseCallback;

    public TcpRequest(long msTimeout, TcpResonseCallback responseCallback) {
        callContext = Vertx.currentContext();
        threadId = Thread.currentThread().getId();
        this.begin = System.currentTimeMillis();
        this.msTimeout = msTimeout;
        this.responseCallback = responseCallback;
    }

    public void onReply(Buffer headerBuffer, Buffer bodyBuffer) {
        TcpData tcpData = new TcpData(headerBuffer, bodyBuffer);

        if (callContext == null || threadId == Thread.currentThread().getId()) {
            responseCallback.success(tcpData);
            return;
        }

        callContext.runOnContext(Void -> {
            responseCallback.success(tcpData);
        });
    }

    public void onSendError(Throwable e) {
        responseCallback.fail(e);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return
     */
    public boolean isTimeout() {
        return System.currentTimeMillis() - begin >= msTimeout;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    public void onTimeout(TimeoutException e) {
        responseCallback.fail(e);
    }
}
