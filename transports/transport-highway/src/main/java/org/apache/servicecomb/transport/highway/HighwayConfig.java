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

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public final class HighwayConfig {
  private HighwayConfig() {
  }

  public static final String KEY_SERVICECOMB_REQUEST_WAIT_IN_POOL_TIMEOUT = "servicecomb.highway.server.requestWaitInPoolTimeout";

  public static final long DEFAULT_REQUEST_WAIT_IN_POOL_TIMEOUT = 30000;

  private static final DynamicLongProperty requestWaitInPoolTimeoutProperty =
      DynamicPropertyFactory.getInstance().getLongProperty(KEY_SERVICECOMB_REQUEST_WAIT_IN_POOL_TIMEOUT,
          DEFAULT_REQUEST_WAIT_IN_POOL_TIMEOUT);

  public static long getRequestWaitInPoolTimeout() {
    return requestWaitInPoolTimeoutProperty.get();
  }

  public static String getAddress() {
    DynamicStringProperty address =
        DynamicPropertyFactory.getInstance().getStringProperty("servicecomb.highway.address", null);
    return address.get();
  }

  public static int getServerThreadCount() {
    DynamicIntProperty address =
        DynamicPropertyFactory.getInstance().getIntProperty("servicecomb.highway.server.thread-count", 1);
    return address.get();
  }

  public static int getClientThreadCount() {
    DynamicIntProperty address =
        DynamicPropertyFactory.getInstance().getIntProperty("servicecomb.highway.client.thread-count", 1);
    return address.get();
  }
}
