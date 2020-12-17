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
package com.huaweicloud.governance.handler;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huaweicloud.governance.handler.ext.RetryExtension;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RetryPolicy;

import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

@Component("RetryHandler")
public class RetryHandler extends AbstractGovHandler<Retry> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);

  @Autowired
  private RetryExtension retryExtension;

  /**
   * @param supplier
   * @param policy
   * @return
   */
  @Override
  public <RESULT> DecorateCheckedSupplier<RESULT> process(DecorateCheckedSupplier<RESULT> supplier, Policy policy) {
    Retry retry = getActuator("servicecomb.retry." + policy.name(), (RetryPolicy) policy, this::getRetry);
    return supplier.withRetry(retry);
  }

  @Override
  public HandlerType type() {
    return HandlerType.CLIENT;
  }

  private Retry getRetry(RetryPolicy retryPolicy) {
    List<Integer> statusList = Arrays.stream(retryPolicy.getRetryOnResponseStatus().split(","))
        .map(Integer::parseInt).collect(Collectors.toList());

    RetryConfig config = RetryConfig.<List<Integer>>custom()
        .maxAttempts(retryPolicy.getMaxAttempts())
        .retryOnResult(getPredicate(statusList))
        .retryExceptions(retryExtension.retryExceptions())
        .waitDuration(Duration.ofMillis(retryPolicy.getWaitDuration()))
        .build();

    RetryRegistry registry = RetryRegistry.of(config);
    return registry.retry(retryPolicy.name());
  }

  private Predicate<List<Integer>> getPredicate(List<Integer> statusList) {
    return response -> retryExtension.isRetry(statusList);
  }
}
