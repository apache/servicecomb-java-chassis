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
import static org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.servicecomb.tests.EmbeddedAppender;
import org.apache.servicecomb.tests.Log4jConfig;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import zipkin.junit.ZipkinRule;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

public class TracingTestBase {

  @ClassRule
  public static final ZipkinRule zipkin = new ZipkinRule();

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final EmbeddedAppender appender = new EmbeddedAppender();

  final RestTemplate restTemplate = new RestTemplate();

  @BeforeClass
  public static void setUpClass() throws Exception {
    System.setProperty(CONFIG_TRACING_COLLECTOR_ADDRESS, zipkin.httpUrl());
    setUpLocalRegistry();

    Log4jConfig.addAppender(appender);
  }

  private static void setUpLocalRegistry() {
    System.setProperty(LOCAL_REGISTRY_FILE_KEY, "notExistJustForceLocal");
  }

  protected void assertThatSpansReceivedByZipkin(Collection<String> logs, String... values) {
    logs.forEach(message -> log.info("Received log: " + message));
    List<String> loggedIds = logs.stream()
        .map(this::extractIds)
        .collect(Collectors.toList());

    // Sleep for 5 seconds to wait the reporter finish posting to zipkin server.
    // See SCB-293
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      log.error("Thread interrupted, ", e.getMessage());
      Thread.currentThread().interrupt();
    }

    String url = zipkin.httpUrl() + "/api/v2/trace/{traceId}";
    log.info("rest url:" + url);
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, traceId(loggedIds));

    assertThat(responseEntity.getStatusCode(), is(OK));
    String body = responseEntity.getBody();
    log.info("Received trace json: {}", body);
    List<Span> spans = new ArrayList<>();
    SpanBytesDecoder.JSON_V2.decodeList(body.getBytes(), spans);

    List<String> tracedValues = tracedValues(spans);
    tracedValues.forEach(value -> log.info("Received value {}", value));
    log.info("values: " + String.join(",", values));
    assertThat(tracedValues, containsInAnyOrder(values));
  }

  private List<String> tracedValues(List<Span> spans) {
    return spans.stream()
        .filter(span -> span.tags() != null)
        .map(span -> span.tags().entrySet())
        .flatMap(Collection::stream)
        .filter(span -> "call.path".equals(span.getKey()) || "http.path".equals(span.getKey())
            || "http.status_code".equals(span.getKey()))
        .filter(span -> span.getValue() != null)
        .map(annotation -> new String(annotation.getValue()))
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
