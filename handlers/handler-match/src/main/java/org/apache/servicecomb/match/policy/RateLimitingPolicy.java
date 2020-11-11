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
package org.apache.servicecomb.match.policy;

public class RateLimitingPolicy extends AbstractPolicy {

  public static final int DEFAULT_RATE = 1000;

  private Integer rate;

  public Integer getRate() {
    if (rate == null) {
      rate = DEFAULT_RATE;
    }
    return rate;
  }

  public void setRate(Integer rate) {
    this.rate = rate;
  }

  @Override
  public String handler() {
    return null;
  }

  @Override
  public String name() {
    return "RateLimitingPolicy";
  }
}
