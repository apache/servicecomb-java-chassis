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

package org.apache.servicecomb.transport.rest.vertx;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import io.vertx.core.Verticle;

public final class TransportConfig {

  public static final int DEFAULT_SERVER_THREAD_COUNT = 1;

  public static final int DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND = 60;

  public static final boolean DEFAULT_SERVER_COMPRESSION_SUPPORT = false;

  // 32K
  public static final int DEFAULT_SERVER_MAX_HEADER_SIZE = 32 * 1024;

  private static Class<? extends Verticle> restServerVerticle = RestServerVerticle.class;

  private TransportConfig() {
  }

  public static Class<? extends Verticle> getRestServerVerticle() {
    return restServerVerticle;
  }

  public static void setRestServerVerticle(Class<? extends Verticle> restServerVerticle) {
    TransportConfig.restServerVerticle = restServerVerticle;
  }

  public static String getAddress() {
    DynamicStringProperty address =
        DynamicPropertyFactory.getInstance().getStringProperty("cse.rest.address", null);
    return address.get();
  }

  public static int getThreadCount() {
    DynamicIntProperty address =
        DynamicPropertyFactory.getInstance()
            .getIntProperty("cse.rest.server.thread-count", DEFAULT_SERVER_THREAD_COUNT);
    return address.get();
  }

  public static int getConnectionIdleTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("cse.rest.server.connection.idleTimeoutInSeconds",
            DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND)
        .get();
  }

  public static boolean getCompressed() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("cse.rest.server.compression", DEFAULT_SERVER_COMPRESSION_SUPPORT)
        .get();
  }

  public static int getMaxHeaderSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("cse.rest.server.maxHeaderSize", DEFAULT_SERVER_MAX_HEADER_SIZE)
        .get();
  }
}
