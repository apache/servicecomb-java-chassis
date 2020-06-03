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

package org.apache.servicecomb.core.invocation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

public class ProducerInvocationFlowTest {
  class TestFlow extends ProducerInvocationFlow {
    public TestFlow(InvocationCreator invocationCreator) {
      super(invocationCreator);
    }

    @Override
    protected Invocation sendCreateInvocationException(Throwable throwable) {
      sendException = throwable;
      return null;
    }

    @Override
    protected void sendResponse(Invocation invocation, Response response) {
      sendInvocation = invocation;
    }
  }

  FilterNode filterNode = FilterNode.EMPTY;

  Throwable sendException;

  Invocation sendInvocation;

  @Injectable
  Invocation invocation;

  @Mocked
  MicroserviceMeta microserviceMeta;

  @Test
  public void should_send_exception_response_when_failed_to_create_invocation() {
    RuntimeException exception = new RuntimeExceptionWithoutStackTrace();
    TestFlow flow = new TestFlow(() -> {
      throw exception;
    });

    flow.run();

    assertThat(Exceptions.unwrap(sendException)).isSameAs(exception);
  }

  private void mockFilterChain() {
    new Expectations() {
      {
        microserviceMeta.getFilterChain();
        result = filterNode;
      }
    };
  }

  @Test
  public void should_start_invocation_when_succeed_to_create_invocation() {
    mockFilterChain();
    TestFlow flow = new TestFlow(() -> invocation);

    flow.run();

    new Verifications() {
      {
        invocation.onStart((HttpServletRequestEx) any, anyLong);
        times = 1;
      }
    };
  }

  @Test
  public void should_send_response_when_invocation_success() {
    mockFilterChain();
    TestFlow flow = new TestFlow(() -> invocation);

    flow.run();

    assertThat(sendInvocation).isSameAs(invocation);
  }

  @Test
  public void should_finish_invocation_when_invocation_success() {
    mockFilterChain();
    TestFlow flow = new TestFlow(() -> invocation);

    flow.run();

    new Verifications() {
      {
        invocation.onFinish((Response) any);
        times = 1;
      }
    };
  }

  private void mockInvocationFailed() {
    filterNode = new FilterNode((_invocation, _node) -> {
      throw new RuntimeExceptionWithoutStackTrace();
    });
    mockFilterChain();
  }

  @Test
  public void should_send_response_when_invocation_fail() {
    mockInvocationFailed();
    TestFlow flow = new TestFlow(() -> invocation);

    flow.run();

    assertThat(sendInvocation).isSameAs(invocation);
  }

  @Test
  public void should_finish_invocation_when_invocation_fail() {
    mockInvocationFailed();
    TestFlow flow = new TestFlow(() -> invocation);

    flow.run();

    new Verifications() {
      {
        invocation.onFinish((Response) any);
        times = 1;
      }
    };
  }
}