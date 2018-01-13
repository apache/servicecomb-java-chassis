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

  public static final String CONSUMER_LIMIT_KEY_PREFIX = "cse.flowcontrol.Consumer.qps.limit.";

  public static final String PROVIDER_LIMIT_KEY_PREFIX = "cse.flowcontrol.Provider.qps.limit.";

  public static final String PROVIDER_LIMIT_KEY_GLOBAL =
      "cse.flowcontrol.Provider.qps.global.limit";

  public static final String CONSUMER_ENABLED = "cse.flowcontrol.Consumer.qps.enabled";

  public static final String PROVIDER_ENABLED = "cse.flowcontrol.Provider.qps.enabled";

  public static final Config INSTANCE = new Config();

  private final DynamicBooleanProperty consumerEanbled =
      DynamicPropertyFactory.getInstance().getBooleanProperty(CONSUMER_ENABLED, true);

  private final DynamicBooleanProperty providerEanbled =
      DynamicPropertyFactory.getInstance().getBooleanProperty(PROVIDER_ENABLED, true);

  private Config() {
    consumerEanbled.addCallback(() -> {
      boolean newValue = consumerEanbled.get();
      LOGGER.info("{} changed from {} to {}", CONSUMER_ENABLED, consumerEanbled, newValue);
    });

    providerEanbled.addCallback(() -> {
      boolean newValue = providerEanbled.get();
      LOGGER.info("{} changed from {} to {}", PROVIDER_ENABLED, providerEanbled, newValue);
    });
  }

  public boolean isConsumerEnabled() {
    return consumerEanbled.get();
  }

  public boolean isProviderEnabled() {
    return providerEanbled.get();
  }
}
