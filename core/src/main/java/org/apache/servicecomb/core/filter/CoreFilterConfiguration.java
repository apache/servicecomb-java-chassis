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
package org.apache.servicecomb.core.filter;

import org.apache.servicecomb.core.filter.impl.ContextMapperFilter;
import org.apache.servicecomb.core.filter.impl.ParameterValidatorFilter;
import org.apache.servicecomb.core.filter.impl.ProviderOperationFilter;
import org.apache.servicecomb.core.filter.impl.RetryFilter;
import org.apache.servicecomb.core.filter.impl.ScheduleFilter;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreFilterConfiguration {
  @Bean
  public ProviderOperationFilter scbProducerOperationFilter() {
    return new ProviderOperationFilter();
  }

  @Bean
  public ScheduleFilter scbScheduleFilter() {
    return new ScheduleFilter();
  }

  @Bean
  public RetryFilter scbRetryFilter(RetryHandler retryHandler) {
    return new RetryFilter(retryHandler);
  }

  @Bean
  public ContextMapperFilter scbContextMapperFilter(MapperHandler mapperHandler) {
    return new ContextMapperFilter(mapperHandler);
  }

  @Bean
  public FilterChainsManager scbFilterChainsManager() {
    return new FilterChainsManager();
  }

  @Bean
  public ParameterValidatorFilter scbParameterValidatorFilter() {
    return new ParameterValidatorFilter();
  }
}
