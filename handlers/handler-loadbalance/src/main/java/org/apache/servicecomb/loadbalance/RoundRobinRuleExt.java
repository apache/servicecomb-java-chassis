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

package org.apache.servicecomb.loadbalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Invocation;

/**
 * A round robin rule
 */
public class RoundRobinRuleExt implements RuleExt {
  private AtomicInteger counter = new AtomicInteger(0);

  @Override
  public ServiceCombServer choose(List<ServiceCombServer> servers, Invocation invocation) {
    if (servers.size() == 0) {
      return null;
    }
    int index = Math.abs(counter.getAndIncrement()) % servers.size();
    return servers.get(index);
  }
}
