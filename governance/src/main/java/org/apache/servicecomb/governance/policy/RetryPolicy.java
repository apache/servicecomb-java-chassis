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

import org.springframework.util.StringUtils;

public class RetryPolicy extends AbstractPolicy {

  public static final int DEFAULT_MAX_ATTEMPTS = 3;

  public static final int DEFAULT_WAIT_DURATION = 1;

  public static final String DEFAULT_RETRY_ON_RESPONSE_STATUS = "502";

  //最多尝试次数
  private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

  //每次重试尝试等待的时间，默认给1。 在异步场景下，这个值必须大于0，否则不会重试。
  private int waitDuration = DEFAULT_WAIT_DURATION;

  //需要重试的http status, 逗号分隔
  private String retryOnResponseStatus;

  public String getRetryOnResponseStatus() {
    if (StringUtils.isEmpty(retryOnResponseStatus)) {
      retryOnResponseStatus = DEFAULT_RETRY_ON_RESPONSE_STATUS;
    }
    return retryOnResponseStatus;
  }

  public void setRetryOnResponseStatus(String retryOnResponseStatus) {
    this.retryOnResponseStatus = retryOnResponseStatus;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public int getWaitDuration() {
    return waitDuration;
  }

  public void setWaitDuration(int waitDuration) {
    this.waitDuration = waitDuration;
  }

  @Override
  public boolean isValid() {
    if (maxAttempts < 1) {
      return false;
    }
    if (waitDuration < 0) {
      return false;
    }
    return super.isValid();
  }

  @Override
  public String toString() {
    return "RetryPolicy{" +
        "maxAttempts=" + maxAttempts +
        ", waitDuration=" + waitDuration +
        ", retryOnResponseStatus='" + retryOnResponseStatus + '\'' +
        '}';
  }
}
