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

package org.apache.servicecomb.foundation.metrics.performance;

/**
 * PerfStatContext
 *
 *
 */
public class PerfStatContext {
  // 调用开始时间，用于统计
  protected long callBegin;

  // 本次统计涉及消息数量
  private int msgCount;

  public PerfStatContext() {
    reset();
  }

  public long getLatency() {
    return System.currentTimeMillis() - callBegin;
  }

  public int getMsgCount() {
    return msgCount;
  }

  public void setMsgCount(int msgCount) {
    this.msgCount = msgCount;
  }

  public void reset() {
    callBegin = System.currentTimeMillis();
    msgCount = 0;
  }
}
