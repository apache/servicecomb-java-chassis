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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.governance.marker.GovHttpRequest;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.governance.marker.operator.RawOperator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigFileApplicationContextInitializer.class)
public class OperatorTest {
  @Autowired
  private RequestProcessor requestProcessor;

  @Test
  public void test_exact_api_path_match() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/bulkhead");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("exact", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assert.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_prefix_api_path_match() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/bulkhead/hello");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("prefix", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assert.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_prefix_api_path_not_match_null() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/bulkhead/hello");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("prefix", null);
    matcher.setApiPath(apiPath);
    Assert.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_suffix_api_path_match() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/api/bulkhead");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("suffix", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assert.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_suffix_api_path_not_match_null() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/api/bulkhead");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("suffix", null);
    matcher.setApiPath(apiPath);
    Assert.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_not_match() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/bulkhead/");
    Matcher matcher = new Matcher();
    RawOperator apiPath = new RawOperator();
    apiPath.put("exact", "/bulkhead");
    matcher.setApiPath(apiPath);
    Assert.assertFalse(requestProcessor.match(request, matcher));

    request.setUri("/bulkhead");
    request.setUri(null);
    Assert.assertFalse(requestProcessor.match(request, matcher));

    request.setUri("/bulkhead");
    apiPath.clear();
    matcher.setApiPath(apiPath);
    Assert.assertFalse(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_match_header_match() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/bulkhead");
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
    Assert.assertTrue(requestProcessor.match(request, matcher));
  }

  @Test
  public void test_exact_api_path_match_header_not_match() {
    GovHttpRequest request = new GovHttpRequest("service", "1.0");
    request.setUri("/bulkhead");
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
    Assert.assertFalse(requestProcessor.match(request, matcher));

    reqHeaders.clear();
    request.setHeaders(reqHeaders);
    Assert.assertFalse(requestProcessor.match(request, matcher));
  }
}
