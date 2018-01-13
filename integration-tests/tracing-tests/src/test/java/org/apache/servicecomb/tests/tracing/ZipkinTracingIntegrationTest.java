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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class ZipkinTracingIntegrationTest extends TracingTestBase {

  @Before
  public void setUp() throws Exception {
    TracingTestMain.main(new String[0]);
  }

  @Test
  public void sendsTracingToConfiguredAddress() throws InterruptedException, NoSuchMethodException {
    ResponseEntity<String> entity = restTemplate.getForEntity("http://localhost:8080/hello", String.class);

    assertThat(entity.getStatusCode(), is(OK));
    assertThat(entity.getBody(), is("hello world, bonjour le monde, hi pojo"));

    TimeUnit.MILLISECONDS.sleep(1000);

    Collection<String> tracingMessages = appender.pollLogs(".*\\[\\w+/\\w+/\\w*\\]\\s+INFO.*in /.*");
    assertThat(tracingMessages.size(), greaterThanOrEqualTo(2));

    assertThatSpansReceivedByZipkin(tracingMessages,
        "/hello",
        SlowRepo.class.getMethod("crawl").toString(),
        "/bonjour",
        "/pojo");
  }
}
