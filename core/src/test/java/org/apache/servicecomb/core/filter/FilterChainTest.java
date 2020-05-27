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

package org.apache.servicecomb.core.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.filter.impl.ScheduleFilter;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class FilterChainTest {
  static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

  static String THREAD_NAME;

  @Mocked
  Invocation invocation;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  OperationConfig operationConfig;

  List<String> msg = new Vector<>();

  Filter recordThreadFilter = (invocation, nextNode) -> {
    msg.add(Thread.currentThread().getName());
    if (nextNode == null) {
      return CompletableFuture.completedFuture(Response.ok(null));
    }

    return nextNode.onFilter(invocation);
  };

  Filter scheduler = new ScheduleFilter();

  Filter exceptionFilter = (invocation, nextNode) -> {
    throw new IllegalStateException("e1");
  };

  @BeforeClass
  public static void beforeClass() {
    try {
      THREAD_NAME = EXECUTOR.submit(() -> Thread.currentThread().getName()).get();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void mockInvocation() {
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;

        operationMeta.getExecutor();
        result = EXECUTOR;

        operationConfig.getNanoRequestWaitInPoolTimeout(anyString);
        result = Long.MAX_VALUE;
      }
    };
  }

  @Test
  public void should_switch_thread_after_schedule() throws ExecutionException, InterruptedException {
    mockInvocation();

    FilterNode.buildChain(recordThreadFilter, scheduler, recordThreadFilter)
        .onFilter(invocation)
        .get();

    assertThat(msg).containsExactly("main", THREAD_NAME);
  }

  @Test
  public void should_stop_chain_when_first_filter_throw_exception() {
    ExecutionException executionException = (ExecutionException) catchThrowable(
        () -> FilterNode.buildChain(exceptionFilter, recordThreadFilter)
            .onFilter(invocation)
            .get());

    assertThat(msg).isEmpty();
    assertThat(executionException.getCause())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("e1");
  }

  @Test
  public void should_stop_chain_when_middle_filter_throw_exception() {
    ExecutionException executionException = (ExecutionException) catchThrowable(
        () -> FilterNode.buildChain(recordThreadFilter, exceptionFilter, recordThreadFilter)
            .onFilter(invocation)
            .get());

    assertThat(msg).containsExactly("main");
    assertThat(executionException.getCause())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("e1");
  }

  @Test
  public void should_support_retry_logic() {
    Filter exceptionFilter = (invocation, nextNode) -> {
      throw new CompletionException(new IOException("net error"));
    };
    SimpleRetryFilter retryFilter = new SimpleRetryFilter().setMaxRetry(3);

    ExecutionException executionException = (ExecutionException) catchThrowable(
        () -> FilterNode.buildChain(retryFilter, recordThreadFilter, exceptionFilter)
            .onFilter(invocation)
            .get());

    assertThat(msg).containsExactly("main", "main", "main");
    assertThat(executionException.getCause())
        .isInstanceOf(IOException.class)
        .hasMessage("net error");
  }
}