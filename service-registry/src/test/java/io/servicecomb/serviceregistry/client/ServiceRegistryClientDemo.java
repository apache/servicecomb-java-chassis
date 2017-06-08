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

package io.servicecomb.serviceregistry.client;

import java.util.ArrayList;
import java.util.List;

import io.servicecomb.serviceregistry.api.registry.HealthCheckMode;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.serviceregistry.api.registry.HealthCheck;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

/**
 * Created by   on 2016/12/6.
 */
public class ServiceRegistryClientDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistryClientDemo.class);

    private static final String APP_NAME = "ServiceRegistryDemo";

    private static final String SERVICE_NAME = "ServiceRegistryClientDemoService";

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) throws Exception {
        Log4jUtils.init();
        BeanUtils.init();

        //Thread.sleep(2000);
        ServiceRegistryClient client = new ServiceRegistryClientImpl();
        client.init();

        // 新增服务
        List<String> list = new ArrayList<>();
        list.add("xxxxxxx");

        Microservice service = new Microservice();
        service.setAppId(APP_NAME);
        service.setServiceName(SERVICE_NAME);
        service.setVersion(VERSION);
        service.setLevel("FRONT");
        service.setSchemas(list);

        String serviceId = client.registerMicroservice(service);
        LOG.info("create service {}", serviceId);

        List<Microservice> mss = client.getAllMicroservices();
        LOG.info("query all services {}", mss.size());

        // Watch
        client.watch(serviceId,
                changedEvent -> {
                    if (changedEvent.succeeded()) {
                        LOG.info("{} {}/{} changed",
                                changedEvent.result().getAction(),
                                changedEvent.result().getKey().getServiceName(),
                                changedEvent.result().getKey().getVersion());
                        for (String s : changedEvent.result().getInstance().getEndpoints()) {
                            LOG.info("  -> {}", s);
                        }
                    } else {
                        LOG.error("", changedEvent.cause());
                    }
                },
                open -> {
                },
                close -> {
                });

        service = client.getMicroservice(serviceId);
        LOG.info("get service {}", service);

        serviceId = client.getMicroserviceId(service.getAppId(), service.getServiceName(), service.getVersion());
        LOG.info("get service id {}", serviceId);

        // 注册实例
        List<String> addresses = new ArrayList<>();
        addresses.add("rest:127.0.0.1:8081");

        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.HEARTBEAT);
        healthCheck.setInterval(10);
        healthCheck.setTimes(0);

        MicroserviceInstance instance = new MicroserviceInstance();
        instance.setServiceId(serviceId);
        instance.setHostName("TestHost");
        instance.setEndpoints(addresses);
        instance.setHealthCheck(healthCheck);

        String instanceId = client.registerMicroserviceInstance(instance);
        LOG.info("register service {} instance {}", serviceId, instanceId);

        List<MicroserviceInstance> microserviceInstances = client.getMicroserviceInstance(serviceId, serviceId);
        for (MicroserviceInstance microserviceInstance : microserviceInstances) {
            LOG.info(microserviceInstance.toString());
        }

        // 实例心跳
        HeartbeatResponse response = client.heartbeat(serviceId, instanceId);
        LOG.info("heartbeat {}", response);

        // 查询给定idl服务
        List<MicroserviceInstance> instances = client.findServiceInstance(serviceId,
                APP_NAME,
                SERVICE_NAME,
                "1.0+");

        for (MicroserviceInstance inst : instances) {
            LOG.info("{}", inst);
        }

        // 删除服务
        //client.deleteService(service.getProviderServiceId(), true);

        LOG.info("finish!!!");
    }
}
