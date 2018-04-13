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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;

import mockit.Injectable;

public class CseAsyncRequestCallbackTest {
  @Test
  public void testNormal() {
    CseAsyncClientHttpRequest request = new CseAsyncClientHttpRequest();
    CseAsyncRequestCallback<HttpEntity<?>> cb = new CseAsyncRequestCallback<>(null);
    cb.doWithRequest(request);
    Assert.assertEquals(null, request.getContext());
  }

  @Test
  public void testHttpEntity(@Injectable HttpEntity<String> entity) {
    CseAsyncRequestCallback<String> cb = new CseAsyncRequestCallback<>(entity);
    CseAsyncClientHttpRequest request = new CseAsyncClientHttpRequest();
    cb.doWithRequest(request);
    Assert.assertEquals(entity.getBody(), request.getBody());
  }

  @Test
  public void testCseEntity(@Injectable CseHttpEntity<String> entity) {
    CseAsyncClientHttpRequest request = new CseAsyncClientHttpRequest();
    entity.addContext("c1", "c2");
    CseAsyncRequestCallback<String> cb = new CseAsyncRequestCallback<>(entity);
    cb.doWithRequest(request);
    Assert.assertEquals(entity.getContext(), request.getContext());
  }
}