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

package io.servicecomb.spring.cloud.zuul.tracing;

import static io.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import io.servicecomb.tests.EmbeddedAppender;
import io.servicecomb.tests.Log4jConfig;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TracedZuulMain.class, webEnvironment = RANDOM_PORT)
public class SpringCloudZuulTracingTest {
  private static final EmbeddedAppender appender = new EmbeddedAppender();

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpLocalRegistry();

    Log4jConfig.addAppender(appender);
  }

  private static void setUpLocalRegistry() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL resource = loader.getResource("registry.yaml");
    System.setProperty(LOCAL_REGISTRY_FILE_KEY, resource.getPath());
  }

  @After
  public void tearDown() throws Exception {
    appender.clear();
  }

  @Test
  public void tracesCallsReceivedFromZuulToCalledService() throws InterruptedException {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity("/dummy/rest/blah", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("blah"));

    TimeUnit.MILLISECONDS.sleep(1000);

    Collection<String> tracingMessages = appender.pollLogs(".*\\[\\w+/\\w+/\\w*\\]\\s+INFO.*(logged tracing|/blah).*");
    assertThat(tracingMessages.size(), greaterThanOrEqualTo(2));

    Iterator<String> iterator = tracingMessages.iterator();
    // caller is the root of tracing tree and its traceId is the same as spanId
    String[] ids = tracingIds(iterator.next());
    String parentTraceId = ids[0];
    String parentSpanId = ids[1];

    assertThat(parentTraceId, is(parentSpanId));

    String message = iterator.next();
    // callee is called by caller and inherits caller's traceId but has its own spanId
    ids = tracingIds(message);
    String childTraceId = ids[0];
    String childSpanId = ids[1];
    String childParentId = ids[2];

    assertThat(childTraceId, is(parentTraceId));
    assertThat(childSpanId, is(not(parentTraceId)));
    assertThat(childParentId, is(parentSpanId));
  }

  @Test
  public void tracesFailedCallsReceivedByZuul() throws InterruptedException {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity("/dummy/rest/oops", String.class);

    assertThat(responseEntity.getStatusCode(), is(INTERNAL_SERVER_ERROR));

    TimeUnit.MILLISECONDS.sleep(1000);

    Collection<String> tracingMessages = appender.pollLogs(".*\\[\\w+/\\w+/\\w*\\]\\s+INFO.*(logged tracing|/oops).*");
    assertThat(tracingMessages.size(), greaterThanOrEqualTo(2));

    Iterator<String> iterator = tracingMessages.iterator();
    // caller is the root of tracing tree and its traceId is the same as spanId
    String[] ids = tracingIds(iterator.next());
    String parentTraceId = ids[0];
    String parentSpanId = ids[1];

    assertThat(parentTraceId, is(parentSpanId));

    String message = iterator.next();
    // callee is called by caller and inherits caller's traceId but has its own spanId
    ids = tracingIds(message);
    String childTraceId = ids[0];
    String childSpanId = ids[1];
    String childParentId = ids[2];

    assertThat(childTraceId, is(parentTraceId));
    assertThat(childSpanId, is(not(parentTraceId)));
    assertThat(childParentId, is(parentSpanId));
  }

  private String[] tracingIds(String message) {
    return message.replaceFirst(".*\\[(\\w+/\\w+/\\w*)\\].*", "$1").trim().split("/");
  }
}
