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

import java.time.Duration;
import java.util.List;

public class FaultInjectionPolicy extends AbstractPolicy {
  public static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofMillis(0);

  private String type = "delay";

  private String delayTime = DEFAULT_TIMEOUT_DURATION.toString();

  private int percentage = -1;

  private int errorCode = -1;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDelayTime() {
    return delayTime;
  }

  public void setDelayTime(String delayTime) {
    this.delayTime = stringOfDuration(delayTime, Duration.ofMillis(-1));
  }

  public int getPercentage() {
    return percentage;
  }

  public void setPercentage(int percentage) {
    this.percentage = percentage;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public boolean isValid() {
    if (Duration.parse(delayTime).toMillis() < 0) {
      return false;
    }
    return super.isValid();
  }

  @Override
  public String toString() {
    return "FaultInjectionPolicy{" +
        "type=" + type +
        ", delayTime=" + delayTime +
        ", percentage=" + percentage +
        ", errorCode=" + errorCode +
        '}';
  }
}
