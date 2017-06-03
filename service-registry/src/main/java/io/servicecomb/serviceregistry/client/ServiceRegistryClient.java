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
     * meta相关
     */

    /**
     *
     * get all microservices
     * @return a list of Microservice
     */
    List<Microservice> getAllMicroservices();

    /**
     *
     * 获取微服务唯一标识
     * @param appId
     * @param microserviceName
     * @param versionRule
     * @return serviceId
     */
    String getMicroserviceId(String appId, String microserviceName, String versionRule);

    /**
     *
     * 注册微服务静态信息
     * @param microservice
     * @return serviceId
     */
    String registerMicroservice(Microservice microservice);

    /**
     *
     * 根据微服务唯一标识查询微服务静态信息
     * @param microserviceId
     * @return
     */
    Microservice getMicroservice(String microserviceId);

    /**
     * 更新微服务properties
     * @param microserviceId
     * @param serviceProperties
     * @return
     */
    boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties);

    /**
     *
     * 判定schema是否已经注册
     * @param microserviceId
     * @param schemaId
     * @return
     */
    boolean isSchemaExist(String microserviceId, String schemaId);

    /**
     * 
     * 注册schema
     * @param microserviceId
     * @param schemaId
     * @param schemaContent
     * @return
     */
    boolean registerSchema(String microserviceId, String schemaId, String schemaContent);

    /**
     * 
     * 获取schema内容
     * @param microserviceId
     * @param schemaId
     * @return
     */
    String getSchema(String microserviceId, String schemaId);

    /**
     *
     * 注册微服务实例
     * @param instance
     * @return instanceId
     */
    String registerMicroserviceInstance(MicroserviceInstance instance);

    /**
     *
     * 根据多个微服务唯一标识查询所有微服务实例信息
     * @param consumerId
     * @param providerId
     * @return
     */
    List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId);

    /**
     * 更新微服务实例properties
     * @param microserviceId
     * @param microserviceInstanceId
     * @param instanceProperties
     * @return
     */
    boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
            Map<String, String> instanceProperties);

    /**
     *
     * 去注册微服务实例
     * @param microserviceId
     * @param microserviceInstanceId
     * @return
     */
    boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId);

    /**
     *
     * 服务端返回失败，表示需要重新注册，重新watch
     * @param microserviceId
     * @param microserviceInstanceId
     * @return
     */
    HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId);

    /**
     *
     * watch实例变化
     * @param selfMicroserviceId
     * @return
     */
    void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback);

    /**
    *
    * watch实例变化
    * @param selfMicroserviceId
    * @return
    */
    void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
            AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose);

    /**
     *
     * 按照app+interface+version查询实例endpoints信息
     * @param selfMicroserviceId
     * @param appId
     * @param serviceName
     * @param versionRule
     * @return
     */
    List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId, String serviceName,
            String versionRule);
}
