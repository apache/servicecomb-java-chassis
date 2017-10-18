/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.loadbalance;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.servicecomb.swagger.invocation.exception.InvocationException;

public class TestLoadBalanceRetryHandler {

  private final class DummyException extends RuntimeException {

    DummyException(String message, Throwable e) {
      super(message, e);
    }
  }

  @Test
  public void testRetryOnSameOperations() {
    List<Class<? extends Throwable>> appendExceptions = Lists
        .newArrayList(new Class[]{InvocationException.class, IllegalAccessException.class});

    LoadBalanceRetryHandler loadBalanceRetryHandler = new LoadBalanceRetryHandler(1, 2, true);
    Assert.assertThat(loadBalanceRetryHandler.getMaxRetriesOnSameServer(), is(1));
    Assert.assertThat(loadBalanceRetryHandler.getMaxRetriesOnNextServer(), is(2));

    loadBalanceRetryHandler.setRetryOnSameExceptions(appendExceptions);
    List<Class<? extends Throwable>> retryOnSameExceptions = loadBalanceRetryHandler.getRetryOnSameExceptions();
    Assert.assertThat(retryOnSameExceptions, hasItem(InvocationException.class));
    Assert.assertThat(retryOnSameExceptions, hasItem(IllegalAccessException.class));

    Assert.assertThat(retryOnSameExceptions.size(), is(4));
    Assert.assertThat(loadBalanceRetryHandler.removeRetryOnSameException(InvocationException.class), is(true));
    Assert.assertThat(loadBalanceRetryHandler.removeRetryOnSameException(SocketException.class), is(false));
    Assert.assertThat(retryOnSameExceptions.size(), is(3));
    Assert.assertThat(retryOnSameExceptions,
        contains(SocketTimeoutException.class, ConnectException.class, IllegalAccessException.class));
  }

  @Test
  public void testIsRetriableException() {
    LoadBalanceRetryHandler loadBalanceRetryHandlerWithRetryEnabled = new LoadBalanceRetryHandler(
        1, 2, true);
    LoadBalanceRetryHandler loadBalanceRetryHandler1WithRetryDisabled = new LoadBalanceRetryHandler(
        1, 2, false);

    Assert.assertThat(loadBalanceRetryHandler1WithRetryDisabled.isRetriableException(new SocketException(), true),
        is(false));

    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled.isRetriableException(new SocketException(), false),
        is(true));
    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled.isRetriableException(new SocketException(), true),
        is(false));
    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled.isRetriableException(new SocketTimeoutException(), true),
        is(true));
    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled.isRetriableException(new ConnectException(), true),
        is(true));

    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled
        .isRetriableException(new DummyException("connect failed", new ConnectException()), true), is(true));
    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled
        .isRetriableException(new DummyException("socket timeout", new SocketTimeoutException()), true), is(true));
    Assert.assertThat(loadBalanceRetryHandlerWithRetryEnabled
        .isRetriableException(new DummyException("illegal access", new IllegalAccessException()), true), is(false));
  }

  @Test
  public void testIsCircuitTrippingException() {
    LoadBalanceRetryHandler loadBalanceRetryHandler = new LoadBalanceRetryHandler(
        1, 2, true);

    Assert.assertThat(loadBalanceRetryHandler.isCircuitTrippingException(new SocketException()), is(true));
    Assert.assertThat(loadBalanceRetryHandler.isCircuitTrippingException(new SocketTimeoutException()), is(true));
    Assert.assertThat(loadBalanceRetryHandler.isCircuitTrippingException(new IllegalAccessException()), is(false));

    Assert.assertThat(loadBalanceRetryHandler
            .isCircuitTrippingException(new DummyException("socket exception", new SocketException())), is(true));
    Assert.assertThat(loadBalanceRetryHandler
            .isCircuitTrippingException(new DummyException("socket timeout", new SocketTimeoutException())), is(true));
    Assert.assertThat(
        loadBalanceRetryHandler
            .isCircuitTrippingException(new DummyException("illegal access", new IllegalAccessException())), is(false));
  }

}