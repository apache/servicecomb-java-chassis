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

package io.servicecomb.foundation.common.base;

public class ServiceCombConstants {

  public static final String CONFIG_APPLICATION_ID = "APPLICATION_ID";

  public static final String CONFIG_SERVICE = "service_description";
  public static final String CONFIG_SERVICE_NAME = CONFIG_SERVICE + ".name";
  public static final String CONFIG_SERVICE_VERSION = CONFIG_SERVICE + ".version";
  public static final String CONFIG_SERVICE_ROLE = CONFIG_SERVICE + ".role";
  public static final String CONFIG_SERVICE_DESCRIPTION = CONFIG_SERVICE + ".description";

  public static final String DEFAULT_SERVICE_NAME = "anonymous-service";

  public static final String CONFIG_TRACING_COLLECTOR_ADDRESS = "servicecomb.tracing.collector.address";
  public static final String CONFIG_TRACING_COLLECTOR_PATH = "/api/v1/spans";
  public static final String DEFAULT_TRACING_COLLECTOR_ADDRESS = "http://127.0.0.1:9411";
}
