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

package org.apache.servicecomb.localservice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In Java Chassis 3.x, {@link org.apache.servicecomb.core.BootListener} instances are
 * collected from the Spring context instead of Java SPI, so the local service boot
 * listener must be registered as a bean.
 */
@Configuration
@ConditionalOnProperty(value = LocalServiceConfig.KEY_ENABLED, havingValue = "true")
public class LocalServiceConfiguration {
  @Bean
  public LocalServiceBootListener localServiceBootListener() {
    return new LocalServiceBootListener();
  }
}
