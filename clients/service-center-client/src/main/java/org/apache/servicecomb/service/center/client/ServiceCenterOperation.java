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

package org.apache.servicecomb.service.center.client;

import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.CreateSchemaRequest;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.ModifySchemasRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceInstanceResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;

public interface ServiceCenterOperation {
  /**
   * Query service center instances.
   *
   * In configuration file, only one address of service center instances can be configured, and
   * other instances can be discovered by calling this method.
   *
   * @return MicroserviceInstancesResponse
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  MicroserviceInstancesResponse getServiceCenterInstances();

  /**
   * Register microservcie.
   *
   * @return RegisterMicroserviceResponse
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  RegisteredMicroserviceResponse registerMicroservice(Microservice microservice);

  /**
   * find all registered microservice of service-center.
   *
   * @return MicroserviceResponse
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  MicroservicesResponse getMicroserviceList();

  /**
   * Get microservice information by service id.
   *
   * @return Microservice
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  Microservice getMicroserviceByServiceId(String serviceId);

  /**
   * query Microservice ID by appId, serviceName, version and environment.
   *
   * @return Microservice ID, null if microservice does not exists.
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  RegisteredMicroserviceResponse queryServiceId(Microservice microservice);

  /**
   * Register microservice instances to service-center
   *
   * Notice: microserviceInstance' service id must be set before calling this method.
   *
   * @return instanceId
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  RegisteredMicroserviceInstanceResponse registerMicroserviceInstance(MicroserviceInstance microserviceInstance);

  /**
   * Find microservice instances of service-center
   *
   * @return MicroserviceInstancesResponse
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  MicroserviceInstancesResponse getMicroserviceInstanceList(String serviceId);

  /**
   * Get microservice instance by id
   *
   * @return MicroserviceInstance
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId);

  /**
   * Find MicroserviceInstance by properties.
   *
   * @return FindMicroserviceInstancesResponse
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.n
   */
  FindMicroserviceInstancesResponse findMicroserviceInstance(String consumerId, String appId, String serviceName,
      String versionRule,
      String revision);

  /**
   * Update status of microservice Instance
   *
   * @return if update is successful
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  boolean updateMicroserviceInstanceStatus(String serviceId, String instanceId,
      MicroserviceInstanceStatus status);

  /**
   * register schema.
   * @return if register is successful
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  boolean registerSchema(String serviceId, String schemaId, CreateSchemaRequest schema);

  /**
   * update schema context of service
   *
   * @return if update is successful
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  boolean updateServiceSchemaContext(String serviceId, SchemaInfo schemaInfo);

  /**
   * batch update schema context of service
   *
   * @return if update is successful
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  boolean batchUpdateServiceSchemaContext(String serviceId, ModifySchemasRequest modifySchemasRequest);

  /**
   * send heart beat of this instance.
   * @return if heartbeat is successful
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  boolean sendHeartBeat(String serviceId, String instanceId);

  /**
   * query token using user confidential
   *
   * @return if heartbeat is successful
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  RbacTokenResponse queryToken(RbacTokenRequest request);
}
