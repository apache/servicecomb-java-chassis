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

package org.apache.servicecomb.demo.springmvc.client;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.CseHttpEntity;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TestContentType {

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  public void runAllTest() {
    testGlobalSetting();
    testApiOperation();
    testRequestMapping();
    testResponseTypeOverwrite();
  }

  private void testGlobalSetting() {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    CseHttpEntity<String> requestEntity = new CseHttpEntity<>("from testGlobalSetting", requestHeaders);
    ResponseEntity<String> responseEntity = restTemplate
        .exchange("cse://springmvc/contentTypeSpringmvc/testGlobalSetting", HttpMethod.POST,
            requestEntity, String.class);
    TestMgr.check(
        "testGlobalSetting: name=[from testGlobalSetting], request content-type=[" + MediaType.TEXT_PLAIN + "]",
        responseEntity.getBody());
    TestMgr.check(MediaType.TEXT_PLAIN, extractContentType(responseEntity.getHeaders().getContentType()));
  }

  private void testApiOperation() {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    CseHttpEntity<String> requestEntity = new CseHttpEntity<>("from testApiOperation", requestHeaders);
    ResponseEntity<String> responseEntity = restTemplate
        .exchange("cse://springmvc/contentTypeSpringmvc/testApiOperation", HttpMethod.POST,
            requestEntity, String.class);
    TestMgr.check(
        "testApiOperation: name=[from testApiOperation], request content-type=[" + MediaType.APPLICATION_JSON + "]",
        responseEntity.getBody());
    TestMgr.check(MediaType.APPLICATION_JSON, extractContentType(responseEntity.getHeaders().getContentType()));
  }

  private void testRequestMapping() {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    CseHttpEntity<String> requestEntity = new CseHttpEntity<>("from testRequestMapping", requestHeaders);
    ResponseEntity<String> responseEntity = restTemplate
        .exchange("cse://springmvc/contentTypeSpringmvc/testRequestMapping", HttpMethod.POST,
            requestEntity, String.class);
    TestMgr.check(
        "testRequestMapping: name=[from testRequestMapping], request content-type=[" + MediaType.APPLICATION_JSON + "]",
        responseEntity.getBody());
    TestMgr.check(MediaType.APPLICATION_JSON, extractContentType(responseEntity.getHeaders().getContentType()));
  }

  private void testResponseTypeOverwrite() {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity("cse://springmvc/contentTypeSpringmvcOverwrite/testResponseTypeOverwrite", String.class);
    TestMgr.check("testResponseTypeOverwrite: OK", responseEntity.getBody());
    TestMgr.check(MediaType.TEXT_PLAIN, extractContentType(responseEntity.getHeaders().getContentType()));
  }

  private String extractContentType(org.springframework.http.MediaType mediaType) {
    return mediaType.getType() + "/" + mediaType.getSubtype();
  }
}
