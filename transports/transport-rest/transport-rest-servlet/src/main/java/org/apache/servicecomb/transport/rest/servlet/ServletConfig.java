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

import org.apache.servicecomb.config.LegacyPropertyFactory;

public final class ServletConfig {
  public static final long DEFAULT_ASYN_SERVLET_TIMEOUT = -1;

  public static final String KEY_SERVLET_URL_PATTERN = "servicecomb.rest.servlet.urlPattern";

  public static final String SERVICECOMB_REST_ADDRESS = "servicecomb.rest.address";

  public static final String KEY_SERVICECOMB_ASYC_SERVLET_TIMEOUT = "servicecomb.rest.server.timeout";

  public static final String DEFAULT_URL_PATTERN = "/*";

  private ServletConfig() {
  }

  public static long getAsyncServletTimeout() {
    return LegacyPropertyFactory.getLongProperty(KEY_SERVICECOMB_ASYC_SERVLET_TIMEOUT,
        DEFAULT_ASYN_SERVLET_TIMEOUT);
  }

  public static String getLocalServerAddress() {
    return LegacyPropertyFactory.getStringProperty(SERVICECOMB_REST_ADDRESS, null);
  }

  public static String getServletUrlPattern() {
    return LegacyPropertyFactory.getStringProperty(KEY_SERVLET_URL_PATTERN, null);
  }
}
