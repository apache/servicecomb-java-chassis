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

package org.apache.servicecomb.common.accessLog.ws;

import com.netflix.config.DynamicPropertyFactory;

public class WebSocketAccessLogConfig {
  private static final String BASE = "servicecomb.accesslog.ws.";

  private static final String SERVER_BASE = BASE + "server.";

  private static final String CLIENT_BASE = BASE + "client.";

  private static final String SERVER_LOG_ENABLED = SERVER_BASE + "enabled";

  private static final String CLIENT_LOG_ENABLED = CLIENT_BASE + "enabled";

  private boolean serverLogEnabled;

  private boolean clientLogEnabled;

  public static final WebSocketAccessLogConfig INSTANCE = new WebSocketAccessLogConfig();

  private WebSocketAccessLogConfig() {
    clientLogEnabled = DynamicPropertyFactory
        .getInstance().getBooleanProperty(CLIENT_LOG_ENABLED, false).get();
    serverLogEnabled = DynamicPropertyFactory
        .getInstance().getBooleanProperty(SERVER_LOG_ENABLED, false).get();
  }

  public boolean isServerLogEnabled() {
    return serverLogEnabled;
  }

  public boolean isClientLogEnabled() {
    return clientLogEnabled;
  }
}
