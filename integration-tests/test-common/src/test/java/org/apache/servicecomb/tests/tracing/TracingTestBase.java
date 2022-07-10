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

package org.apache.servicecomb.tests.tracing;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_ADDRESS;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.servicecomb.tests.EmbeddedAppender;
import org.apache.servicecomb.tests.Log4jConfig;
import org.awaitility.Awaitility;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import zipkin2.Span;
import zipkin2.junit.ZipkinRule;

public class TracingTestBase {

  @ClassRule
  public static final ZipkinRule zipkin = new ZipkinRule();

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final EmbeddedAppender appender = new EmbeddedAppender();

  final RestTemplate restTemplate = new RestTemplate();

  @BeforeAll
  public static void setUpClass() throws Exception {
    System.setProperty(CONFIG_TRACING_COLLECTOR_ADDRESS, zipkin.httpUrl());

    Log4jConfig.addAppender(appender);
  }

  protected void assertThatSpansReceivedByZipkin(Collection<String> logs, String... values) {
    logs.forEach(message -> log.info("Received log: " + message));
    List<String> loggedIds = logs.stream()
        .map(this::extractIds)
        .collect(Collectors.toList());

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> zipkin.getTrace(traceId(loggedIds)) != null);
    List<Span> spans = zipkin.getTrace(traceId(loggedIds));

    List<String> tracedValues = tracedValues(spans);
    int times = 100;
    while (tracedValues.size() < values.length && times > 0) {
      try {
        Thread.sleep(10);
        times--;
        spans = zipkin.getTrace(traceId(loggedIds));
        tracedValues = tracedValues(spans);
      } catch (InterruptedException e) {
        log.error("Thread interrupted, {}", e.getMessage());
        Thread.currentThread().interrupt();
      }
    }

    tracedValues.forEach(value -> log.info("Received value {}", value));
    log.info("values: " + String.join(",", values));
    MatcherAssert.assertThat(tracedValues, containsInAnyOrder(values));
  }

  private List<String> tracedValues(List<Span> spans) {
    return spans.stream()
        .filter(span -> span.tags() != null)
        .map(span -> span.tags().entrySet())
        .flatMap(Collection::stream)
        .filter(span -> "call.path".equals(span.getKey()) || "http.path".equals(span.getKey())
            || "http.status_code".equals(span.getKey()))
        .filter(span -> span.getValue() != null)
        .map(Map.Entry::getValue)
        .distinct()
        .collect(Collectors.toList());
  }

  private String extractIds(String message) {
    return message.replaceFirst(".*\\[(\\w+/\\w+/\\w*)\\].*", "$1").trim();
  }

  private String traceId(List<String> ids) {
    return ids.get(0).split("/")[0];
  }
}
