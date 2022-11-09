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

package org.apache.servicecomb.governance.processor.injection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fault injection parameters which decides the fault injection condition.
 */
public class FaultParam {
  private static final Logger LOGGER = LoggerFactory.getLogger(FaultParam.class);

  private final long reqCount;

  private Sleepable sleepable = (delay) -> {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      LOGGER.info("Interrupted exception is received");
    }
  };

  public long getReqCount() {
    return reqCount;
  }

  public FaultParam(long reqCount) {
    this.reqCount = reqCount;
  }

  public Sleepable getSleepable() {
    return sleepable;
  }

  public void setSleepable(Sleepable sleepable) {
    this.sleepable = sleepable;
  }
}
