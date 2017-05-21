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

package io.servicecomb.demo.springmvc.server;

import org.apache.commons.configuration.SystemConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import io.servicecomb.config.archaius.sources.YAMLConfigurationSource;
import io.servicecomb.adapter.spingmvc.RegistryInitializer;
import io.servicecomb.adapter.springmvc.impl.SEPLoadbalance;
import io.servicecomb.adapter.springmvc.impl.SEPRestTemplate;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;

public class RegistryTest {
    public static void main(String[] args) throws Exception {
        DynamicConfiguration configFromYamlFile =
            new DynamicConfiguration(new YAMLConfigurationSource(),
                    new FixedDelayPollingScheduler());
        // configuration from system properties
        ConcurrentMapConfiguration configFromSystemProperties =
            new ConcurrentMapConfiguration(new SystemConfiguration());

        // create a hierarchy of configuration that makes
        // 1) dynamic configuration source override system properties
        ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        finalConfig.addConfiguration(configFromSystemProperties, "systemEnvConfig");
        finalConfig.addConfiguration(configFromYamlFile, "configFromYamlFile");
        ConfigurationManager.install(finalConfig);

        RegistryInitializer.initRegistry();

        ServiceRegistryClient client = RegistryInitializer.getServiceRegistryClient();
        Microservice owner = RegistryInitializer.getSelfMicroservice();
        MicroserviceInstance ownerInst = RegistryInitializer.getSelfMicroserviceInstance();

        Microservice service2 = new Microservice();
        service2.setAppId(owner.getAppId());
        service2.setDescription(owner.getDescription());
        service2.setLevel(owner.getLevel());
        service2.setServiceId("");
        service2.setServiceName("springmvc2");
        service2.setStatus(owner.getStatus());
        service2.setVersion(owner.getVersion());

        String serviceId = client.getMicroserviceId(service2.getAppId(),
                service2.getServiceName(),
                service2.getVersion());
        if (serviceId != null && !serviceId.isEmpty()) {
            System.out.println("service already exists.");
        } else {
            serviceId = client.registerMicroservice(service2);
            System.out.println("dlddldl s id=" + serviceId);
        }
        service2.setServiceId(serviceId);

        MicroserviceInstance instance = new MicroserviceInstance();
        instance.setInstanceId("");
        instance.setEndpoints(ownerInst.getEndpoints());
        instance.setHealthCheck(ownerInst.getHealthCheck());
        instance.setHostName(ownerInst.getHostName());
        instance.setServiceId(service2.getServiceId());
        instance.setStage(ownerInst.getStage());
        instance.setStatus(ownerInst.getStatus());
        serviceId = client.registerMicroserviceInstance(instance);
        System.out.println("dlddldl i id=" + serviceId);
        instance.setInstanceId(serviceId);

        SEPLoadbalance load = new SEPLoadbalance("springmvc2");
        System.out.println(load.choose()); // should be : 10.57.65.225:8080
        SEPRestTemplate template = new SEPRestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
//        System.out.println(template.getForObject("http://springmvc" + "/controller/sayhi?name=world",headers,
//                        String.class));
        System.out.println( template.exchange(
                "http://springmvc" + "/controller/sayhi?name=world", HttpMethod.GET, entity, String.class));
        
        //发送心跳，不然实例会消失
        while (true) {
            System.out.println("ddddd:" + client.heartbeat(service2.getServiceId(), instance.getInstanceId()));
            Thread.sleep(3000);
        }
    }
}
