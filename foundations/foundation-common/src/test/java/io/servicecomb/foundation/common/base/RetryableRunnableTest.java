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

package io.servicecomb.foundation.common.base;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class RetryableRunnableTest {

  private final Runnable blockedRunnable = mock(Runnable.class);
  private final DescriptiveRunnable runnable = mock(DescriptiveRunnable.class);
  private final RetryableRunnable retryableRunnable = new RetryableRunnable(runnable, 50);
  private final RuntimeException exception = new RuntimeException("oops");

  @Before
  public void setUp() throws Exception {
    when(runnable.description()).thenReturn("some descriptive runnable");
  }

  @Test
  public void runOnceWhenNoException() {
    retryableRunnable.run();

    verify(runnable).run();
  }

  @Test
  public void retriesUnderlyingRunnableUntilSuccess() {
    doThrow(exception).doThrow(exception).doNothing().when(runnable).run();

    retryableRunnable.run();

    verify(runnable, times(3)).run();
  }

  @Test
  public void exitsWhenInterrupted() throws InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    doThrow(exception).when(runnable).run();

    Future<?> retryable = executorService.submit(retryableRunnable);
    executorService.submit(blockedRunnable);

    TimeUnit.MILLISECONDS.sleep(100);

    retryable.cancel(true);

    TimeUnit.MILLISECONDS.sleep(100);

    assertThat(retryable.isCancelled(), is(true));

    verify(blockedRunnable).run();
    executorService.shutdown();
  }
}