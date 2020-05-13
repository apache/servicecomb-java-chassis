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

package org.apache.servicecomb.serviceregistry.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DiscoveryContext {
  private Object inputParameters;

  private Map<String, Object> contextParameters = new HashMap<>();

  // some filter support rerun logic, eg:ZoneAware
  // instances grouping to self zone, other zone, and so on
  // first try self zone, after other filter(Isolation Filter), no instances are available
  // then try other zone
  private Stack<DiscoveryTreeNode> rerunStack = new Stack<>();

  private DiscoveryTreeNode currentNode;

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

  public void setCurrentNode(DiscoveryTreeNode node) {
    this.currentNode = node;
  }

  public void pushRerunFilter() {
    rerunStack.push(currentNode);
  }

  public DiscoveryTreeNode popRerunFilter() {
    if (rerunStack.isEmpty()) {
      return null;
    }

    return rerunStack.pop();
  }
}
