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

package io.servicecomb.core.provider.consumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import io.servicecomb.core.Response;

/**
 * 业务线程在阻塞等待着，不必另起线程
 * 将应答流程包装为Runnable，先唤醒业务线程，再在业务线程中执行runnable
 *
 * @version  [版本号, 2016年12月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class SyncResponseExecutor implements Executor {
    private CountDownLatch latch;

    private Runnable cmd;

    private Response response;

    /**
     * <构造函数> [参数说明]
     */
    public SyncResponseExecutor() {
        latch = new CountDownLatch(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable cmd) {
        this.cmd = cmd;
        latch.countDown();
    }

    public Response waitResponse() throws InterruptedException {
        // TODO:增加配置，指定超时时间
        latch.await();
        // cmd为null，是没走execute，直接返回的场景
        if (cmd != null) {
            cmd.run();
        }

        return response;
    }

    /**
     * 对response进行赋值
     * @param response response的新值
     */
    public void setResponse(Response response) {
        this.response = response;
        if (cmd == null) {
            // 走到这里，没有cmd
            // 说明没走到网络线程，直接就返回了
            // 或者在网络线程中没使用execute的方式返回，这会导致返回流程在网络线程中执行
            // 虽然不合适，但是也不应该导致业务线程无法唤醒
            latch.countDown();
        }
    }
}
