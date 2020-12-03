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

package org.apache.servicecomb.qps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

public final class Config {
  private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

  public static final String STRATEGY_KEY = "servicecomb.flowcontrol.strategy";

  public static final String ANY_SERVICE = "ANY";

  public static final String CONSUMER_BUCKET_KEY_PREFIX = "servicecomb.flowcontrol.Consumer.qps.bucket.";

  public static final String PROVIDER_BUCKET_KEY_PREFIX = "servicecomb.flowcontrol.Provider.qps.bucket.";

  public static final String PROVIDER_BUCKET_KEY_GLOBAL =
      "servicecomb.flowcontrol.Provider.qps.global.bucket";

  public static final String CONSUMER_BUCKET_KEY_GLOBAL =
      "servicecomb.flowcontrol.Consumer.qps.global.bucket";

  public static final String CONSUMER_LIMIT_KEY_PREFIX = "servicecomb.flowcontrol.Consumer.qps.limit.";

  public static final String PROVIDER_LIMIT_KEY_PREFIX = "servicecomb.flowcontrol.Provider.qps.limit.";

  public static final String PROVIDER_LIMIT_KEY_GLOBAL =
      "servicecomb.flowcontrol.Provider.qps.global.limit";

  public static final String CONSUMER_LIMIT_KEY_GLOBAL =
      "servicecomb.flowcontrol.Consumer.qps.global.limit";

  public static final String CONSUMER_ENABLED = "servicecomb.flowcontrol.Consumer.qps.enabled";

  public static final String PROVIDER_ENABLED = "servicecomb.flowcontrol.Provider.qps.enabled";

  public static Config INSTANCE = new Config();

  private final DynamicBooleanProperty consumerEnabled =
      DynamicPropertyFactory.getInstance().getBooleanProperty(CONSUMER_ENABLED, true);

  private final DynamicBooleanProperty providerEnabled =
      DynamicPropertyFactory.getInstance().getBooleanProperty(PROVIDER_ENABLED, true);

  public Config() {
    consumerEnabled.addCallback(() -> {
      boolean newValue = consumerEnabled.get();
      LOGGER.info("{} changed from {} to {}", CONSUMER_ENABLED, consumerEnabled, newValue);
    });

    providerEnabled.addCallback(() -> {
      boolean newValue = providerEnabled.get();
      LOGGER.info("{} changed from {} to {}", PROVIDER_ENABLED, providerEnabled, newValue);
    });
  }

  public boolean isConsumerEnabled() {
    return consumerEnabled.get();
  }

  public boolean isProviderEnabled() {
    return providerEnabled.get();
  }
}
