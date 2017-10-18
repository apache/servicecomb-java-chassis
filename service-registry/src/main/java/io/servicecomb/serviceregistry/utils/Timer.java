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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by   on 2017/3/12.
 */
public class Timer {
  public static final int DEFAULT_MAX_TIMEOUT = 60;

  public static final int DEFAULT_RETRY_TIMES = 3;

  public static final int DEFAULT_STEP_SIZE = 10;

  private int max = 0;

  private int times = 0; // when less then zero then timing forever

  private int stepSize = 0;

  private boolean increase = true;

  private AtomicInteger current = new AtomicInteger(0);

  public static Timer newDefaultTimer() {
    return new Timer();
  }

  public static Timer newForeverTimer() {
    return new Timer(0, -1);
  }

  public Timer() {
    this(DEFAULT_MAX_TIMEOUT, DEFAULT_RETRY_TIMES, DEFAULT_STEP_SIZE, true);
  }

  public Timer(int max) {
    this(max, DEFAULT_RETRY_TIMES, DEFAULT_STEP_SIZE, true);
  }

  public Timer(int max, int times) {
    this(max, times, DEFAULT_STEP_SIZE, true);
  }

  public Timer(int max, int times, int stepSize, boolean increase) {
    this.max = (max <= 0 ? DEFAULT_MAX_TIMEOUT : max);
    this.times = times;
    this.stepSize = (stepSize <= 0 ? DEFAULT_STEP_SIZE : stepSize);
    this.increase = increase;
  }

  public int getMax() {
    return this.max;
  }

  public int getTimes() {
    return this.times;
  }

  public int getCurrent() {
    return this.current.get();
  }

  public int getNextTimeout() {
    if (this.increase) {
      int timeout = (this.current.get() + 1) * this.stepSize;
      return (timeout > this.max ? this.max : timeout);
    }
    return (this.stepSize > this.max ? this.max : this.stepSize);
  }

  public int nextTimeout() {
    int current = this.current.incrementAndGet();
    if (this.increase) {
      int timeout = current * this.stepSize;
      return (timeout > this.max ? this.max : timeout);
    }
    return (this.stepSize > this.max ? this.max : this.stepSize);
  }

  public void sleep() throws TimerException {
    if (this.times >= 0 && this.times <= this.current.get()) {
      throw new TimerException();
    }
    try {
      TimeUnit.SECONDS.sleep(nextTimeout());
    } catch (InterruptedException ignored) {
    }
  }

  public void reset() {
    this.current.set(0);
  }
}
