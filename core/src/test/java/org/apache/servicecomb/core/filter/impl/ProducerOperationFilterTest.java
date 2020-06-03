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

package org.apache.servicecomb.core.filter.impl;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

public class ProducerOperationFilterTest {
  ProducerOperationFilter filter = new ProducerOperationFilter();

  @Injectable
  Invocation invocation;

  @Mocked
  SwaggerProducerOperation producerOperation;

  static InvocationContext threadInvocationContext;

  public static class Controller {
    public void sync() {
      threadInvocationContext = ContextUtils.getInvocationContext();
    }

    public void syncException() {
      throw new RuntimeExceptionWithoutStackTrace("syncException");
    }

    public CompletableFuture<Void> async() {
      threadInvocationContext = ContextUtils.getInvocationContext();
      return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> asyncException() {
      throw new RuntimeExceptionWithoutStackTrace("asyncException");
    }
  }

  @Test
  public void should_record_invocation_trace_time() throws NoSuchMethodException {
    setInvokeSyncMethod();
    filter.onFilter(invocation, FilterNode.EMPTY);

    new Verifications() {
      {
        invocation.onBusinessMethodStart();
        times = 1;

        invocation.onBusinessMethodFinish();
        times = 1;

        invocation.onBusinessFinish();
        times = 1;
      }
    };
  }

  private void setInvokeMethod(String name) throws NoSuchMethodException {
    Controller instance = new Controller();
    Method method = instance.getClass().getMethod(name);
    new Expectations() {
      {
        producerOperation.getProducerInstance();
        result = instance;

        producerOperation.getProducerMethod();
        result = method;
      }
    };
  }

  private void setInvokeSyncMethod() throws NoSuchMethodException {
    setInvokeMethod("sync");
  }

  private void setInvokeAsyncMethod() throws NoSuchMethodException {
    setInvokeMethod("async");
  }

  @Test
  public void should_provide_thread_local_invocation_context_for_sync_method() throws NoSuchMethodException {
    setInvokeSyncMethod();

    filter.onFilter(invocation, FilterNode.EMPTY);

    assertThat(threadInvocationContext).isSameAs(invocation);
  }

  @Test
  public void should_clear_thread_local_invocation_context_after_sync_method() throws NoSuchMethodException {
    setInvokeSyncMethod();

    filter.onFilter(invocation, FilterNode.EMPTY);

    assertThat(ContextUtils.getInvocationContext()).isNull();
  }

  @Test
  public void should_catch_sync_business_exception() throws NoSuchMethodException {
    setInvokeMethod("syncException");

    CompletableFuture<Response> future = filter.onFilter(invocation, FilterNode.EMPTY);

    assertThat(future).hasFailedWithThrowableThat()
        .isInstanceOf(RuntimeExceptionWithoutStackTrace.class)
        .hasMessage("syncException");
  }

  @Test
  public void should_provide_thread_local_invocation_context_for_async_method() throws NoSuchMethodException {
    setInvokeAsyncMethod();

    filter.onFilter(invocation, FilterNode.EMPTY);

    assertThat(threadInvocationContext).isSameAs(invocation);
  }

  @Test
  public void should_clear_thread_local_invocation_context_after_async_method() throws NoSuchMethodException {
    setInvokeAsyncMethod();

    filter.onFilter(invocation, FilterNode.EMPTY);

    assertThat(ContextUtils.getInvocationContext()).isNull();
  }

  @Test
  public void should_catch_async_business_exception() throws NoSuchMethodException {
    setInvokeMethod("asyncException");

    CompletableFuture<Response> future = filter.onFilter(invocation, FilterNode.EMPTY);

    assertThat(future).hasFailedWithThrowableThat()
        .isInstanceOf(RuntimeExceptionWithoutStackTrace.class)
        .hasMessage("asyncException");
  }

  @Test
  public void should_unify_IllegalArgumentException_message_when_convert_exception() throws NoSuchMethodException {
    setInvokeSyncMethod();
    new Expectations() {
      {
        invocation.toProducerArguments();
        result = new Object[] {1};
      }
    };

    CompletableFuture<Response> future = filter.onFilter(invocation, FilterNode.EMPTY);
    assertThat(future).hasFailedWithThrowableThat()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("wrong number of arguments");

    InvocationException throwable = Exceptions
        .convert(invocation, catchThrowable(() -> future.get()), INTERNAL_SERVER_ERROR);
    assertThat(throwable).hasCauseInstanceOf(IllegalArgumentException.class);
    CommonExceptionData data = (CommonExceptionData) throwable.getErrorData();
    assertThat(data.getMessage()).isEqualTo("Parameters not valid or types not match.");
  }
}