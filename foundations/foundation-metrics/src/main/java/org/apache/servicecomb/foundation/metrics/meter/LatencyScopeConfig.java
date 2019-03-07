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
package org.apache.servicecomb.foundation.metrics.meter;

public class LatencyScopeConfig {
  // [min, max)
  // even max equals Long.MAX_VALUE, still not include it
  // because it will never happened in real cases
  private long msMin;

  private long msMax;

  public LatencyScopeConfig(long msMin, long msMax) {
    this.msMin = msMin;
    this.msMax = msMax;
  }

  public long getMsMin() {
    return msMin;
  }

  public long getMsMax() {
    return msMax;
  }

  @Override
  public String toString() {
    return "LatencyScopeConfig[" + msMin + ", " + msMax + ')';
  }
}
