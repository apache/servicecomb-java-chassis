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
package io.servicecomb.bizkeeper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.Response;

@Component
public class FromCacheFallbackPolicy implements FallbackPolicy {
  private static final String POLICY_NAME = "fromCache";

  private Map<String, Response> cachedResponse = new ConcurrentHashMap<>();

  @Override
  public String name() {
    return POLICY_NAME;
  }

  @Override
  public Response getFallbackResponse(Invocation invocation) {
    if (cachedResponse.get(invocation.getInvocationQualifiedName()) != null) {
      return cachedResponse.get(invocation.getInvocationQualifiedName());
    } else {
      return Response.succResp(null);
    }
  }

  @Override
  public void record(Invocation invocation, Response response, boolean isSuccess) {
    if (isSuccess) {
      cachedResponse.put(invocation.getInvocationQualifiedName(), response);
    }
  }

}
