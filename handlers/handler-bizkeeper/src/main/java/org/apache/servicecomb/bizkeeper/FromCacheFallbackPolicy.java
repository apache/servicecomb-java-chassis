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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.stereotype.Component;

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
