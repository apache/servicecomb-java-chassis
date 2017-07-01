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

import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICE_NAME;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_ADDRESS;

import brave.Tracing;
import brave.context.log4j12.MDCCurrentTraceContext;
import brave.http.HttpTracing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

@Configuration
class TracingConfiguration {

  @Bean
  Sender sender(@Value("${" + CONFIG_TRACING_COLLECTOR_ADDRESS + "}") String tracingCollectorAddress) {
    return OkHttpSender.create(tracingCollectorAddress);
  }

  @Bean
  Reporter<Span> reporter(Sender sender) {
    return AsyncReporter.builder(sender).build();
  }

  @Bean
  Tracing tracing(Reporter<Span> reporter, @Value("${" + CONFIG_SERVICE_NAME + "}") String serviceName) {
    return Tracing.newBuilder()
        .localServiceName(serviceName)
        .currentTraceContext(MDCCurrentTraceContext.create()) // puts trace IDs into logs
        .reporter(reporter).build();
  }

  @Bean
  HttpTracing httpTracing(Tracing tracing) {
    return HttpTracing.create(tracing);
  }

}