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

package org.apache.servicecomb.foundation.log;

import com.netflix.config.DynamicPropertyFactory;

public class LogConfig {

    private static final String SERVER_BASE = "servicecomb.accesslog.";

    private static final String CLIENT_BASE = "servicecomb.outlog.";

    private static final String SERVER_LOG_ENABLED = SERVER_BASE + "enabled";

    private static final String SERVER_LOG_PATTERN = SERVER_BASE + "pattern";

    private static final String CLIENT_LOG_ENABLED = CLIENT_BASE + "enabled";

    private static final String CLIENT_LOG_PATTERN = CLIENT_BASE + "pattern";

    private static final String DEFAULT_SERVER_PATTERN = "%h - - %t %r %s %B %D";

    private static final String DEFAULT_CLIENT_PATTERN = "%h %SCB-transport - - %t %r %s %D";

    public static final LogConfig INSTANCE = new LogConfig();

    private boolean serverLogEnabled;

    private boolean clientLogEnabled;

    private String serverLogPattern;

    private String clientLogPattern;

    private LogConfig() {
        init();
    }

    private void init() {
        clientLogEnabled = DynamicPropertyFactory
            .getInstance().getBooleanProperty(CLIENT_LOG_ENABLED, false).get();
        serverLogEnabled = DynamicPropertyFactory
            .getInstance().getBooleanProperty(SERVER_LOG_ENABLED, false).get();
        clientLogPattern = DynamicPropertyFactory
          .getInstance().getStringProperty(CLIENT_LOG_PATTERN, DEFAULT_CLIENT_PATTERN).get();
        serverLogPattern = DynamicPropertyFactory
            .getInstance().getStringProperty(SERVER_LOG_PATTERN, DEFAULT_SERVER_PATTERN).get();
    }

    public boolean isServerLogEnabled() {
        return serverLogEnabled;
    }

    public boolean isClientLogEnabled() {
        return clientLogEnabled;
    }

    public String getServerLogPattern() {
        return serverLogPattern;
    }

    public String getClientLogPattern() {
        return clientLogPattern;
    }
}
