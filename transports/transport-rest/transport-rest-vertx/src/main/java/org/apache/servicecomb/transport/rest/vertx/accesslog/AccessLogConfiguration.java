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

package org.apache.servicecomb.transport.rest.vertx.accesslog;

import com.netflix.config.DynamicPropertyFactory;

public final class AccessLogConfiguration {

  private static final String BASE = "servicecomb.accesslog.";

  private static final String ACCESSLOG_ENABLED = BASE + "enabled";

  private static final String ACCESSLOG_PATTERN = BASE + "pattern";

  public static final AccessLogConfiguration INSTANCE = new AccessLogConfiguration();

  public static final String DEFAULT_PATTERN = "%h - - %t %r %s %B %D";

  private AccessLogConfiguration() {

  }

  public boolean getAccessLogEnabled() {
    return getBooleanProperty(false, ACCESSLOG_ENABLED);
  }

  public String getAccesslogPattern() {
    return getProperty(DEFAULT_PATTERN, ACCESSLOG_PATTERN);
  }

  private String getProperty(String defaultValue, String key) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
  }

  private boolean getBooleanProperty(boolean defaultValue, String key) {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(key, defaultValue).get();
  }
}
