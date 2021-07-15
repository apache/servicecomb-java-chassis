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
package org.apache.servicecomb.governance.policy;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.entity.Configurable;
import org.apache.servicecomb.governance.properties.GovernanceProperties;
import org.apache.servicecomb.governance.utils.GovernanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;

public abstract class AbstractPolicy extends Configurable {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPolicy.class);

  @Override
  public boolean isValid() {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    return true;
  }

  public Duration parseToDuration(String time, Duration defaultValue) {
    if (StringUtils.isEmpty(time)) {
      return defaultValue;
    }
    if(time.matches(GovernanceUtils.DIGIT_REGEX)) {
      return Duration.ofMillis(Integer.valueOf(time));
    }
    try {
      return Duration.parse(GovernanceUtils.DIGIT_PREFIX+time);
    } catch (DateTimeParseException e){
      LOGGER.warn("Parsed time to be a Duration failed. It will use the default value.");
    }
    return defaultValue;
  }
}
