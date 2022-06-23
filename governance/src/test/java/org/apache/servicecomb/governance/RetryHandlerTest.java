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
package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.RetryExtension;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.resilience4j.retry.MaxRetriesExceededException;
import io.github.resilience4j.retry.Retry;

public class RetryHandlerTest {
  @Test
  public void testNotFailAfterMaxAttemptsWhenThrow() {
    RetryExtension retryExtension = Mockito.mock(RetryExtension.class);
    RetryProperties retryProperties = Mockito.mock(RetryProperties.class);
    GovernanceRequest governanceRequest = Mockito.mock(GovernanceRequest.class);
    Mockito.when(retryExtension.isRetry(Mockito.any())).thenReturn(true);

    RetryPolicy retryPolicy = new RetryPolicy();
    retryPolicy.setName("test");
    retryPolicy.setFailAfterMaxAttempts(false);
    RetryHandler retryHandler = new RetryHandler(retryProperties, retryExtension);

    Retry retry = retryHandler.createProcessor(governanceRequest, retryPolicy);
    Assertions.assertThrows(IllegalStateException.class, () -> retry.<Integer>executeCheckedSupplier(() -> {
      throw new IllegalStateException();
    }));
  }

  @Test
  public void testFailAfterMaxAttemptsWhenThrow() {
    RetryExtension retryExtension = Mockito.mock(RetryExtension.class);
    RetryProperties retryProperties = Mockito.mock(RetryProperties.class);
    GovernanceRequest governanceRequest = Mockito.mock(GovernanceRequest.class);
    Mockito.when(retryExtension.isRetry(Mockito.any())).thenReturn(true);

    RetryPolicy retryPolicy = new RetryPolicy();
    retryPolicy.setName("test");
    retryPolicy.setFailAfterMaxAttempts(true);
    RetryHandler retryHandler = new RetryHandler(retryProperties, retryExtension);

    Retry retry = retryHandler.createProcessor(governanceRequest, retryPolicy);
    Assertions.assertThrows(IllegalStateException.class, () -> retry.<Integer>executeCheckedSupplier(() -> {
      throw new IllegalStateException();
    }));
  }

  @Test
  public void testFailAfterMaxAttemptsOnResult() {
    RetryExtension retryExtension = Mockito.mock(RetryExtension.class);
    RetryProperties retryProperties = Mockito.mock(RetryProperties.class);
    GovernanceRequest governanceRequest = Mockito.mock(GovernanceRequest.class);
    Mockito.when(retryExtension.isRetry(Mockito.any(), Mockito.any())).thenReturn(true);

    RetryPolicy retryPolicy = new RetryPolicy();
    retryPolicy.setName("test");
    retryPolicy.setFailAfterMaxAttempts(true);
    RetryHandler retryHandler = new RetryHandler(retryProperties, retryExtension);

    Retry retry = retryHandler.createProcessor(governanceRequest, retryPolicy);
    Assertions.assertThrows(MaxRetriesExceededException.class, () -> retry.<Integer>executeCheckedSupplier(() -> -1));
  }
}
