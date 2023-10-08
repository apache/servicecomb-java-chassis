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
package org.apache.servicecomb.qps;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(value = FlowControlQpsConfiguration.FLOW_CONTROL_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class FlowControlQpsConfiguration {
  public static final String FLOW_CONTROL_PREFIX = "servicecomb.flowcontrol";

  public static final String FLOW_CONTROL_ENABLED = FLOW_CONTROL_PREFIX + ".enabled";

  @Bean
  public ProviderFlowControlFilter providerFlowControlFilter(Environment environment) {
    return new ProviderFlowControlFilter(environment);
  }

  @Bean
  public ConsumerFlowControlFilter consumerFlowControlFilter(Environment environment) {
    return new ConsumerFlowControlFilter(environment);
  }
}
