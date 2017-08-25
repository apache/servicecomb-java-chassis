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

package io.servicecomb.foundation.vertx.client.tcp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.NetThreadData;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;

public class TestClientPoolManager {
  private ClientPoolManager<HttpClientWithContext> instance;

  @Before
  public void setUp() throws Exception {
    instance = new ClientPoolManager<>();
  }

  @Test
  public void testAddNetThread() {
    @SuppressWarnings("unchecked")
    NetThreadData<HttpClientWithContext> netThread = Mockito.mock(NetThreadData.class);
    instance.addNetThread(netThread);
    HttpClientWithContext context = Mockito.mock(HttpClientWithContext.class);
    Mockito.when(netThread.selectClientPool()).thenReturn(context);
    HttpClientWithContext netThreadValue = instance.findThreadBindClientPool();
    Assert.assertNotNull(netThreadValue);
  }
}
