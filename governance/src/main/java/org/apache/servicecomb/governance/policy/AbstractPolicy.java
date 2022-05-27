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
import org.apache.servicecomb.governance.utils.GovernanceUtils;

import java.time.Duration;

public abstract class AbstractPolicy extends Configurable {

  @Override
  public boolean isValid() {
    return !StringUtils.isEmpty(name);
  }

  private Duration parseToDuration(String time, Duration defaultValue) {
    if (StringUtils.isEmpty(time)) {
      return defaultValue;
    }
    if (time.matches(GovernanceUtils.DIGIT_REGEX)) {
      if (Long.parseLong(time) < 0) {
        throw new RuntimeException("The value of time should not be less than 0.");
      }
      return Duration.ofMillis(Long.parseLong(time));
    }
    return Duration.parse(GovernanceUtils.DIGIT_PREFIX + time);
  }

  public String stringOfDuration(String time, Duration defaultValue) {
    return parseToDuration(time, defaultValue).toString();
  }
}
