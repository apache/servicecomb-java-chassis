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

package org.apache.servicecomb.serviceregistry.api.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by   on 2017/1/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthCheck {
  private HealthCheckMode mode;

  private int port;

  private int interval;

  private int times;

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public int getTimes() {
    return times;
  }

  public void setTimes(int times) {
    this.times = times;
  }

  public HealthCheckMode getMode() {
    return mode;
  }

  public void setMode(HealthCheckMode mode) {
    this.mode = mode;
  }

  public int getTTL() {
    if (this.mode != HealthCheckMode.HEARTBEAT) {
      return 0;
    }
    return getInterval() * (getTimes() + 1);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
