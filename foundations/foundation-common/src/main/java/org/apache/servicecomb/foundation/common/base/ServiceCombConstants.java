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

package org.apache.servicecomb.foundation.common.base;

public interface ServiceCombConstants {

  String CONFIG_APPLICATION_ID_KEY = "APPLICATION_ID";

  String CONFIG_SERVICE_DESCRIPTION_KEY = "service_description";

  String CONFIG_MICROSERVICE_NAME_KEY = "name";

  String CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY = CONFIG_SERVICE_DESCRIPTION_KEY + "." + CONFIG_MICROSERVICE_NAME_KEY;

  String CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY = CONFIG_SERVICE_DESCRIPTION_KEY + ".version";

  String CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY = CONFIG_SERVICE_DESCRIPTION_KEY + ".role";

  String CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY = CONFIG_SERVICE_DESCRIPTION_KEY + ".description";

  String DEFAULT_MICROSERVICE_NAME = "defaultMicroservice";

  String DEFAULT_APPLICATION_ID = "defaultApplicationId";

  String CONFIG_TRACING_COLLECTOR_ADDRESS = "servicecomb.tracing.collector.address";

  String CONFIG_TRACING_COLLECTOR_API_V1 = "v1";

  String CONFIG_TRACING_COLLECTOR_API_V2 = "v2";

  String CONFIG_TRACING_COLLECTOR_API_VERSION = "servicecomb.tracing.collector.apiVersion";

  String CONFIG_TRACING_ENABLED_KEY = "servicecomb.tracing.enabled";

  String CONFIG_TRACING_COLLECTOR_PATH = "/api/{0}/spans";

  String DEFAULT_TRACING_COLLECTOR_ADDRESS = "http://127.0.0.1:9411";

  String CONFIG_SERVICECOMB_PREFIX = "servicecomb.";

  String CONFIG_CSE_PREFIX = "cse.";

  String CONFIG_KEY_SPLITER = "_";

  String CONFIG_FRAMEWORK_DEFAULT_NAME = "servicecomb-java-chassis";

  String CONFIG_DEFAULT_REGISTER_BY = "SDK";

  String SERVICECOMB_ENV = "SERVICECOMB_ENV";

  String DEFAULT_SERVICECOMB_ENV = "";
}
