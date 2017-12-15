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

package io.servicecomb.tests.tracing;

import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_ADDRESS;
import static io.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.tests.EmbeddedAppender;
import io.servicecomb.tests.Log4jConfig;
import zipkin.Codec;
import zipkin.Span;
import zipkin.junit.ZipkinRule;

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
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL resource = loader.getResource("registry.yaml");
    System.setProperty(LOCAL_REGISTRY_FILE_KEY, resource.getPath());
  }

  protected void assertThatSpansReceivedByZipkin(Collection<String> logs, String... values) {
    logs.forEach(message -> log.info("Received log: " + message));
    List<String> loggedIds = logs.stream()
        .map(this::extractIds)
        .collect(Collectors.toList());

    String url = zipkin.httpUrl() + "/api/v1/trace/{traceId}";
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, traceId(loggedIds));

    assertThat(responseEntity.getStatusCode(), is(OK));
    String body = responseEntity.getBody();
    log.info("Received trace json: {}", body);
    List<Span> spans = Codec.JSON.readSpans(body.getBytes());

    List<String> tracedValues = tracedValues(spans);
    tracedValues.forEach(value -> log.info("Received value {}", value));
    assertThat(tracedValues, contains(values));
  }

  private List<String> tracedValues(List<Span> spans) {
    return spans.stream()
        .filter(span -> span.binaryAnnotations != null)
        .map(span -> span.binaryAnnotations)
        .flatMap(Collection::stream)
        .filter(span -> "call.path".equals(span.key) || "http.path".equals(span.key) || "http.status_code".equals(span.key))
        .filter(span -> span.value != null)
        .map(annotation -> new String(annotation.value))
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
