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
package org.apache.servicecomb.config.etcd;

import org.springframework.core.env.Environment;

public class EtcdConfig {
  public static final String ZOOKEEPER_DEFAULT_ENVIRONMENT = "production";

  public static final String PROPERTY_CONNECT_STRING = "servicecomb.config.etcd.connectString";

  public static final String PROPERTY_SESSION_TIMEOUT = "servicecomb.config.etcd.sessionTimeoutMillis";

  public static final String PROPERTY_CONNECTION_TIMEOUT = "servicecomb.config.etcd.connectionTimeoutMills";

  public static final String PROPERTY_AUTH_SCHEMA = "servicecomb.config.etcd.authenticationSchema";

  public static final String PROPERTY_AUTH_INFO = "servicecomb.config.etcd.authenticationInfo";

  public static final String PROPERTY_INSTANCE_TAG = "servicecomb.config.etcd.instanceTag";

  private final Environment environment;

  public EtcdConfig(Environment environment) {
    this.environment = environment;
  }

  public String getConnectString() {
    return environment.getProperty(PROPERTY_CONNECT_STRING, "http://127.0.0.1:2379");
  }

  public int getSessionTimeoutMillis() {
    return environment.getProperty(PROPERTY_SESSION_TIMEOUT, int.class, 60000);
  }

  public int getConnectionTimeoutMillis() {
    return environment.getProperty(PROPERTY_CONNECTION_TIMEOUT, int.class, 1000);
  }

  public String getAuthSchema() {
    return environment.getProperty(PROPERTY_AUTH_SCHEMA);
  }

  public String getAuthInfo() {
    return environment.getProperty(PROPERTY_AUTH_INFO);
  }

  public String getInstanceTag() {
    return environment.getProperty(PROPERTY_INSTANCE_TAG);
  }
}
