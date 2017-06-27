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

package io.servicecomb.tests.tracing.springmvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SpringMvcTracingTest {
  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(9411);

  private final RestTemplate restTemplate = new RestTemplate();

  @BeforeClass
  public static void setUp() throws Exception {
    stubFor(post(urlEqualTo("/api/v1/spans"))
        .withRequestBody(containing("http.path"))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)));

    TracingTestMain.main(new String[0]);
  }

  @Test
  public void sendsTracingToConfiguredAddress() throws InterruptedException {
    ResponseEntity<String> entity = restTemplate.getForEntity("http://localhost:8080/hello", String.class);

    assertThat(entity.getStatusCode(), is(OK));
    assertThat(entity.getBody(), is("hello world"));

    TimeUnit.MILLISECONDS.sleep(1000);

    verify(exactly(1), postRequestedFor(urlEqualTo("/api/v1/spans")));
  }
}
