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

package org.apache.servicecomb.metrics.core.custom;

/**
 WindowCounterService is complex service for manage Window Time-related Step Counter,
 It will output total,count,tps,rate,average,max and min
 examples:
 if record four time in one window,and window time = 2000 (2 seconds), like :
 record("Order Amount",100)
 record("Order Amount",200)
 record("Order Amount",300)
 record("Order Amount",400)

 Output metrics include:
 Order Amount.total = 1000
 Order Amount.count = 4
 Order Amount.tps = 2           count / time(second)
 Order Amount.rate = 500        total / time(second
 Order Amount.average = 250     total / count
 Order Amount.max = 400
 Order Amount.min = 100
 */
public interface WindowCounterService {
  void record(String name, long value);
}
