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

package org.apache.servicecomb.core.governance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.governance.InvocationContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceCombInvocationContext implements InvocationContext {
  private static final String CONTEXT_KEY = "x-servicecomb-governance-match";

  private static final ThreadLocal<org.apache.servicecomb.swagger.invocation.context.InvocationContext> contextMgr = new ThreadLocal<>();

  public static void setInvocationContext(
      org.apache.servicecomb.swagger.invocation.context.InvocationContext invocationContext) {
    contextMgr.set(invocationContext);
  }

  public static void removeInvocationContext() {
    contextMgr.remove();
  }

  @Override
  public Map<String, Boolean> getCalculatedMatches() {
    Map<String, Boolean> result = contextMgr.get().getLocalContext(CONTEXT_KEY);
    if (result == null) {
      return Collections.emptyMap();
    }
    return result;
  }

  @Override
  public void addMatch(String key, Boolean value) {
    Map<String, Boolean> result = contextMgr.get().getLocalContext(CONTEXT_KEY);
    if (result == null) {
      result = new HashMap<>();
      contextMgr.get().addLocalContext(CONTEXT_KEY, result);
    }
    result.put(key, value);
  }
}
