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

package org.apache.servicecomb.transport.rest.client;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestRestTransportClient {

  private RestTransportClient instance = null;

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);


  @Before
  public void setUp() throws Exception {
    instance = new RestTransportClient();
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testGetInstance() {
    Assert.assertNotNull(instance);
  }


  @Test
  public void testRestTransportClientException() {
    boolean status = true;
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(operationMeta);
    try {
      instance.send(invocation, asyncResp);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertFalse(status);
  }
}
