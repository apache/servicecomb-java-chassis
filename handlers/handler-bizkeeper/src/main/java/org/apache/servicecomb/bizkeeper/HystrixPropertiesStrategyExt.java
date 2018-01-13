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

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.Setter;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;

/**
 * 定制HystrixPropertiesStrategy。 配置需要支持动态生效和调整， 默认的hystrix实现会根据CommonKey进行缓存。
 * CircuitBreake需要统计并记录请求数据，需要缓存并保证每个commandKey对应一个Breaker；单纯清理CommandKey无法动态生效。
 * 参考：HystrixCircuitBreaker.Factory.getInstance(commandKey, groupKey,
 * properties, metrics)
 *
 */
public final class HystrixPropertiesStrategyExt extends HystrixPropertiesStrategy {

  private static final HystrixPropertiesStrategyExt INSTANCE = new HystrixPropertiesStrategyExt();

  private final Map<String, HystrixCommandProperties> commandPropertiesMap = new ConcurrentHashMap<>();

  private HystrixPropertiesStrategyExt() {
  }

  public static HystrixPropertiesStrategyExt getInstance() {
    return INSTANCE;
  }

  @Override
  public HystrixCommandProperties getCommandProperties(HystrixCommandKey commandKey, Setter builder) {
    HystrixCommandProperties commandProperties = commandPropertiesMap.get(commandKey.name());
    if (commandProperties == null) {
      commandProperties = new HystrixCommandPropertiesExt(commandKey, builder);
      commandPropertiesMap.putIfAbsent(commandKey.name(), commandProperties);
    }
    return commandProperties;
  }
}
