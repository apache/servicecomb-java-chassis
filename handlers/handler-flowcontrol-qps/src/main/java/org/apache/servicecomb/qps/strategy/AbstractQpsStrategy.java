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

package org.apache.servicecomb.qps.strategy;

import org.apache.servicecomb.qps.QpsStrategy;


public abstract class AbstractQpsStrategy implements QpsStrategy {

  private Long qpsLimit;

  private Long bucketLimit;

  private String key;

  public Long getBucketLimit() {
    return bucketLimit;
  }

  public void setBucketLimit(Long bucketLimit) {
    this.bucketLimit = bucketLimit;
  }

  @Override
  public abstract boolean isLimitNewRequest();

  @Override
  public abstract String name();

  public void setQpsLimit(Long qpsLimit) {
    this.qpsLimit = qpsLimit;
  }

  public Long getQpsLimit() {
    return qpsLimit;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
