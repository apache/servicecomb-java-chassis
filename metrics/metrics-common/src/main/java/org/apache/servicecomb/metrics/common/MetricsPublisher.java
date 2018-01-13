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

package org.apache.servicecomb.metrics.common;

import java.util.List;

public interface MetricsPublisher {
  /**  What's the WindowTime ?
   We all know there are two major type of metric :
   1.Time-unrelated,you can get the latest value any time immediately:
   Counter -> increase or decrease
   Guage -> set a certain one value
   2.Time-related,only after a centain time pass you can compute the right value,"a centain time" called WindowTime
   Max & Min -> the max value or min value in a centain time
   Average -> average value, the simplest algorithm is f = sum / count
   Rate -> like TPS,algorithm is f = sum / second
  
   Will be return "servicecomb.metrics.window_time" setting in microservice.yaml
   */
  List<Long> getAppliedWindowTime();

  //same as getRegistryMetric({first setting windowTime})
  RegistryMetric metrics();

  /**
   * windowTime usage example:
   * if there is two window time set in "servicecomb.metrics.window_time" like 1000,2000
   * then windowTime = 1000 will return result of the setting 1000(1 second)
   * windowTime = 2000 will return result of the setting 2000(2 second)
   *
   * there are three monitor of max,min,total
   * 0----------1----------2----------3----------  <-time line (second)
   *   100,200    300,400                          <-value record
   *
   *                 ↑ getRegistryMetric(1000) will return max=200 min=100 total=300
   *                   getRegistryMetric(2000) will return max=0 min=0 total=0
   *                             ↑ getRegistryMetric(1000) will return max=300 min=400 total=700
   *                               getRegistryMetric(2000) will return max=400 min=100 total=1000
   *
   * @param windowTime getAppliedWindowTime() item
   * @return RegistryMetric
   */
  RegistryMetric metricsWithWindowTime(long windowTime);
}
