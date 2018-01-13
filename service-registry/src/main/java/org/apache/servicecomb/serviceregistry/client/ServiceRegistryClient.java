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

package org.apache.servicecomb.serviceregistry.client;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;

public interface ServiceRegistryClient {
  void init();

  /**
   * get all microservices
   */
  List<Microservice> getAllMicroservices();

  /**
   *
   * 获取微服务唯一标识
   */
  String getMicroserviceId(String appId, String microserviceName, String versionRule);

  /**
   *
   * 注册微服务静态信息
   */
  String registerMicroservice(Microservice microservice);

  /**
   *
   * 根据微服务唯一标识查询微服务静态信息
   */
  Microservice getMicroservice(String microserviceId);

  /**
   * 更新微服务properties
   */
  boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties);

  /**
   *
   * 判定schema是否已经注册
   */
  boolean isSchemaExist(String microserviceId, String schemaId);

  /**
   *
   * 注册schema
   */
  boolean registerSchema(String microserviceId, String schemaId, String schemaContent);

  /**
   *
   * 获取schema内容
   */
  String getSchema(String microserviceId, String schemaId);

  /**
   *
   * 注册微服务实例
   */
  String registerMicroserviceInstance(MicroserviceInstance instance);

  /**
   *
   * 根据多个微服务唯一标识查询所有微服务实例信息
   */
  List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId);

  /**
   * 更新微服务实例properties
   */
  boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
      Map<String, String> instanceProperties);

  /**
   *
   * 去注册微服务实例
   */
  boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId);

  /**
   *
   * 服务端返回失败，表示需要重新注册，重新watch
   */
  HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId);

  /**
   *
   * watch实例变化
   */
  void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback);

  /**
   *
   * watch实例变化
   */
  void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
      AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose);

  /**
   *
   * 按照app+interface+version查询实例endpoints信息
   */
  List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName,
      String versionRule);

  /**
  *
  * 按照app+interface+version+revision查询实例endpoints信息
  */
  MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName,
      String versionRule, String revision);

  /**
   * 通过serviceId， instanceId 获取instance对象。
   * @param serviceId
   * @param instanceId
   * @return MicroserviceInstance
   */
  MicroserviceInstance findServiceInstance(String serviceId, String instanceId);
}
