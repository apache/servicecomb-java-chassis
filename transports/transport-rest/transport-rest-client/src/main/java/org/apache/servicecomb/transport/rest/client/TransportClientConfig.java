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

package org.apache.servicecomb.transport.rest.client;

import com.netflix.config.DynamicPropertyFactory;

public final class TransportClientConfig {
  private static Class<? extends RestTransportClient> restTransportClientCls = RestTransportClient.class;

  private TransportClientConfig() {
  }

  public static Class<? extends RestTransportClient> getRestTransportClientCls() {
    return restTransportClientCls;
  }

  public static void setRestTransportClientCls(Class<? extends RestTransportClient> restTransportClientCls) {
    TransportClientConfig.restTransportClientCls = restTransportClientCls;
  }

  public static int getThreadCount() {
    return DynamicPropertyFactory.getInstance().getIntProperty("cse.rest.client.thread-count", 1).get();
  }

  public static int getConnectionMaxPoolSize() {
    return DynamicPropertyFactory.getInstance().getIntProperty("cse.rest.client.connection.maxPoolSize", 5).get();
  }

  public static int getConnectionIdleTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("cse.rest.client.connection.idleTimeoutInSeconds", 30)
        .get();
  }

  public static boolean getConnectionKeepAlive() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty("cse.rest.client.connection.keepAlive", true).get();
  }

  public static boolean getConnectionCompression() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("cse.rest.client.connection.compression", false)
        .get();
  }
}
