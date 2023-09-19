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

package org.apache.servicecomb.foundation.vertx;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.dns.AddressResolverOptions;

public class AddressResolverConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddressResolverConfig.class);

  /**
   * get the target endpoints with custom address resolve config
   * @param tag config tag, such as sc.consumer or cc.consumer
   * @return AddressResolverOptions
   */
  public static AddressResolverOptions getAddressResolverOptions(String tag) {
    AddressResolverOptions addressResolverOptions = new AddressResolverOptions();
    addressResolverOptions
        .setServers(getStringListProperty(
            AddressResolverOptions.DEFAULT_SERVERS,
            "addressResolver." + tag + ".servers",
            "addressResolver.servers"));
    addressResolverOptions
        .setOptResourceEnabled(getBooleanProperty(
            AddressResolverOptions.DEFAULT_OPT_RESOURCE_ENABLED,
            "addressResolver." + tag + ".optResourceEnabled",
            "addressResolver.optResourceEnabled"));
    addressResolverOptions
        .setCacheMinTimeToLive(getPositiveIntProperty(
            AddressResolverOptions.DEFAULT_CACHE_MIN_TIME_TO_LIVE,
            "addressResolver." + tag + ".cacheMinTimeToLive",
            "addressResolver.cacheMinTimeToLive"));
    addressResolverOptions
        .setCacheMaxTimeToLive(getPositiveIntProperty(
            AddressResolverOptions.DEFAULT_CACHE_MAX_TIME_TO_LIVE,
            "addressResolver." + tag + ".cacheMaxTimeToLive",
            "addressResolver.cacheMaxTimeToLive"));
    addressResolverOptions
        .setCacheNegativeTimeToLive(getPositiveIntProperty(
            AddressResolverOptions.DEFAULT_CACHE_NEGATIVE_TIME_TO_LIVE,
            "addressResolver." + tag + ".cacheNegativeTimeToLive",
            "addressResolver.cacheNegativeTimeToLive"));
    addressResolverOptions
        .setQueryTimeout(getPositiveIntProperty(
            AddressResolverOptions.DEFAULT_QUERY_TIMEOUT,
            "addressResolver." + tag + ".queryTimeout",
            "addressResolver.queryTimeout"));
    addressResolverOptions
        .setMaxQueries(getPositiveIntProperty(
            AddressResolverOptions.DEFAULT_MAX_QUERIES,
            "addressResolver." + tag + ".maxQueries",
            "addressResolver.maxQueries"));
    addressResolverOptions
        .setRdFlag(getBooleanProperty(
            AddressResolverOptions.DEFAULT_RD_FLAG,
            "addressResolver." + tag + ".rdFlag",
            "addressResolver.rdFlag"));
    addressResolverOptions
        .setSearchDomains(getStringListProperty(
            AddressResolverOptions.DEFAULT_SEACH_DOMAINS,
            "addressResolver." + tag + ".searchDomains",
            "addressResolver.searchDomains"));
    addressResolverOptions
        .setNdots(getPositiveIntProperty(
            AddressResolverOptions.DEFAULT_NDOTS,
            "addressResolver." + tag + ".ndots",
            "addressResolver.ndots"));
    addressResolverOptions
        .setRotateServers(getBooleanProperty(
            AddressResolverOptions.DEFAULT_ROTATE_SERVERS,
            "addressResolver." + tag + ".rotateServers",
            "addressResolver.rotateServers"));
    return addressResolverOptions;
  }

  private static List<String> getStringListProperty(List<String> defaultValue, String... keys) {
    for (String key : keys) {
      String[] vals = LegacyPropertyFactory.getProperty(key, String[].class);
      if (vals != null && vals.length > 0) {
        return Arrays.asList(vals);
      }
    }
    return defaultValue;
  }

  private static int getPositiveIntProperty(int defaultValue, String... keys) {
    for (String key : keys) {
      Integer val = LegacyPropertyFactory.getProperty(key, Integer.class);
      if (val != null && val <= 0) {
        LOGGER.warn("Address resover key:{}'s value:{} is not positive, please check!", key, val);
        continue;
      }
      if (val != null) {
        return val;
      }
    }
    return defaultValue;
  }

  private static boolean getBooleanProperty(boolean defaultValue,
      String... keys) {
    for (String key : keys) {
      Boolean val = LegacyPropertyFactory.getProperty(key, Boolean.class);
      if (val != null) {
        return val;
      }
    }
    return defaultValue;
  }
}
