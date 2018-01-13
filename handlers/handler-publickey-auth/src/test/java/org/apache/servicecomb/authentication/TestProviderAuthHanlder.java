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
package org.apache.servicecomb.authentication;

import org.apache.servicecomb.authentication.provider.ProviderAuthHanlder;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestProviderAuthHanlder {
  Invocation invocation = null;

  AsyncResponse asyncResp = null;

  @Before
  public void setUp() throws Exception {
    invocation = Mockito.mock(Invocation.class);
    asyncResp = Mockito.mock(AsyncResponse.class);
    Mockito.when(invocation.getContext(Const.AUTH_TOKEN)).thenReturn("testtoken");
  }

  @Test
  public void testHandle() throws Exception {
    ProviderAuthHanlder providerAuthHanlder = new ProviderAuthHanlder();
    providerAuthHanlder.handle(invocation, asyncResp);
    Assert.assertTrue(true);
  }
}
