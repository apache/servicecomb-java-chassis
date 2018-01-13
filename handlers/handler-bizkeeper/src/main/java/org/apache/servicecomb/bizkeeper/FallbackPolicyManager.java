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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;

public class FallbackPolicyManager {
  private static final Map<String, FallbackPolicy> POLICIES = new HashMap<>();

  public static void addPolicy(FallbackPolicy policy) {
    POLICIES.put(policy.name().toLowerCase(), policy);
  }

  public static void record(String type, Invocation invocation, Response response, boolean isSuccess) {
    FallbackPolicy policy = getPolicy(type, invocation);
    if (policy != null) {
      policy.record(invocation, response, isSuccess);
    }
  }

  public static Response getFallbackResponse(String type, Invocation invocation) {
    FallbackPolicy policy = getPolicy(type, invocation);
    if (policy != null) {
      return policy.getFallbackResponse(invocation);
    } else {
      return Response.failResp(invocation.getInvocationType(),
          BizkeeperExceptionUtils
              .createBizkeeperException(BizkeeperExceptionUtils.CSE_HANDLER_BK_FALLBACK,
                  null,
                  invocation.getOperationMeta().getMicroserviceQualifiedName()));
    }
  }

  private static FallbackPolicy getPolicy(String type, Invocation invocation) {
    String policyKey = Configuration.INSTANCE.getFallbackPolicyPolicy(type,
        invocation.getMicroserviceName(),
        invocation.getOperationMeta().getMicroserviceQualifiedName());
    FallbackPolicy policy = null;
    if (policyKey != null) {
      policy = POLICIES.get(policyKey.toLowerCase());
    }
    return policy;
  }
}
