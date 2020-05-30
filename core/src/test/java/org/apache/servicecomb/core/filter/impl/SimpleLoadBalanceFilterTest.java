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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

public class SimpleLoadBalanceFilterTest {
  SimpleLoadBalanceFilter filter = new SimpleLoadBalanceFilter();

  @Injectable
  Invocation invocation;

  @Injectable
  Endpoint endpoint;

  @Mocked
  DiscoveryContext discoveryContext;

  @Injectable
  FilterNode nextNode;

  @Test
  public void should_invoke_next_directly_when_invocation_already_has_endpoint()
      throws ExecutionException, InterruptedException {
    Response response = Response.ok("ok");
    new Expectations() {
      {
        invocation.getEndpoint();
        result = endpoint;

        nextNode.onFilter(invocation);
        result = CompletableFuture.completedFuture(response);
      }
    };

    Response result = filter.onFilter(invocation, nextNode).get();

    assertThat(result).isSameAs(response);
    new Verifications() {
      {
        discoveryContext.setInputParameters(invocation);
        times = 0;
      }
    };
  }
}