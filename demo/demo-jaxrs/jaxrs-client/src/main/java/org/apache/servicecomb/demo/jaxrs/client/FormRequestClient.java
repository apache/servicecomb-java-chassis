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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;


@Component
public class FormRequestClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormRequestClient.class);

  private static final String CSE_URL = "cse://jaxrs/form/formRequest";

  public static void testFormRequest(RestTemplate restTemplate) throws Exception {
    testFormRequestFail(restTemplate);
    //testFormRequestFail maybe close connection，sleep two seconds in case testFormRequestSuccess fail
    Thread.sleep(2000);
    testFormRequestSuccess(restTemplate);
  }


  // formSize is less than default maxFormAttributeSize , success
  private static void testFormRequestSuccess(RestTemplate restTemplate) {
    try {
      ResponseEntity<String> responseEntity = restTemplate
          .postForEntity(FormRequestClient.CSE_URL, createFormHttpEntity(512), String.class);
      TestMgr.check(responseEntity.getBody(), "formRequest success : 512");
    } catch (Throwable e) {
      LOGGER.error("formRequest success error : ", e);
      TestMgr.failed("", e);
    }
  }

  // formSize is greater than default maxFormAttributeSize , throw exception
  private static void testFormRequestFail(RestTemplate restTemplate) {
    try {
      restTemplate.postForEntity(FormRequestClient.CSE_URL, createFormHttpEntity(2048), String.class);
      TestMgr.failed("", new Exception("formRequest fail"));
    } catch (Throwable e) {
      TestMgr.check(e.getMessage().contains("Size exceed allowed maximum capacity"), true);
    }
  }

  private static HttpEntity<MultiValueMap<String, String>> createFormHttpEntity(int size) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("formData", Strings.repeat("a", size));
    return new HttpEntity<>(formData, headers);
  }
}
