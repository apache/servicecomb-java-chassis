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

package io.servicecomb.edge.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.serviceregistry.version.VersionRule;
import io.servicecomb.serviceregistry.version.VersionRuleUtils;

public class CompatiblePathVersionMapper {
  // v1 -> 1.0.0-2.0.0
  // v2 -> 2.0.0-3.0.0
  private Map<String, VersionRule> mapper = new ConcurrentHashMap<>();

  public VersionRule getOrCreate(String pathVersion) {
    return mapper.computeIfAbsent(pathVersion, pv -> {
      return createVersionRule(pathVersion);
    });
  }

  // v + number
  protected VersionRule createVersionRule(String pathVersion) {
    if (StringUtils.isEmpty(pathVersion) || Character.toUpperCase(pathVersion.charAt(0)) != 'V') {
      throw new ServiceCombException(
          String.format("pathVersion \"%s\" is invalid, format must be v+number or V+number.", pathVersion));
    }

    int number = 0;
    try {
      number = Integer.parseInt(pathVersion.substring(1));
    } catch (NumberFormatException e) {
      throw new ServiceCombException(
          String.format("pathVersion \"%s\" is invalid, format must be v+number or V+number.", pathVersion), e);
    }

    if (number < 0 || number > Short.MAX_VALUE) {
      throw new ServiceCombException(
          String.format("pathVersion \"%s\" is invalid, version range is [0, %d].", pathVersion, Short.MAX_VALUE));
    }

    if (number == Short.MAX_VALUE) {
      return VersionRuleUtils.getOrCreate(String.format("%d.0.0+", number));
    }

    return VersionRuleUtils.getOrCreate(String.format("%d.0.0-%d.0.0", number, number + 1));
  }
}
