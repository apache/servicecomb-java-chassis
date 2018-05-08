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

package org.apache.servicecomb.spring.cloud.zuul.tracing;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.tests.tracing.TracingTestBase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.netflix.config.DynamicProperty;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TracedZuulMain.class, webEnvironment = RANDOM_PORT)
public class SpringCloudZuulTracingTest extends TracingTestBase {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("property.test5", "from_system_property");
  }

  @AfterClass
  public static void afterClass() {
    System.clearProperty("property.test5");
  }

  @After
  public void tearDown() {
    appender.clear();
  }

  @Test
  public void tracesCallsReceivedFromZuulToCalledService() throws InterruptedException {
    ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/dummy/rest/blah", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("blah"));

    TimeUnit.MILLISECONDS.sleep(1000);

    Collection<String> tracingMessages = appender.pollLogs(".*\\[\\w+/\\w+/\\w*\\]\\s+INFO.*(logged tracing|/blah).*");
    assertThat(tracingMessages.size(), greaterThanOrEqualTo(2));

    assertThatSpansReceivedByZipkin(tracingMessages, "/dummy/rest/blah", "/blah");
  }

  @Test
  public void tracesFailedCallsReceivedByZuul() throws InterruptedException {
    ResponseEntity<String> response = testRestTemplate.getForEntity("/dummy/rest/oops", String.class);
    assertThat(response.getStatusCodeValue(), is(590));
    assertThat(response.getBody(), is("CommonExceptionData [message=Cse Internal Server Error]"));

    TimeUnit.MILLISECONDS.sleep(1000);

    Collection<String> tracingMessages = appender.pollLogs(".*\\[\\w+/\\w+/\\w*\\]\\s+INFO.*(logged tracing|/oops).*");
    assertThat(tracingMessages.size(), greaterThanOrEqualTo(2));

    assertThatSpansReceivedByZipkin(tracingMessages, "/dummy/rest/oops", "/oops", "590");
  }

  @Test
  public void testGetConfigFromSpringBoot() {
    DynamicProperty dynamicProperty = DynamicProperty.getInstance("property.test0");
    assertEquals("from_properties", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test1");
    assertEquals("from_yml", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test2");
    assertEquals("from_yaml_from_yml", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test3");
    assertEquals("from_yaml_dev_from_properties", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test4");
    assertEquals("from_microservice_yaml", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test5");
    assertEquals("from_system_property", dynamicProperty.getString());

    ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/dummy/rest/testProperty", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(),
        is("from_properties-from_yml-from_yaml_from_yml-from_yaml_dev_from_properties-from_microservice_yaml-from_system_property"));
  }
}
