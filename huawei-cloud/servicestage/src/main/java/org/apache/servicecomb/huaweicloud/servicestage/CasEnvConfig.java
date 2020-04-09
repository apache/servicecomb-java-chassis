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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.netflix.config.DynamicPropertyFactory;

public class CasEnvConfig {
  private static final String CAS_BASE = "servicecomb.huaweicloud.servicestage.cas.";

  private static final String APPLICATION_ID = CAS_BASE + "application-id";

  private static final String COMPONENT_NAME = CAS_BASE + "component-name";

  private static final String INSTANCE_VERSION = CAS_BASE + "instance-version";

  private static final String INSTANCE_ID = CAS_BASE + "instance-id";

  private static final String ENVIRONMENT_ID = CAS_BASE + "environment-id";

  public static final CasEnvConfig INSTANCE = new CasEnvConfig();

  private Map<String, String> properties = new HashMap<>();

  private CasEnvConfig() {
    init();
  }

  private void init() {
    properties.put("CAS_APPLICATION_ID", DynamicPropertyFactory
        .getInstance().getStringProperty(APPLICATION_ID, EMPTY).get());
    properties.put("CAS_COMPONENT_NAME", DynamicPropertyFactory
        .getInstance().getStringProperty(COMPONENT_NAME, EMPTY).get());
    properties.put("CAS_INSTANCE_VERSION", DynamicPropertyFactory
        .getInstance().getStringProperty(INSTANCE_VERSION, EMPTY).get());
    properties.put("CAS_INSTANCE_ID", DynamicPropertyFactory
        .getInstance().getStringProperty(INSTANCE_ID, EMPTY).get());
    properties.put("CAS_ENVIRONMENT_ID", DynamicPropertyFactory
        .getInstance().getStringProperty(ENVIRONMENT_ID, EMPTY).get());
  }

  public Map<String, String> getNonEmptyProperties() {
    return properties.entrySet().stream().filter(entry -> StringUtils.isNotEmpty(entry.getValue()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
