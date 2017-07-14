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

package io.servicecomb.tests.tracing;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.seanyinx.github.unit.scaffolding.Poller;
import io.servicecomb.tests.EmbeddedAppender;
import io.servicecomb.tests.Log4jConfig;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ZipkinTracingIntegrationTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(9411);
  private static final EmbeddedAppender appender = new EmbeddedAppender();
  private static final Poller poller = new Poller(10000, 200);
  private final RestTemplate restTemplate = new RestTemplate();

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpLocalRegistry();

    Log4jConfig.addAppender(appender);
    stubFor(post(urlEqualTo("/api/v1/spans"))
        .withRequestBody(containing("http.path"))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)));

  }

  @Before
  public void setUp() throws Exception {
    TracingTestMain.main(new String[0]);
  }

  private static void setUpLocalRegistry() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL resource = loader.getResource("registry.yaml");
    System.setProperty(LOCAL_REGISTRY_FILE_KEY, resource.getPath());
  }

  @Test
  public void sendsTracingToConfiguredAddress() throws InterruptedException {
    ResponseEntity<String> entity = restTemplate.getForEntity("http://localhost:8080/hello", String.class);

    assertThat(entity.getStatusCode(), is(OK));
    assertThat(entity.getBody(), is("hello world, bonjour le monde, hi pojo"));

    TimeUnit.MILLISECONDS.sleep(1000);

    Collection<String> tracingMessages = appender.pollLogs(".*\\[\\w+/\\w+/\\w*\\]\\s+INFO.*in /.*");
    assertThat(tracingMessages.size(), greaterThanOrEqualTo(2));

    Iterator<String> iterator = tracingMessages.iterator();
    // caller is the root of tracing tree and its traceId is the same as spanId
    String[] ids = tracingIds(iterator.next());
    String parentTraceId = ids[0];
    String parentSpanId = ids[1];

    assertThat(parentTraceId, is(parentSpanId));
    while (iterator.hasNext()) {
      String message = iterator.next();
      // callee is called by caller and inherits caller's traceId but has its own spanId
      ids = tracingIds(message);
      String childTraceId = ids[0];
      String childSpanId = ids[1];
      String childParentId = ids[2];

      assertThat(childTraceId, is(parentTraceId));
      assertThat(childSpanId, is(not(parentTraceId)));
      assertThat(childParentId, is(parentSpanId));

      parentSpanId = childSpanId;
    }

    poller.assertEventually(() -> {
          try {
            verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/api/v1/spans")));
            return true;
          } catch (Exception e) {
            return false;
          }
        }
    );
  }

  private String[] tracingIds(String message) {
    return message.replaceFirst(".*\\[(\\w+/\\w+/\\w*)\\].*", "$1").trim().split("/");
  }
}
