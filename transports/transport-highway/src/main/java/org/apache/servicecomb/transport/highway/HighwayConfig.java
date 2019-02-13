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

package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.transport.common.TransportConfigUtils;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public final class HighwayConfig {
  private HighwayConfig() {
  }

  public static String getAddress() {
    DynamicStringProperty address =
        DynamicPropertyFactory.getInstance().getStringProperty("servicecomb.highway.address", null);
    return address.get();
  }

  public static int getServerThreadCount() {
    return TransportConfigUtils.readVerticleCount(
        "servicecomb.highway.server.verticle-count",
        "servicecomb.highway.server.thread-count");
  }

  public static int getClientThreadCount() {
    return TransportConfigUtils.readVerticleCount(
        "servicecomb.highway.client.verticle-count",
        "servicecomb.highway.client.thread-count");
  }
}
