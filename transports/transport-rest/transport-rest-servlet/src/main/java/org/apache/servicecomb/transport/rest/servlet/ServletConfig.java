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

package org.apache.servicecomb.transport.rest.servlet;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public final class ServletConfig {
  static final long DEFAULT_TIMEOUT = 3000;

  public static final String KEY_SERVLET_URL_PATTERN = "servicecomb.rest.servlet.urlPattern";

  public static final String SERVICECOMB_REST_ADDRESS = "servicecomb.rest.address";

  public static final String KEY_SERVICECOMB_REST_SERVER_TIMEOUT = "servicecomb.rest.server.timeout";

  public static final String DEFAULT_URL_PATTERN = "/*";

  private ServletConfig() {
  }

  public static long getServerTimeout() {
    DynamicLongProperty address =
        DynamicPropertyFactory.getInstance().getLongProperty(KEY_SERVICECOMB_REST_SERVER_TIMEOUT, DEFAULT_TIMEOUT);
    return address.get();
  }

  public static String getLocalServerAddress() {
    DynamicStringProperty address =
        DynamicPropertyFactory.getInstance().getStringProperty(SERVICECOMB_REST_ADDRESS, null);
    return address.get();
  }

  public static String getServletUrlPattern() {
    DynamicStringProperty address =
        DynamicPropertyFactory.getInstance().getStringProperty(KEY_SERVLET_URL_PATTERN, null);
    return address.get();
  }
}
