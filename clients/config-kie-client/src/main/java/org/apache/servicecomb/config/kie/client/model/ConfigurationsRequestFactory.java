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

package org.apache.servicecomb.config.kie.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.http.client.common.HttpUtils;

public class ConfigurationsRequestFactory {
  private static final String KEY_APP = "app";

  private static final String KEY_ENVIRONMENT = "environment";

  private static final String KEY_SERVICE = "service";

  private static final int SERVICE_ORDER = 100;

  private static final int APP_ORDER = 200;

  private static final int CUSTOM_ORDER = 300;

  public static List<ConfigurationsRequest> buildConfigurationRequests(KieConfiguration configuration) {
    List<ConfigurationsRequest> result = new ArrayList<>();
    if (configuration.isEnableAppConfig()) {
      result.add(createAppConfigurationsRequest(configuration));
    }
    if (configuration.isEnableServiceConfig()) {
      result.add(createServiceConfigurationsRequest(configuration));
    }
    if (configuration.isEnableCustomConfig()) {
      result.add(createCustomConfigurationsRequest(configuration));
    }
    return result;
  }

  private static ConfigurationsRequest createAppConfigurationsRequest(KieConfiguration configuration) {
    return new ConfigurationsRequest()
        .setOrder(APP_ORDER)
        .setWithExact(true)
        .setLabelsQuery(buildLabelQuery(buildLabelQueryItem(KEY_APP, configuration.getAppName()),
            buildLabelQueryItem(KEY_ENVIRONMENT, configuration.getEnvironment())));
  }

  private static ConfigurationsRequest createServiceConfigurationsRequest(KieConfiguration configuration) {
    return new ConfigurationsRequest()
        .setOrder(SERVICE_ORDER)
        .setWithExact(true)
        .setLabelsQuery(buildLabelQuery(buildLabelQueryItem(KEY_APP, configuration.getAppName()),
            buildLabelQueryItem(KEY_SERVICE, configuration.getServiceName()),
            buildLabelQueryItem(KEY_ENVIRONMENT, configuration.getEnvironment())));
  }

  private static ConfigurationsRequest createCustomConfigurationsRequest(KieConfiguration configuration) {
    return new ConfigurationsRequest()
        .setOrder(CUSTOM_ORDER)
        .setWithExact(false)
        .setLabelsQuery(
            buildLabelQuery(buildLabelQueryItem(configuration.getCustomLabel(), configuration.getCustomLabelValue())));
  }

  private static String buildLabelQuery(String... labels) {
    StringBuilder result = new StringBuilder();
    for (String label : labels) {
      result.append(label);
      result.append("&");
    }
    return result.toString();
  }

  private static String buildLabelQueryItem(String key, String value) {
    try {
      return "label=" + HttpUtils.encodeURLParam(key + ":" + value);
    } catch (IOException e) {
      throw new IllegalArgumentException("unexpected param", e);
    }
  }
}
