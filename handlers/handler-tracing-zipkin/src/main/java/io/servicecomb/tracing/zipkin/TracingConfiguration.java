/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.tracing.zipkin;

import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_ADDRESS;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_PATH;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_MICROSERVICE_NAME;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_TRACING_COLLECTOR_ADDRESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Tracing;
import brave.context.log4j12.MDCCurrentTraceContext;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import io.servicecomb.config.DynamicProperties;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

@Configuration
class TracingConfiguration {

  @Bean
  Sender sender(DynamicProperties dynamicProperties) {
    return OkHttpSender.create(
        dynamicProperties.getStringProperty(
            CONFIG_TRACING_COLLECTOR_ADDRESS,
            DEFAULT_TRACING_COLLECTOR_ADDRESS)
            .trim()
            .replaceAll("/+$", "")
            .concat(CONFIG_TRACING_COLLECTOR_PATH));
  }

  @Bean
  Reporter<Span> zipkinReporter(Sender sender) {
    return AsyncReporter.builder(sender).build();
  }

  @Bean
  Tracing tracing(Reporter<Span> reporter, DynamicProperties dynamicProperties,
      CurrentTraceContext currentTraceContext) {
    return Tracing.newBuilder()
        .localServiceName(dynamicProperties.getStringProperty(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY,
            DEFAULT_MICROSERVICE_NAME))
        .currentTraceContext(currentTraceContext) // puts trace IDs into logs
        .reporter(reporter)
        .build();
  }

  @Bean
  CurrentTraceContext currentTraceContext() {
    return MDCCurrentTraceContext.create();
  }

  @Bean
  HttpTracing httpTracing(Tracing tracing) {
    return HttpTracing.create(tracing);
  }
}
