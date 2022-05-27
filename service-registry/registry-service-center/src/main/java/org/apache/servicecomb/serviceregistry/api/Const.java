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

package org.apache.servicecomb.serviceregistry.api;

import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;

/**
 * Created by   on 2017/1/9.
 */
public final class Const {
  private Const() {
  }

  public static final class REGISTRY_API {
    public static final String DOMAIN_NAME = ServiceRegistryConfig.INSTANCE.getDomainName();

    public static final String CURRENT_VERSION = ServiceRegistryConfig.INSTANCE.getRegistryApiVersion();

    // 2017-10-21 add new implementations for v4. We can remove v3 support after a period.
    public static final String VERSION_V3 = "v3";

    public static final String LATEST_API_VERSION = "v4";

    public static final String V4_PREFIX = String.format("/v4/%s/registry", DOMAIN_NAME);

    public static final String MICROSERVICE_OPERATION_ALL;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_OPERATION_ALL = "/registry/v3/microservices";
      } else {
        MICROSERVICE_OPERATION_ALL = V4_PREFIX + "/microservices";
      }
    }

    public static final String MICROSERVICE_OPERATION_ONE;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_OPERATION_ONE = "/registry/v3/microservices/%s";
      } else {
        MICROSERVICE_OPERATION_ONE = V4_PREFIX + "/microservices/%s";
      }
    }

    public static final String MICROSERVICE_INSTANCE_OPERATION_ALL;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_INSTANCE_OPERATION_ALL = "/registry/v3/microservices/%s/instances";
      } else {
        MICROSERVICE_INSTANCE_OPERATION_ALL = V4_PREFIX + "/microservices/%s/instances";
      }
    }

    public static final String MICROSERVICE_INSTANCE_OPERATION_ONE;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_INSTANCE_OPERATION_ONE = "/registry/v3/microservices/%s/instances/%s";
      } else {
        MICROSERVICE_INSTANCE_OPERATION_ONE = V4_PREFIX + "/microservices/%s/instances/%s";
      }
    }

    public static final String MICROSERVICE_INSTANCES;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_INSTANCES = "/registry/v3/instances";
      } else {
        MICROSERVICE_INSTANCES = V4_PREFIX + "/instances";
      }
    }

    public static final String MICROSERVICE_PROPERTIES;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_PROPERTIES = "/registry/v3/microservices/%s/properties";
      } else {
        MICROSERVICE_PROPERTIES = V4_PREFIX + "/microservices/%s/properties";
      }
    }

    public static final String MICROSERVICE_INSTANCE_PROPERTIES;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_INSTANCE_PROPERTIES = "/registry/v3/microservices/%s/instances/%s/properties";
      } else {
        MICROSERVICE_INSTANCE_PROPERTIES = V4_PREFIX + "/microservices/%s/instances/%s/properties";
      }
    }

    public static final String MICROSERVICE_HEARTBEAT;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_HEARTBEAT = "/registry/v3/microservices/%s/instances/%s/heartbeat";
      } else {
        MICROSERVICE_HEARTBEAT = V4_PREFIX + "/microservices/%s/instances/%s/heartbeat";
      }
    }

    public static final String MICROSERVICE_EXISTENCE;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_EXISTENCE = "/registry/v3/existence";
      } else {
        MICROSERVICE_EXISTENCE = V4_PREFIX + "/existence";
      }
    }

    public static final String MICROSERVICE_ALL_SCHEMAs;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_ALL_SCHEMAs = "/registry/v3/microservices/%s/schemas";
      } else {
        MICROSERVICE_ALL_SCHEMAs = V4_PREFIX + "/microservices/%s/schemas";
      }
    }

    public static final String MICROSERVICE_SCHEMA;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_SCHEMA = "/registry/v3/microservices/%s/schemas/%s";
      } else {
        MICROSERVICE_SCHEMA = V4_PREFIX + "/microservices/%s/schemas/%s";
      }
    }

    public static final String MICROSERVICE_WATCH;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_WATCH = "/registry/v3/microservices/%s/watcher";
      } else {
        MICROSERVICE_WATCH = V4_PREFIX + "/microservices/%s/watcher";
      }
    }

    public static final String SERVICECENTER_VERSION;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        SERVICECENTER_VERSION = "/version";
      } else {
        SERVICECENTER_VERSION = V4_PREFIX + "/version";
      }
    }

    public static final String MICROSERVICE_INSTANCE_STATUS;

    static {
      if (VERSION_V3.equals(CURRENT_VERSION)) {
        MICROSERVICE_INSTANCE_STATUS = "/registry/v3/microservices/%s/instances/%s/status";
      } else {
        MICROSERVICE_INSTANCE_STATUS = V4_PREFIX + "/microservices/%s/instances/%s/status";
      }
    }

    public static final String RBAC_TOKEN = "/v4/token";
  }

  public static final String REGISTRY_APP_ID = "default";

  public static final String REGISTRY_SERVICE_NAME = "SERVICECENTER";

  public static final String KIE_NAME = "KIE";

  public static final String CONFIG_CENTER_NAME = "CseConfigCenter";

  public static final String CSE_MONITORING_NAME = "CseMonitoring";

  public static final String PATH_CHECKSESSION = "checksession";

  public static final int SERVICE_CENTER_ORDER = 100;

  public static final String SERVICECENTER_FRAMEWORK_VERSION = "1.0.0";

  public static final String SERVICE_CENTER_ENABLED = "servicecomb.service.registry.enabled";

  public static final String SAME_ZONE = "sameZone";

  public static final String SAME_REGION = "sameRegion";
}
