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
package org.apache.servicecomb.metrics.core.meter.os.cpu;

import com.netflix.spectator.api.Id;

public abstract class AbstractCpuUsage {

  protected Id id;

  protected double usage;

  protected int cpuCount = Runtime.getRuntime().availableProcessors();

  static class Period {
    double last;

    double period;

    void update(double current) {
      period = current - last;
      last = current;
    }
  }

  public AbstractCpuUsage(Id id) {
    this.id = id;
  }

  public Id getId() {
    return id;
  }

  public double getUsage() {
    return usage;
  }

  public void setUsage(double usage) {
    this.usage = usage;
  }

  protected void updateUsage(double periodBusy, double periodTotal, boolean irixMode) {
    usage = periodTotal == 0 ? 0 : periodBusy / periodTotal;
    if (usage > 1) {
      usage = 1;
    }
    if (irixMode) {
      usage *= cpuCount;
    }
  }
}
