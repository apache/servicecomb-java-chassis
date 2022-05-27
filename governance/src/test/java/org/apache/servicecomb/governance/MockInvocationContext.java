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

package org.apache.servicecomb.governance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MockInvocationContext implements InvocationContext {
  private final ThreadLocal<Map<String, Boolean>> context = new ThreadLocal<>();

  @Override
  public Map<String, Boolean> getCalculatedMatches() {
    Map<String, Boolean> result = context.get();
    if (result == null) {
      return Collections.emptyMap();
    }
    return result;
  }

  @Override
  public void addMatch(String key, Boolean value) {
    Map<String, Boolean> result = context.get();
    if (result == null) {
      result = new HashMap<>();
      context.set(result);
    }
    result.put(key, value);
  }
}
