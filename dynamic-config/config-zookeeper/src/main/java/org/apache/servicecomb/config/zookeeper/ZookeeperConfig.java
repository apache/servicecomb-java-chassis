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
package org.apache.servicecomb.config.zookeeper;

import org.springframework.core.env.Environment;

public class ZookeeperConfig {
  public static final String ZOOKEEPER_DEFAULT_ENVIRONMENT = "production";

  public static final String PROPERTY_CONNECT_STRING = "servicecomb.config.zk.connect-string";

  public static final String PROPERTY_SESSION_TIMEOUT = "servicecomb.config.zk.session-timeout-millis";

  public static final String PROPERTY_CONNECTION_TIMEOUT = "servicecomb.config.zk.connection-timeout-mills";

  private final Environment environment;

  public ZookeeperConfig(Environment environment) {
    this.environment = environment;
  }

  public String getConnectString() {
    return environment.getProperty(PROPERTY_CONNECT_STRING);
  }

  public int getSessionTimeoutMillis() {
    return environment.getProperty(PROPERTY_SESSION_TIMEOUT, int.class, 60000);
  }

  public int getConnectionTimeoutMillis() {
    return environment.getProperty(PROPERTY_CONNECTION_TIMEOUT, int.class, 1000);
  }
}
