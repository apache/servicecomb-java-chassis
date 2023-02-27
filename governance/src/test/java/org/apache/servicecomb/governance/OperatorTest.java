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

package org.apache.servicecomb.governance;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.governance.marker.operator.RawOperator;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceCommonConfiguration.class, MockConfiguration.class})
public class OperatorTest {
  private RequestProcessor requestProcessor;

  @Autowired
  public void setRequestProcessor(RequestProcessor requestProcessor) {
    this.requestProcessor = requestProcessor;
  }

  @Test
  public void test_unknown_operator() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/test");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("unknown", "/test");
    matcher.setApiPath(apiPath);
    Assertions.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_match() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/bulkhead");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("exact", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assertions.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_prefix_api_path_match() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/bulkhead/hello");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("prefix", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assertions.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_prefix_api_path_not_match_null() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/bulkhead/hello");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("prefix", null);
    matcher.setApiPath(apiPath);
    Assertions.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_suffix_api_path_match() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/api/bulkhead");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("suffix", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assertions.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_suffix_api_path_not_match_null() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/api/bulkhead");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("suffix", null);
    matcher.setApiPath(apiPath);
    Assertions.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_not_match() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/bulkhead/");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("exact", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    request.setApiPath("/bulkhead");
    request.setApiPath(null);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    request.setApiPath("/bulkhead");
    apiPath.clear();
    matcher.setApiPath(apiPath);
    Assertions.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_match_header_match() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/bulkhead");
    request.setMethod("GET");
    Map<String, String> reqHeaders = new HashMap<>();
    reqHeaders.put("header1", "value1");
    request.setHeaders(reqHeaders);
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("exact", "/bulkhead");
    matcher.setApiPath(apiPath);
    matcher.setMethod(Arrays.asList("GET"));
    Map<String, RawOperator> headers = new HashMap<>();
    RawOperator header1 = new RawOperator();
    header1.put("exact", "value1");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_match_header_not_match() {
    GovernanceRequest request = new GovernanceRequest();
    request.setApiPath("/bulkhead");
    request.setMethod("GET");
    Map<String, String> reqHeaders = new HashMap<>();
    reqHeaders.put("header1", "value2");
    request.setHeaders(reqHeaders);
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("exact", "/bulkhead");
    matcher.setApiPath(apiPath);
    matcher.setMethod(Arrays.asList("GET"));
    Map<String, RawOperator> headers = new HashMap<>();
    RawOperator header1 = new RawOperator();
    header1.put("exact", "value1");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    reqHeaders.clear();
    request.setHeaders(reqHeaders);
    Assertions.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_header_low_case() {
    GovernanceRequest request = new GovernanceRequest();
    Map<String, String> reqHeaders = new HashMap<>();
    reqHeaders.put("hEadeR", "100");
    request.setHeaders(reqHeaders);
    Matcher matcher = new Matcher();
    Map<String, RawOperator> headers = new HashMap<>();
    RawOperator header1 = new RawOperator();
    header1.put("compare", ">10");
    headers.put("HeAder", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_compare_header_match() {
    GovernanceRequest request = new GovernanceRequest();
    Map<String, String> reqHeaders = new HashMap<>();
    reqHeaders.put("header1", "100");
    request.setHeaders(reqHeaders);
    Matcher matcher = new Matcher();
    Map<String, RawOperator> headers = new HashMap<>();

    RawOperator header1 = new RawOperator();
    header1.put("compare", ">10");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", ">=10");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", "<1000");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", "<=1000");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", "=100");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_compare_header_not_match() {
    GovernanceRequest request = new GovernanceRequest();
    Map<String, String> reqHeaders = new HashMap<>();
    reqHeaders.put("header1", "100");
    request.setHeaders(reqHeaders);
    Matcher matcher = new Matcher();
    Map<String, RawOperator> headers = new HashMap<>();

    RawOperator header1 = new RawOperator();
    header1.put("compare", ">1000");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", ">=1000");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", "<10");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", "<=10");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertFalse(requestProcessor.match(request, matcher));

    header1 = new RawOperator();
    header1.put("compare", "=200");
    headers.put("header1", header1);
    matcher.setHeaders(headers);
    Assertions.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_time_changed_to_duration() {
    RetryPolicy retryPolicy = new RetryPolicy();
    String result;
    result = retryPolicy.stringOfDuration("100", Duration.ofMillis(10));
    Assertions.assertEquals("PT0.1S", result);
    Assertions.assertEquals(100, Duration.parse(result).toMillis());

    result = retryPolicy.stringOfDuration("3S", Duration.ofMillis(10));
    Assertions.assertEquals("PT3S", result);
    Assertions.assertEquals(3000, Duration.parse(result).toMillis());

    result = retryPolicy.stringOfDuration("1M", Duration.ofMillis(10));
    Assertions.assertEquals("PT1M", result);
    Assertions.assertEquals(60000, Duration.parse(result).toMillis());

    result = retryPolicy.stringOfDuration("1H", Duration.ofMillis(10));
    Assertions.assertEquals("PT1H", result);
    Assertions.assertEquals(3600000, Duration.parse(result).toMillis());

    result = retryPolicy.stringOfDuration("3", Duration.ofMillis(10));
    Assertions.assertEquals("PT0.003S", result);
    Assertions.assertEquals(3, Duration.parse(result).toMillis());
  }
}
