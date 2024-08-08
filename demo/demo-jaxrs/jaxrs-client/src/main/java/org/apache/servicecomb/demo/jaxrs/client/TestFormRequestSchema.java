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

package org.apache.servicecomb.demo.jaxrs.client;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;


@Component
public class TestFormRequestSchema implements CategorizedTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestFormRequestSchema.class);

  private final RestOperations restTemplate = RestTemplateBuilder.create();

  @Override
  public void testRestTransport() throws Exception {
    testFormRequestFail();
    // testFormRequestFail会关闭连接，防止下个测试用例失败，睡眠2s
    Thread.sleep(2000);
    testFormRequestSuccess();
    testFormRequestBufferSize();
  }

  // formSize is less than default maxFormAttributeSize , success
  private void testFormRequestSuccess() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("formData", "a".repeat(512));
      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
      ResponseEntity<String> responseEntity = restTemplate
          .postForEntity("cse://jaxrs/form/formRequest", requestEntity, String.class);
      TestMgr.check(responseEntity.getBody(), "formRequest success : 512");
    } catch (Throwable e) {
      LOGGER.error("testFormRequestSuccess-->", e);
      TestMgr.failed("", e);
    }
  }

  // formSize is greater than default maxFormAttributeSize , throw exception
  private void testFormRequestFail() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("formData", "a".repeat(1688));
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
    try {
      restTemplate.postForEntity("cse://jaxrs/form/formRequest", requestEntity, String.class);
      TestMgr.fail("Size exceed allowed maximum capacity");
    } catch (Throwable e) {
      TestMgr.check(e.getMessage().contains("Internal Server Error"), true);
    }
  }

  private void testFormRequestBufferSize() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    // we can not test a situation for form exceed max buffer size, because the netty buffer is very
    // big and the trunc can always be decoded and cached buffer size is always 0.
    formData.add("F0123456789001234567890012345678900123456789001234567890"
            + "0123456789001234567890012345678900123456789001234567890", "a".repeat(1020)
        // we can not test a situation for form exceed max buffer size, because the netty buffer is very
        // big and the trunc can always be decoded and cached buffer size is always 0.
    );
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
    try {
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity("cse://jaxrs/form/formLongName", requestEntity, String.class);
      TestMgr.check(responseEntity.getBody(), "formRequest success : 1020");
    } catch (Throwable e) {
      LOGGER.error("testFormRequestBufferSize-->", e);
      TestMgr.failed("", e);
    }
  }
}
