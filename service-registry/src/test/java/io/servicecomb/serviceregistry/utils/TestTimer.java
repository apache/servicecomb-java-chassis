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

package io.servicecomb.serviceregistry.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by   on 2017/3/13.
 */
public class TestTimer {
  @Test(expected = TimerException.class)
  public void testTimer() throws TimerException {
    Timer timer = new Timer();
    Assert.assertEquals(Timer.DEFAULT_MAX_TIMEOUT, timer.getMax());
    Assert.assertEquals(Timer.DEFAULT_RETRY_TIMES, timer.getTimes());
    Assert.assertEquals(0, timer.getCurrent());
    Assert.assertEquals((timer.getCurrent() + 1) * Timer.DEFAULT_STEP_SIZE, timer.nextTimeout());
    Assert.assertEquals((timer.getCurrent() + 1) * Timer.DEFAULT_STEP_SIZE, timer.getNextTimeout());

    Timer timer1 = new Timer(1, 2, 3, false);
    Timer timer2 = new Timer(1);
    timer2.reset();
    @SuppressWarnings("unused")
    Timer timer3 = new Timer(1, 2);
    Assert.assertNotNull(Timer.newDefaultTimer());
    Assert.assertNotNull(Timer.newForeverTimer());
    Assert.assertEquals(1, timer1.nextTimeout());
    Assert.assertEquals(1, timer1.nextTimeout());
    timer1.sleep();
  }
}
