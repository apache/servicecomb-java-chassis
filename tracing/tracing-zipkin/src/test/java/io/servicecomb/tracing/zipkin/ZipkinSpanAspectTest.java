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

import static io.servicecomb.tracing.zipkin.ZipkinTracingAdviser.CALL_PATH;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import brave.Tracing;
import brave.internal.StrictCurrentTraceContext;
import io.servicecomb.tracing.zipkin.ZipkinSpanAspectTest.TracingConfig;
import io.servicecomb.tracing.zipkin.app.ZipkinSpanTestApplication;
import io.servicecomb.tracing.zipkin.app.ZipkinSpanTestApplication.CustomSpanTask;
import io.servicecomb.tracing.zipkin.app.ZipkinSpanTestApplication.SomeSlowTask;
import zipkin.Span;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ZipkinSpanTestApplication.class, TracingConfig.class})
public class ZipkinSpanAspectTest {

  @Autowired
  private Queue<Span> spans;

  @Autowired
  private SomeSlowTask someSlowTask;
  
  @Autowired
  private CustomSpanTask customSpanTask;

  @Autowired
  private Tracing tracing;

  @After
  public void tearDown() throws Exception {
    tracing.close();
  }

  @Test
  public void reportedSpanContainsAnnotatedMethodInfo() throws Exception {
    someSlowTask.crawl();

    await().atMost(2, SECONDS).until(() -> !spans.isEmpty());

    zipkin.Span span = spans.poll();
    assertThat(span.name, is("crawl"));
    assertThat(tracedValues(span), contains(SomeSlowTask.class.getMethod("crawl").toString()));
  }
  
  @Test
  public void reportCustomSpanInfomation() throws Exception {
    customSpanTask.invoke();
    await().atMost(2, SECONDS).until(() -> !spans.isEmpty());
  
    zipkin.Span span = spans.poll();
    assertThat(span.name, is("transaction1"));
    assertThat(tracedValues(span), contains("startA"));
    
  }

  private List<String> tracedValues(zipkin.Span spans) {
    return spans.binaryAnnotations.stream()
        .filter(span -> CALL_PATH.equals(span.key) || "error".equals(span.key))
        .filter(span -> span.value != null)
        .map(annotation -> new String(annotation.value))
        .distinct()
        .collect(Collectors.toList());
  }

  @Configuration
  static class TracingConfig {
    @Bean
    Queue<Span> spans() {
      return new ConcurrentLinkedDeque<>();
    }

    @Bean
    Tracing tracing(Queue<Span> spans) {
      return Tracing.newBuilder()
          .currentTraceContext(new StrictCurrentTraceContext())
          .reporter(spans::add)
          .build();
    }
  }
}
