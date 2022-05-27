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
package org.apache.servicecomb.core.governance;

import com.netflix.config.DynamicPropertyFactory;

public class GovernanceConfiguration {
  public static final String ROOT = "servicecomb.loadbalance.";

  // retry configurations
  public static final String RETRY_ENABLED = "retryEnabled";

  public static final String RETRY_ON_NEXT = "retryOnNext";

  public static final String RETRY_ON_SAME = "retryOnSame";

  public static boolean isRetryEnabled(String microservice) {
    String p = getStringProperty("false",
        ROOT + microservice + "." + RETRY_ENABLED,
        ROOT + RETRY_ENABLED);
    return Boolean.parseBoolean(p);
  }

  public static int getRetryNextServer(String microservice) {
    return getRetryServer(microservice, RETRY_ON_NEXT);
  }

  public static int getRetrySameServer(String microservice) {
    return getRetryServer(microservice, RETRY_ON_SAME);
  }

  private static int getRetryServer(String microservice, String retryType) {
    final int defaultValue = 0;
    String p = getStringProperty("0",
        ROOT + microservice + "." + retryType,
        ROOT + retryType);
    try {
      int result = Integer.parseInt(p);
      if (result > 0) {
        return result;
      }
      return defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static String getStringProperty(String defaultValue, String... keys) {
    String property;
    for (String key : keys) {
      property = DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
      if (property != null) {
        return property;
      }
    }
    return defaultValue;
  }
}
