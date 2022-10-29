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

package org.apache.servicecomb.provider.springmvc.reference.async;

import org.apache.servicecomb.provider.springmvc.reference.CseHttpEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;

public class CseAsyncRequestCallbackTest {
  @Test
  public void testNormal() {
    CseAsyncClientHttpRequest request = new CseAsyncClientHttpRequest();
    CseAsyncRequestCallback<HttpEntity<?>> cb = new CseAsyncRequestCallback<>(null);
    cb.doWithRequest(request);
    Assertions.assertNull(request.getContext());
  }

  @Test
  public void testHttpEntity() {
    HttpEntity<?> entity = Mockito.mock(HttpEntity.class);
    CseAsyncRequestCallback<?> cb = new CseAsyncRequestCallback<>(entity);
    CseAsyncClientHttpRequest request = new CseAsyncClientHttpRequest();
    cb.doWithRequest(request);
    Assertions.assertEquals(entity.getBody(), request.getBody());
  }

  @Test
  public void testCseEntity() {
    CseHttpEntity<?> entity = Mockito.mock(CseHttpEntity.class);
    CseAsyncClientHttpRequest request = new CseAsyncClientHttpRequest();
    entity.addContext("c1", "c2");
    CseAsyncRequestCallback<?> cb = new CseAsyncRequestCallback<>(entity);
    cb.doWithRequest(request);
    Assertions.assertEquals(entity.getContext(), request.getContext());
  }
}
