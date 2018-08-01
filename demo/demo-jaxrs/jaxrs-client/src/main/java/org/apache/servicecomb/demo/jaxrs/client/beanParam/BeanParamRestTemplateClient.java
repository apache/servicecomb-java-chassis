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

package org.apache.servicecomb.demo.jaxrs.client.beanParam;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.CseHttpEntity;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BeanParamRestTemplateClient {
  RestTemplate restTemplate;

  public BeanParamRestTemplateClient() {
    restTemplate = RestTemplateBuilder.create();
  }


  public void testAll() {
    testBeanParam();
    testUpload();
  }

  public void testBeanParam() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "cookieSwaggerLong=11");
    headers.add("headerSwaggerInt", "2");
    HttpEntity<Object> requestEntity1 = new CseHttpEntity<>(headers);
    ResponseEntity<String> result = restTemplate.exchange(
        "cse://jaxrs/beanParamTest/pathSwaggerValue/simple?querySwaggerStr=querySwaggerValue&extraQuery=extra",
        HttpMethod.GET,
        requestEntity1,
        String.class);
    TestMgr.check(
        "invocationContextConsistency=true|testBeanParameter=TestBeanParameter{queryStr='querySwaggerValue', headerInt=2, "
            + "pathStr='pathSwaggerValue', cookieLong=11}|extraQuery=extra",
        result.getBody());
  }

  public void testUpload() {
    BufferedInputStream bufferedInputStream0 = new BufferedInputStream(new ByteArrayInputStream("up0".getBytes()));
    BufferedInputStream bufferedInputStream1 = new BufferedInputStream(new ByteArrayInputStream("up1".getBytes()));
    BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new ByteArrayInputStream("up2".getBytes()));

    HashMap<String, Object> formData = new HashMap<>();
    formData.put("up0", bufferedInputStream0);
    formData.put("up1", bufferedInputStream1);
    formData.put("up2", bufferedInputStream2);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(formData, headers);

    String result = restTemplate.postForObject("cse://jaxrs/beanParamTest/upload?query=fromTemplate&extraQuery=ex",
        entity,
        String.class);
    TestMgr.check(
        "testBeanParameter=TestBeanParameterWithUpload{queryStr='fromTemplate'}|extraQuery=ex|up0=up0|up1=up1|up2=up2",
        result);
  }
}
