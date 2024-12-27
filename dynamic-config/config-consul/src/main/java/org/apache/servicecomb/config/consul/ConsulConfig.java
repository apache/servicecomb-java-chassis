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
package org.apache.servicecomb.config.consul;

import org.springframework.core.env.Environment;

public class ConsulConfig {

  public static final String CONSUL_CONFIG_PREFIX = "servicecomb.config.consul";

  public static final String CONSUL_DEFAULT_ENVIRONMENT = "production";

  public static final String PROPERTY_CONSUL_HOST = "servicecomb.config.consul.host";

  public static final String PROPERTY_CONSUL_PORT = "servicecomb.config.consul.port";

  public static final String PROPERTY_CONSUL_SCHEME = "servicecomb.config.consul.scheme";

  public static final String PROPERTY_CONSUL_ACL_TOKEN = "servicecomb.config.consul.acl-token";

  public static final String PROPERTY_CONSUL_DELAY_TIME = "servicecomb.config.consul.delay-time";

  public static final String PROPERTY_INSTANCE_TAG = "servicecomb.config.consul.instance-tag";

  public static final String PATH_ENVIRONMENT = "/servicecomb/config/environment/%s";

  public static final String PATH_APPLICATION = "/servicecomb/config/application/%s/%s";

  public static final String PATH_SERVICE = "/servicecomb/config/service/%s/%s/%s";

  public static final String PATH_VERSION = "/servicecomb/config/version/%s/%s/%s/%s";

  public static final String PATH_TAG = "/servicecomb/config/tag/%s/%s/%s/%s/%s";

  private final Environment environment;

  public ConsulConfig(Environment environment) {
    this.environment = environment;
  }

  public String getConsulHost() {
    return environment.getProperty(PROPERTY_CONSUL_HOST, String.class, "127.0.0.1");
  }

  public int getConsulPort() {
    return environment.getProperty(PROPERTY_CONSUL_PORT, int.class, 8500);
  }

  public String getConsulScheme() {
    return environment.getProperty(PROPERTY_CONSUL_SCHEME);
  }

  public String getConsulAclToken() {
    return environment.getProperty(PROPERTY_CONSUL_ACL_TOKEN);
  }

  public int getConsulDelayTime() {
    return environment.getProperty(PROPERTY_CONSUL_DELAY_TIME, int.class, 1000);
  }

  public String getInstanceTag() {
    return environment.getProperty(PROPERTY_INSTANCE_TAG);
  }
}
