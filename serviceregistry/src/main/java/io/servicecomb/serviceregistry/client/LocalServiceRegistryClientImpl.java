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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.config.YAMLUtil;
import io.servicecomb.foundation.vertx.AsyncResultCallback;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

/**
 * Created by   on 2017/3/31.
 */
public class LocalServiceRegistryClientImpl implements ServiceRegistryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceRegistryClientImpl.class);

    private static final String LOCAL_REGISTRY_FILE = System.getProperty("local.registry.file");

    private Map<String, Object> localRegistry;

    public LocalServiceRegistryClientImpl() {
        try {
            localRegistry = YAMLUtil.yaml2Properties(new FileInputStream(new File(LOCAL_REGISTRY_FILE)));
        } catch (FileNotFoundException e) {
            LOGGER.error("can not load local registry file:" + LOCAL_REGISTRY_FILE, e);
        }
    }

    public LocalServiceRegistryClientImpl(InputStream is) {
        localRegistry = YAMLUtil.yaml2Properties(is);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public List<Microservice> getAllMicroservices() {
        return null;
    }

    @Override
    public String getMicroserviceId(String appId, String microserviceName, String version) {
        return "";
    }

    @Override
    public String registerMicroservice(Microservice microservice) {
        //We need to add the logic to write this local file to simulate the service register scenario
        return "localservice";
    }

    @Override
    public Microservice getMicroservice(String microserviceId) {
        return null;
    }

    @Override
    public String registerMicroserviceInstance(MicroserviceInstance instance) {
        //We need to add the logic to write this local file to simulate the service register scenario
        return "localservice";
    }

    @Override
    public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
        return null;
    }

    @Override
    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
        return true;
    }

    @Override
    public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
        return null;
    }

    @Override
    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {
        watch(selfMicroserviceId, callback, v -> {
        }, v -> {
        });
    }

    @Override
    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
            AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId, String serviceName,
            String versionRule) {
        List<Object> services = (List<Object>) localRegistry.get(serviceName);
        Map<String, Object> serviceItem = null;
        serviceItem = (Map<String, Object>) services.get(0);
        if (serviceItem == null) {
            return null;
        }
        List<Map<String, Object>> instances = (List<Map<String, Object>>) serviceItem.get("instances");
        List<MicroserviceInstance> retInstances = new ArrayList<MicroserviceInstance>();
        for (Map<String, Object> i : instances) {
            MicroserviceInstance instance = new MicroserviceInstance();
            List<String> endpoints = (List<String>) i.get("endpoints");
            instance.setEndpoints(endpoints);
            retInstances.add(instance);
        }

        return retInstances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSchemaExist(String microserviceId, String schemaId) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchema(String microserviceId, String schemaId) {
        return null;
    }

    @Override
    public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
        return false;
    }

    @Override
    public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
            Map<String, String> instanceProperties) {
        return false;
    }
}
