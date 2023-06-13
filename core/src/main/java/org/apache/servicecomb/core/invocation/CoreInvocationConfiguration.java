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
package org.apache.servicecomb.core.invocation;

import java.util.List;

import org.apache.servicecomb.core.invocation.timeout.PassingTimeStrategy;
import org.apache.servicecomb.core.invocation.timeout.ProcessingTimeStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

@Configuration
public class CoreInvocationConfiguration {
  @Bean
  public InvocationTimeoutBootListener invocationTimeoutBootListener(EventBus eventBus,
      List<InvocationTimeoutStrategy> strategies,
      Environment environment) {
    return new InvocationTimeoutBootListener(eventBus, strategies, environment);
  }

  @Bean
  public PassingTimeStrategy passingTimeStrategy() {
    return new PassingTimeStrategy();
  }

  @Bean
  public ProcessingTimeStrategy processingTimeStrategy() {
    return new ProcessingTimeStrategy();
  }
}
