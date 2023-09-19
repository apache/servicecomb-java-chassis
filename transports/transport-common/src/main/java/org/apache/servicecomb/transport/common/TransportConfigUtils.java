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
package org.apache.servicecomb.transport.common;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransportConfigUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransportConfigUtils.class);

  private TransportConfigUtils() {
  }

  // old verticle count key is ambiguous
  // suggest to use new name
  public static int readVerticleCount(String key, String deprecatedKey) {
    int count = LegacyPropertyFactory.getIntProperty(key, -1);
    if (count > 0) {
      return count;
    }

    count = LegacyPropertyFactory.getIntProperty(deprecatedKey, -1);
    if (count > 0) {
      LOGGER.warn("{} is ambiguous, and deprecated, recommended to use {}.", deprecatedKey, key);
      return count;
    }

    // default value
    count = Math.min(Runtime.getRuntime().availableProcessors(), 8);
    LOGGER.info("{} not defined, set to {}.", key, count);
    return count;
  }
}
