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

package org.apache.servicecomb.injection;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;

public class FaultInjectionConfig implements Serializable {

  private static final long serialVersionUID = -1651314684298456357L;

  private final String type;

  private final Duration delayTime;

  private final float percentage;

  private final int responseStatus;

  private FaultInjectionConfig(String type, Duration delayTime, float percentage,
      int responseStatus) {
    this.type = type;
    this.delayTime = delayTime;
    this.percentage = percentage;
    this.responseStatus = responseStatus;
  }

  public static Builder custom() {
    return new Builder();
  }

  public static class Builder {

    private String type;

    private Duration delayTime;

    private float percentage;

    private int responseStatus;

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public Builder setDelayTime(Duration delayTime) {
      this.delayTime = delayTime;
      return this;
    }

    public Builder setPercentage(float percentage) {
      this.percentage = percentage;
      return this;
    }

    public Builder setResponseStatus(int responseStatus) {
      this.responseStatus = responseStatus;
      return this;
    }

    public FaultInjectionConfig build() {
      return new FaultInjectionConfig(type, delayTime, percentage, responseStatus);
    }
  }
}
