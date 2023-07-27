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
package org.apache.servicecomb.registry.api;

import java.util.List;
import java.util.Map;

/**
 * Standard information used for microservice instance registration and discovery.
 */
public interface MicroserviceInstance {
  /**
   * Environment(Required): Used for logic separation of microservice instance. Only
   * microservice instance with same environment can discovery each other.
   */
  String getEnvironment();

  /**
   * Application(Required): Used for logic separation of microservice instance. Only
   * microservice instance with same application can discovery each other.
   */
  String getApplication();

  /**
   * Service Name(Required): Unique identifier for microservice.
   */
  String getServiceName();

  /**
   * Service Name Alias(Optional): Unique identifier for microservice.
   *   This alias is used by registry implementation to support rename
   *   of a microservice, e.g. old consumers use old service name can
   *   find a renamed microservice service.
   */
  String getAlias();

  /**
   * Service Version(Required): version of this microservice.
   */
  String getVersion();

  DataCenterInfo getDataCenterInfo();

  /**
   * Service Description(Optional)
   */
  String getDescription();

  /**
   * Service Properties(Optional)
   */
  Map<String, String> getProperties();

  /**
   * Service Schemas(Optional): Open API information.
   */
  Map<String, String> getSchemas();

  /**
   * Service endpoints(Optional).
   */
  List<String> getEndpoints();

  /**
   * Microservice instance id(Required). This id can be generated when microservice instance is starting
   * or assigned by registry implementation.
   *
   * When microservice instance is restarted, this id should be changed.
   */
  String getInstanceId();
}
