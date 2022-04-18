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
package org.apache.servicecomb.provider.springmvc.reference;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.servicecomb.foundation.common.utils.GenericsUtils;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;

public class CseRequestCallback implements RequestCallback {
  private final Object requestBody;

  private final RequestCallback orgCallback;

  private final Type responseType;

  public CseRequestCallback(Object requestBody, RequestCallback orgCallback, Type responseType) {
    this.requestBody = requestBody;
    this.orgCallback = orgCallback;
    this.responseType = responseType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doWithRequest(ClientHttpRequest request) throws IOException {
    orgCallback.doWithRequest(request);
    CseClientHttpRequest req = (CseClientHttpRequest) request;
    req.setResponseType(overrideResponseType());

    if (!CseHttpEntity.class.isInstance(requestBody)) {
      return;
    }

    CseHttpEntity<?> entity = (CseHttpEntity<?>) requestBody;
    req.setContext(entity.getContext());
  }

  private Type overrideResponseType() {
    if (GenericsUtils.isGenericResponseType(responseType)) {
      // code: List<GenericObjectParam> response = restTemplate
      //    .postForObject("/testListObjectParam", request, List.class);
      // will using server schema type to deserialize
      return null;
    }
    // code: MyObject response = .postForObject("/testListObjectParam", request, MyObject.class);
    return responseType;
  }
}
