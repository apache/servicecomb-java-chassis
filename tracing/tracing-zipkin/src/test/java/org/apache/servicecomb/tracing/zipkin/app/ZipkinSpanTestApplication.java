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

package org.apache.servicecomb.tracing.zipkin.app;

import org.apache.servicecomb.tracing.Span;
import org.apache.servicecomb.tracing.zipkin.EnableZipkinTracing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZipkinTracing
public class ZipkinSpanTestApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZipkinSpanTestApplication.class);
  }

  @Bean
  SomeSlowTask someSlowTask() {
    return new SomeSlowTask();
  }
  
  @Bean
  CustomSpanTask customSpanTask() {
    return new CustomSpanTask();
  }

  public static class SomeSlowTask {
    @Span
    public String crawl() {
      return "crawling...";
    }
  }
  
  public static class CustomSpanTask {
    @Span(spanName = "transaction1", callPath = "startA")
    public String invoke() {
      return "invoke the method";
    }
  }
}
