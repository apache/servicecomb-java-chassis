/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.health;

public class HealthCheckResult {
  private boolean isHealth;

  private String information;

  private Object extraData;

  private long timestamp;

  public boolean isHealth() {
    return isHealth;
  }

  public String getInformation() {
    return information;
  }

  public Object getExtraData() {
    return extraData;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public HealthCheckResult() {
  }

  public HealthCheckResult(boolean isHealth, String information, Object extraData) {
    this();
    this.isHealth = isHealth;
    this.information = information;
    this.extraData = extraData;
    this.timestamp = System.currentTimeMillis();
  }
}
