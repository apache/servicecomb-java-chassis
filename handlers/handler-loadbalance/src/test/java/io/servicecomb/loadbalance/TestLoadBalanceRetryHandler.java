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

    LoadBalanceRetryHandler.getInstance().setRetryRuntimeParams(1, 2, true);
    Assert.assertThat(LoadBalanceRetryHandler.getInstance().getMaxRetriesOnSameServer(), is(1));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance().getMaxRetriesOnNextServer(), is(2));

    LoadBalanceRetryHandler.getInstance().addRetryOnSameExceptions(appendExceptions);
    List<Class<? extends Throwable>> retryOnSameExceptions = LoadBalanceRetryHandler.getInstance()
        .getRetryOnSameExceptions();
    Assert.assertThat(retryOnSameExceptions, hasItem(InvocationException.class));
    Assert.assertThat(retryOnSameExceptions, hasItem(IllegalAccessException.class));

    Assert.assertThat(retryOnSameExceptions.size(), is(4));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance().removeRetryOnSameException(InvocationException.class),
        is(true));
    Assert
        .assertThat(LoadBalanceRetryHandler.getInstance().removeRetryOnSameException(SocketException.class), is(false));
    Assert.assertThat(retryOnSameExceptions.size(), is(3));
    Assert.assertThat(retryOnSameExceptions,
        contains(SocketTimeoutException.class, ConnectException.class, IllegalAccessException.class));
  }

  @Test
  public void testIsRetriableException() {
    LoadBalanceRetryHandler.getInstance().setRetryRuntimeParams(1, 2, true);
    Assert
        .assertThat(LoadBalanceRetryHandler.getInstance().isRetriableException(new SocketException(), false), is(true));
    Assert
        .assertThat(LoadBalanceRetryHandler.getInstance().isRetriableException(new SocketException(), true), is(false));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance().isRetriableException(new SocketTimeoutException(), true),
        is(true));
    Assert
        .assertThat(LoadBalanceRetryHandler.getInstance().isRetriableException(new ConnectException(), true), is(true));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance()
        .isRetriableException(new DummyException("connect failed", new ConnectException()), true), is(true));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance()
        .isRetriableException(new DummyException("socket timeout", new SocketTimeoutException()), true), is(true));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance()
        .isRetriableException(new DummyException("illegal access", new IllegalAccessException()), true), is(false));

    LoadBalanceRetryHandler.getInstance().setRetryRuntimeParams(1, 2, false);
    Assert
        .assertThat(LoadBalanceRetryHandler.getInstance().isRetriableException(new SocketException(), true), is(false));

  }

  @Test
  public void testIsCircuitTrippingException() {
    LoadBalanceRetryHandler.getInstance().setRetryRuntimeParams(1, 2, true);

    Assert
        .assertThat(LoadBalanceRetryHandler.getInstance().isCircuitTrippingException(new SocketException()), is(true));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance().isCircuitTrippingException(new SocketTimeoutException()),
        is(true));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance().isCircuitTrippingException(new IllegalAccessException()),
        is(false));

    Assert.assertThat(LoadBalanceRetryHandler.getInstance()
        .isCircuitTrippingException(new DummyException("socket exception", new SocketException())), is(true));
    Assert.assertThat(LoadBalanceRetryHandler.getInstance()
        .isCircuitTrippingException(new DummyException("socket timeout", new SocketTimeoutException())), is(true));
    Assert.assertThat(
        LoadBalanceRetryHandler.getInstance()
            .isCircuitTrippingException(new DummyException("illegal access", new IllegalAccessException())), is(false));
  }

}