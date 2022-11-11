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

package org.apache.servicecomb.governance.policy;

import java.time.Duration;

public class GovernanceCachePolicy extends AbstractPolicy {
  public static final Duration DEFAULT_TTL = Duration.ofMillis(21600000);

  public static final long DEFAULT_MAXIMUM_SIZE = 60000;

  public static final int DEFAULT_CONCURRENCY_LEVEL = 8;

  private String ttl = DEFAULT_TTL.toString();

  private long maximumSize = DEFAULT_MAXIMUM_SIZE;

  private int concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;

  public String getTtl() {
    return ttl;
  }

  public void setTtl(String ttl) {
    this.ttl = stringOfDuration(ttl, DEFAULT_TTL);
  }

  public Long getMaximumSize() {
    return maximumSize;
  }

  public void setMaximumSize(Long maximumSize) {
    this.maximumSize = maximumSize;
  }

  public int getConcurrencyLevel() {
    return concurrencyLevel;
  }

  public void setConcurrencyLevel(int concurrencyLevel) {
    this.concurrencyLevel = concurrencyLevel;
  }

  @Override
  public String toString() {
    return "CachePolicy{" + "ttl=" + ttl + ",concurrencyLevel=" + concurrencyLevel + ", maximumSize=" + maximumSize
        + '}';
  }
}
