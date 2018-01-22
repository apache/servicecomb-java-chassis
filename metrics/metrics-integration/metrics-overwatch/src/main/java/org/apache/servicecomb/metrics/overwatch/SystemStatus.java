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

package org.apache.servicecomb.metrics.overwatch;

import java.util.Map;

public class SystemStatus {
  private final Integer time;

  private final String system;

  private final Map<String, Map<String, InstanceStatus>> stats;

  public Integer getTime() {
    return time;
  }

  public String getSystem() {
    return system;
  }

  public Map<String, Map<String, InstanceStatus>> getStats() {
    return stats;
  }

  public SystemStatus(Integer time, String system, Map<String, Map<String, InstanceStatus>> stats) {
    this.time = time;
    this.system = system;
    this.stats = stats;
  }


}
