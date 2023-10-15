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
package org.apache.servicecomb.huaweicloud.servicestage;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;

public class CasEnvConfig {
  private static final String APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

  private static final String SERVICE_PROPS = "SERVICECOMB_SERVICE_PROPS";

  private static final String INSTANCE_PROPS = "SERVICECOMB_INSTANCE_PROPS";

  public static final CasEnvConfig INSTANCE = new CasEnvConfig();

  private Map<String, String> parseProps(String value) {
    Map<String, String> rs = new HashMap<>();
    if (StringUtils.isEmpty(value)) {
      return rs;
    }
    return Arrays.stream(value.split(",")).map(v -> v.split(":"))
        .filter(v -> v.length == 2)
        .collect(Collectors.toMap(v -> v[0], v -> v[1]));
  }

  public Map<String, String> getNonEmptyInstanceProperties() {
    Map<String, String> map = new HashMap<>();

    map.put(APPLICATION_ID, LegacyPropertyFactory
        .getStringProperty(APPLICATION_ID, EMPTY));
    map.put(COMPONENT_NAME, LegacyPropertyFactory
        .getStringProperty(COMPONENT_NAME, EMPTY));
    map.put(INSTANCE_VERSION, LegacyPropertyFactory
        .getStringProperty(INSTANCE_VERSION, EMPTY));
    map.put(INSTANCE_ID, LegacyPropertyFactory
        .getStringProperty(INSTANCE_ID, EMPTY));
    map.put(ENVIRONMENT_ID, LegacyPropertyFactory
        .getStringProperty(ENVIRONMENT_ID, EMPTY));

    Map<String, String> instanceProps = map.entrySet().stream()
        .filter(entry -> StringUtils.isNotEmpty(entry.getValue()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    instanceProps.putAll(parseProps(LegacyPropertyFactory
        .getStringProperty(INSTANCE_PROPS, EMPTY)));

    return instanceProps;
  }

  public Map<String, String> getNonEmptyServiceProperties() {
    return parseProps(LegacyPropertyFactory
        .getStringProperty(SERVICE_PROPS, EMPTY));
  }
}
