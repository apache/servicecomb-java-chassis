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
import org.springframework.http.HttpEntity;

@SuppressWarnings("deprecation")
// TODO : upgrade to spring 5 will having warning's , we'll fix it later
public class CseAsyncRequestCallback<T> implements org.springframework.web.client.AsyncRequestCallback {
  private final HttpEntity<T> requestBody;

  CseAsyncRequestCallback(HttpEntity<T> requestBody) {
    this.requestBody = requestBody;
  }

  @Override
  @SuppressWarnings("deprecation")
// TODO : upgrade to spring 5 will having warning's , we'll fix it later
  public void doWithRequest(org.springframework.http.client.AsyncClientHttpRequest request) {
    CseAsyncClientHttpRequest cseAsyncClientHttpRequest = (CseAsyncClientHttpRequest) request;
    if (requestBody != null) {
      cseAsyncClientHttpRequest.setRequestBody(requestBody.getBody());
      cseAsyncClientHttpRequest.setHttpHeaders(requestBody.getHeaders());
    }

    if (!CseHttpEntity.class.isInstance(requestBody)) {
      return;
    }

    CseAsyncClientHttpRequest req = (CseAsyncClientHttpRequest) request;
    CseHttpEntity<?> entity = (CseHttpEntity<?>) requestBody;
    req.setContext(entity.getContext());
  }
}
