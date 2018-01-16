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

package org.apache.servicecomb.samples.metrics.custom;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.servicecomb.metrics.core.custom.CounterService;
import org.apache.servicecomb.metrics.core.custom.GaugeService;
import org.apache.servicecomb.metrics.core.custom.WindowCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShopDemoService {

  private final CounterService counterService;

  private final GaugeService gaugeService;

  private final WindowCounterService windowCounterService;

  //autowire metrics service
  @Autowired
  public ShopDemoService(CounterService counterService, GaugeService gaugeService,
      WindowCounterService windowCounterService) {
    this.counterService = counterService;
    this.gaugeService = gaugeService;
    this.windowCounterService = windowCounterService;
  }

  public void login(String name, String password) {
    counterService.increment("Active User");
  }

  public void logout(String session) {
    counterService.decrement("Active User");
  }

  public void order(double amount) throws InterruptedException {
    long start = System.currentTimeMillis();
    //sim  do order process
    Thread.sleep(100);

    //sim record order process time
    windowCounterService.record("Order Latency", System.currentTimeMillis() - start);

    windowCounterService.record("Order Count", 1);

    //only support long,please do unit convert ,$99.00 -> $9900 , $59.99 -> 5999
    windowCounterService.record("Order Amount", (long) round(amount * 100, 0));
  }

  public void discount(double value) {
    //make a discount to Levis Jeans

    gaugeService.update("Levis Jeans", value);
  }

  private double round(double value, int places) {
    if (!Double.isNaN(value)) {
      BigDecimal decimal = new BigDecimal(value);
      return decimal.setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
    return 0;
  }
}
