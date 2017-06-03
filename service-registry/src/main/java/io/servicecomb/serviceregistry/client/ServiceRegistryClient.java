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

import java.util.List;
import java.util.Map;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.foundation.vertx.AsyncResultCallback;

public interface ServiceRegistryClient {
    void init() throws Exception;


    /**
     *
     * get all microservices
     * @return a list of Microservice
     */
    List<Microservice> getAllMicroservices();

    String getMicroserviceId(String appId, String microserviceName, String versionRule);

    String registerMicroservice(Microservice microservice);

    Microservice getMicroservice(String microserviceId);

    boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties);

    boolean isSchemaExist(String microserviceId, String schemaId);

    boolean registerSchema(String microserviceId, String schemaId, String schemaContent);

    String getSchema(String microserviceId, String schemaId);

    String registerMicroserviceInstance(MicroserviceInstance instance);

    List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId);

    boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
            Map<String, String> instanceProperties);

    boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId);

    HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId);

    void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback);

    void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
            AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose);

    List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId, String serviceName,
            String versionRule);
}
