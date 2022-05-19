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

package org.apache.servicecomb.bizkeeper;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import rx.Observable;

public class TestBizkeeperCommand {

  @Test
  public void testGetCacheKeyProvider() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ProviderBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    String str = bizkeeperCommand.getCacheKey();
    Assertions.assertNull(str);

    Response resp = Mockito.mock(Response.class);
    Mockito.when(resp.isFailed()).thenReturn(false);
    Assertions.assertFalse(bizkeeperCommand.isFailedResponse(resp));
    Mockito.when(resp.isFailed()).thenReturn(true);
    InvocationException excp = Mockito.mock(InvocationException.class);
    Mockito.when(resp.getResult()).thenReturn(excp);
    Mockito.when(excp.getStatusCode()).thenReturn(400);
    Assertions.assertFalse(bizkeeperCommand.isFailedResponse(resp));
    Mockito.when(resp.getResult()).thenReturn(excp);
    Mockito.when(excp.getStatusCode()).thenReturn(590);
    Assertions.assertTrue(bizkeeperCommand.isFailedResponse(resp));
  }

  @Test
  public void testResumeWithFallbackProvider() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ProviderBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    Observable<Response> observe = bizkeeperCommand.resumeWithFallback();
    Assertions.assertNotNull(observe);
  }

  @Test
  public void testConstructProvider() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ProviderBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    Observable<Response> response = bizkeeperCommand.construct();
    Assertions.assertNotNull(response);
  }

  @Test
  public void testGetCacheKeyWithContextInitializedProvider() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ProviderBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    HystrixRequestContext.initializeContext();
    String cacheKey = bizkeeperCommand.getCacheKey();
    Assertions.assertNotNull(cacheKey);
  }

  @Test
  public void testGetCacheKeyConsumer() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ConsumerBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    String str = bizkeeperCommand.getCacheKey();
    Assertions.assertNull(str);

    Response resp = Mockito.mock(Response.class);
    Mockito.when(resp.isFailed()).thenReturn(false);
    Assertions.assertFalse(bizkeeperCommand.isFailedResponse(resp));
    Mockito.when(resp.isFailed()).thenReturn(true);
    InvocationException excp = Mockito.mock(InvocationException.class);
    Mockito.when(resp.getResult()).thenReturn(excp);
    Mockito.when(excp.getStatusCode()).thenReturn(400);
    Assertions.assertFalse(bizkeeperCommand.isFailedResponse(resp));
    Mockito.when(resp.getResult()).thenReturn(excp);
    Mockito.when(excp.getStatusCode()).thenReturn(490);
    Assertions.assertTrue(bizkeeperCommand.isFailedResponse(resp));
  }

  @Test
  public void testResumeWithFallbackConsumer() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ConsumerBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    Observable<Response> observe = bizkeeperCommand.resumeWithFallback();
    Assertions.assertNotNull(observe);
  }

  @Test
  public void testConstructConsumer() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ConsumerBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    Observable<Response> response = bizkeeperCommand.construct();
    Assertions.assertNotNull(response);
  }

  @Test
  public void testGetCacheKeyWithContextInitializedConsumer() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ConsumerBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));

    HystrixRequestContext.initializeContext();
    String cacheKey = bizkeeperCommand.getCacheKey();
    Assertions.assertNotNull(cacheKey);
  }
}
