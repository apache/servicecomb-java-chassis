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

package io.servicecomb.serviceregistry;

import java.util.concurrent.TimeUnit;

import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import io.servicecomb.serviceregistry.client.IpPortManager;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.notify.NotifyManager;
import io.servicecomb.serviceregistry.notify.RegistryEvent;
import io.servicecomb.serviceregistry.utils.TimerException;
import io.servicecomb.foundation.common.CommonThread;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version [版本号, 2016年12月15日]
 * @see  [相关类/方法]
 * @since [产品/模块版本]
 */
public class RegistryThread extends CommonThread {
    private boolean registerSuccess = false;

    public RegistryThread() {
        super();
        setName("RegistryThread");
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public void doInit() {
        ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;

        try {
            if (isEmpty(RegistryUtils.getMicroservice().getServiceId())) {
                RegistryUtils.ensureRegisterMicroservice();
            }

            if (isEmpty(RegistryUtils.getMicroserviceInstance().getInstanceId())) {
                RegistryUtils.ensureRegisterInstance();
            }

            // 初始化client发现SR的动态集群扩容能力
            if (serviceRegistryConfig.isRegistryAutoDiscovery()) {
                IpPortManager.INSTANCE.createServiceRegistryCache();
            }
        } catch (TimerException e) {
            RegistryUtils.exception(e);
            return;
        }

        registerSuccess = true;
        NotifyManager.INSTANCE.notify(RegistryEvent.INITIALIZED, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // 本进程微服务及实例注册
        this.doInit();

        // 心跳
        int interval = ServiceRegistryConfig.INSTANCE.getHeartbeatInterval();
        while (isRunning()) {
            if (!registerSuccess) {
                try {
                    RegistryUtils.ensureRegisterMicroservice();
                } catch (TimerException e) {
                    continue;
                }
                registerSuccess = RegistryUtils.regsiterInstance();
            }

            if (registerSuccess) {
                RegistryUtils.watch();
            }

            try {
                TimeUnit.SECONDS.sleep(interval);
            } catch (InterruptedException e) {
                continue;
            }

            // 定时心跳
            HeartbeatResponse response = RegistryUtils.heartbeat();
            if (response != null && !response.isOk()) {
                // 心跳返回实例不存在，需要重新注册
                registerSuccess = false;
            }
        }
    }
}
