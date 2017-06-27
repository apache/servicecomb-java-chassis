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

package io.servicecomb.tracing.rest.springmvc;

import brave.Tracing;
import brave.context.log4j2.ThreadContextCurrentTraceContext;
import brave.http.HttpTracing;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import brave.spring.webmvc.TracingHandlerInterceptor;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

/**
 * This adds tracing configuration to any web mvc controllers or rest template clients. This should
 * be configured last.
 */
@Configuration
@Import({TracingClientHttpRequestInterceptor.class, TracingHandlerInterceptor.class})
public class  TracingConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private TracingHandlerInterceptor tracingInterceptor;

  @Autowired
  private TracingClientHttpRequestInterceptor clientInterceptor;

  @Autowired
  private RestTemplate restTemplate;

  /**
   * Configuration for how to send spans to Zipkin
   */
  @Bean
  Sender sender() {
    return OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
  }

  /**
   * Configuration for how to buffer spans into messages for Zipkin
   */
  @Bean
  Reporter<Span> reporter() {
    return AsyncReporter.builder(sender()).build();
  }

  /**
   * Controls aspects of tracing such as the name that shows up in the UI
   */
  @Bean
  Tracing tracing() {
    return Tracing.newBuilder()
        .localServiceName("brave-webmvc-example")
        .currentTraceContext(ThreadContextCurrentTraceContext.create()) // puts trace IDs into logs
        .reporter(reporter()).build();
  }

  // decides how to name and tag spans. By default they are named the same as the http method.
  @Bean
  HttpTracing httpTracing() {
    return HttpTracing.create(tracing());
  }

  @PostConstruct
  void init() {
    List<ClientHttpRequestInterceptor> interceptors =
        new ArrayList<>(restTemplate.getInterceptors());
    interceptors.add(clientInterceptor);
    restTemplate.setInterceptors(interceptors);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(tracingInterceptor);
  }
}