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

package org.apache.servicecomb.qps;

import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.qps.strategy.LeakyBucketStrategy;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author GuoYl123
 * @Date 2020/7/16
 **/
public class TestQpsStrategy {

  @Test
  public void testFixedWindowStrategy() {
    AbstractQpsStrategy qpsStrategy = new FixedWindowStrategy();
    qpsStrategy.setKey("abc");
    qpsStrategy.setQpsLimit(100L);
    Assert.assertEquals(false, qpsStrategy.isLimitNewRequest());

    qpsStrategy.setQpsLimit(1L);
    Assert.assertEquals(true, qpsStrategy.isLimitNewRequest());
  }


  @Test
  public void testLeakyBucketStrategy() {
    LeakyBucketStrategy qpsStrategy = new LeakyBucketStrategy();
    qpsStrategy.setKey("abc");
    qpsStrategy.setQpsLimit(100L);
    Assert.assertEquals(false, qpsStrategy.isLimitNewRequest());

    qpsStrategy.setQpsLimit(1L);
    qpsStrategy.setBucketLimit(1L);
    Assert.assertEquals(true, qpsStrategy.isLimitNewRequest());
  }

}
