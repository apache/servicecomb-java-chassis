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

import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.loadbalance.filter.InstancePropertyDiscoveryFilter;
import org.apache.servicecomb.loadbalance.filter.PriorityInstancePropertyDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = LoadBalanceConfiguration.LOAD_BALANCE_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class LoadBalanceConfiguration {
  public static final String LOAD_BALANCE_PREFIX = "servicecomb.loadbalance";

  public static final String LOAD_BALANCE_ENABLED = LOAD_BALANCE_PREFIX + ".enabled";

  @Bean
  public ConsumerFilter loadBalanceFilter(ExtensionsManager extensionsManager, DiscoveryTree discoveryTree) {
    return new LoadBalanceFilter(extensionsManager, discoveryTree);
  }

  @Bean
  public RuleNameExtentionsFactory ruleNameExtentionsFactory() {
    return new RuleNameExtentionsFactory();
  }

  @Bean
  public ExtensionsManager extensionsManager(List<ExtensionsFactory> extensionsFactories) {
    return new ExtensionsManager(extensionsFactories);
  }

  @Bean
  public PriorityInstancePropertyDiscoveryFilter priorityInstancePropertyDiscoveryFilter() {
    return new PriorityInstancePropertyDiscoveryFilter();
  }

  @Bean
  public InstancePropertyDiscoveryFilter instancePropertyDiscoveryFilter() {
    return new InstancePropertyDiscoveryFilter();
  }
}
