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

package org.apache.servicecomb.samples;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class HeaderParamWithListSchemaIT implements CategorizedTestCase {
  RestOperations template = new RestTemplate();

  @Override
  public void testRestTransport() throws Exception {
    testHeaderListDefault();
    testHeaderListMulti();
    testHeaderListCSV();
    testHeaderListSSV();
    testHeaderListPipes();
  }

  // default to multi
  private void testHeaderListDefault() {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("headerList", "a");
    headers.add("headerList", "b");
    headers.add("headerList", "c");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template
        .exchange(Config.GATEWAY_URL + "/headerList/headerListDefault", HttpMethod.GET, entity, String.class).getBody();
    TestMgr.check("3:[a, b, c]", result);
  }

  private void testHeaderListPipes() {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("headerList", "a|b|c");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template
        .exchange(Config.GATEWAY_URL + "/headerList/headerListPIPES", HttpMethod.GET, entity, String.class).getBody();
    TestMgr.check("3:[a, b, c]", result);
  }

  private void testHeaderListSSV() {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("headerList", "a b c");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template
        .exchange(Config.GATEWAY_URL + "/headerList/headerListSSV", HttpMethod.GET, entity, String.class).getBody();
    TestMgr.check("3:[a, b, c]", result);
  }

  private void testHeaderListCSV() {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("headerList", "a,b,c");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template
        .exchange(Config.GATEWAY_URL + "/headerList/headerListCSV", HttpMethod.GET, entity, String.class).getBody();
    TestMgr.check("3:[a, b, c]", result);

    headers.add("headerList", "a, b, c");
    entity = new HttpEntity<>(headers);
    result = template
        .exchange(Config.GATEWAY_URL + "/headerList/headerListCSV", HttpMethod.GET, entity, String.class).getBody();
    TestMgr.check("3:[a, b, c]", result);
  }

  private void testHeaderListMulti() {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("headerList", "a");
    headers.add("headerList", "b");
    headers.add("headerList", "c");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template
        .exchange(Config.GATEWAY_URL + "/headerList/headerListMULTI", HttpMethod.GET, entity, String.class).getBody();
    TestMgr.check("3:[a, b, c]", result);
  }
}
