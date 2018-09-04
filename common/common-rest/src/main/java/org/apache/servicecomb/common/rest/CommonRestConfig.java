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

package org.apache.servicecomb.common.rest;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;

public class CommonRestConfig {
  public static final String KEY_SERVICECOMB_REQUEST_WAIT_IN_POOL_TIMEOUT = "servicecomb.rest.server.requestWaitInPoolTimeout";

  public static final long DEFAULT_REQUEST_WAIT_IN_POOL_TIMEOUT = 30000;

  private static final DynamicLongProperty requestWaitInPoolTimeoutProperty =
      DynamicPropertyFactory.getInstance().getLongProperty(KEY_SERVICECOMB_REQUEST_WAIT_IN_POOL_TIMEOUT,
          DEFAULT_REQUEST_WAIT_IN_POOL_TIMEOUT);

  public static long getRequestWaitInPoolTimeout() {
    return requestWaitInPoolTimeoutProperty.get();
  }
}
