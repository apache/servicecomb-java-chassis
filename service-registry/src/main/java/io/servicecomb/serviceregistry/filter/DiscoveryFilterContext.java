/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DiscoveryFilterContext {
  private Object inputParameters;

  private Map<String, Object> contextParameters = new HashMap<>();

  // some filter support rerun logic, eg:ZoneAware
  // instances grouping to self zone, other zone, and so on
  // first try self zone, after other filter(Isolation Filter), no instances are available
  // then try other zone
  private Stack<Integer> rerunFilterStack = new Stack<>();

  private int currentFilter;

  @SuppressWarnings("unchecked")
  public <T> T getInputParameters() {
    return (T) inputParameters;
  }

  public void setInputParameters(Object inputParameters) {
    this.inputParameters = inputParameters;
  }

  @SuppressWarnings("unchecked")
  public <T> T getContextParameter(String name) {
    return (T) contextParameters.get(name);
  }

  public void putContextParameter(String name, Object value) {
    contextParameters.put(name, value);
  }

  public void setCurrentFilter(int currentFilter) {
    this.currentFilter = currentFilter;
  }

  public void pushRerunFilter() {
    rerunFilterStack.push(currentFilter);
  }

  public int popRerunFilter() {
    if (rerunFilterStack.isEmpty()) {
      return -1;
    }

    return rerunFilterStack.pop();
  }
}
