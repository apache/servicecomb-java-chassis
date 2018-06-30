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
package org.apache.servicecomb.serviceregistry.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * monitor ServiceCenterTask status and print diagnosis data
 */
public class ServiceCenterTaskMonitor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterTaskMonitor.class);

  private static final long MAX_TIME_TAKEN = 1000;

  private long beginTime = 0;

  private long lastEndTime = 0;

  private int interval = -1;

  public void beginCycle(int interval) {
    this.beginTime = System.currentTimeMillis();
    if (this.interval != interval) {
      LOGGER.info("sc task interval changed from {} to {}", this.interval, interval);
      this.interval = interval;
    } else {
      if (this.beginTime - this.lastEndTime > interval * 1000 + MAX_TIME_TAKEN) {
        LOGGER.warn("sc task postponed for {}ms for some reason.", this.beginTime - this.lastEndTime);
      }
    }
  }

  public void endCycle() {
    this.lastEndTime = System.currentTimeMillis();
    if (this.lastEndTime - this.beginTime > MAX_TIME_TAKEN) {
      LOGGER.warn("sc task taken more than {}ms to execute", this.lastEndTime - this.beginTime);
    }
  }
}
