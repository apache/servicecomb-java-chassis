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
package org.apache.servicecomb.it.testcase;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TestAnnotatedAttribute {
  interface AnnotatedAttributeIntf {
    String fromHeader(String inputs);
  }

  private static Consumers<AnnotatedAttributeIntf> consumersSpringmvc =
      new Consumers<>("annotatedAttributeSpringmvc", AnnotatedAttributeIntf.class);

  @Test
  public void fromHeader_springmvc_rt() {
    fromHeader_rt(consumersSpringmvc);
  }

  @Test
  public void fromQuery_springmvc_rt() {
    fromQuery_rt(consumersSpringmvc);
  }

  @Test
  public void fromCookie_springmvc_rt() {
    fromCookie_rt(consumersSpringmvc);
  }

  @Test
  public void fromCookieRequired_springmvc_rt() {
    should_throw_exception_when_required_cookie_parameter_not_set_value_call_by_restTemplate(consumersSpringmvc);
  }

  @Test
  public void fromCookieDefaultValue_springmvc_rt() {
    fromCookieDefaultValue_rt(consumersSpringmvc);
  }

  @Test
  public void fromPath_springmvc_rt() {
    fromPath_rt(consumersSpringmvc);
  }

  @Test
  public void fromAttribute_springmvc_rt() {
    fromAttribute_rt(consumersSpringmvc);
  }

  protected void fromHeader_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", "default");
    headers.add("input2", "fromValue");
    headers.add("input3", "fromName");

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/fromHeader",
            HttpMethod.GET,
            entity,
            String.class);
    Assertions.assertEquals("default,fromValue,fromName", response.getBody());
  }

  protected void fromQuery_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    String result = consumers.getSCBRestTemplate()
        .getForObject("/fromQuery?input={1}&input2={2}&input3={3}",
            String.class,
            "default",
            "fromValue",
            "fromName");
    Assertions.assertEquals("default,fromValue,fromName", result);
  }

  protected void fromCookie_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "input=default");
    headers.add(HttpHeaders.COOKIE, "input2=fromValue");
    headers.add(HttpHeaders.COOKIE, "input3=fromName");

    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> result = consumers.getSCBRestTemplate()
        .exchange("/fromCookie",
            HttpMethod.GET,
            requestEntity,
            String.class);
    Assertions.assertEquals("default,fromValue,fromName", result.getBody());
  }

  protected void should_throw_exception_when_required_cookie_parameter_not_set_value_call_by_restTemplate(
      Consumers<AnnotatedAttributeIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    try {
      consumers.getSCBRestTemplate()
          .exchange("/fromCookieRequired",
              HttpMethod.GET,
              requestEntity,
              String.class);
      Assertions.assertEquals("required is true, throw exception", "but not throw exception");
    } catch (InvocationException e) {
      Assertions.assertEquals(400, e.getStatusCode());
      Assertions.assertTrue(
          e.getMessage()
              .contains("InvocationException: code=400;msg=CommonExceptionData [message=Parameter is not valid"));
    }
    headers.add(HttpHeaders.COOKIE, "input1=default1");
    requestEntity = new HttpEntity<>(headers);
    try {
      consumers.getSCBRestTemplate()
          .exchange("/fromCookieRequired",
              HttpMethod.GET,
              requestEntity,
              String.class);
      Assertions.assertEquals("required is true, throw exception", "but not throw exception");
    } catch (InvocationException e) {
      Assertions.assertEquals(400, e.getStatusCode());
      Assertions.assertTrue(
          e.getMessage()
              .contains("InvocationException: code=400;msg=CommonExceptionData [message=Parameter is not valid"));
    }
    headers.add(HttpHeaders.COOKIE, "input=joker");
    requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> result = consumers.getSCBRestTemplate()
        .exchange("/fromCookieRequired",
            HttpMethod.GET,
            requestEntity,
            String.class);
    Assertions.assertEquals("joker", result.getBody());
  }

  protected void fromCookieDefaultValue_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> result = consumers.getSCBRestTemplate()
        .exchange("/fromCookieDefaultValue",
            HttpMethod.GET,
            requestEntity,
            String.class);
    Assertions.assertEquals("default", result.getBody());

    headers.add(HttpHeaders.COOKIE, "input1=jokers");
    requestEntity = new HttpEntity<>(headers);
    result = consumers.getSCBRestTemplate()
        .exchange("/fromCookieDefaultValue",
            HttpMethod.GET,
            requestEntity,
            String.class);
    Assertions.assertEquals("default", result.getBody());

    headers.add(HttpHeaders.COOKIE, "input=joker");
    requestEntity = new HttpEntity<>(headers);
    result = consumers.getSCBRestTemplate()
        .exchange("/fromCookieDefaultValue",
            HttpMethod.GET,
            requestEntity,
            String.class);
    Assertions.assertEquals("joker", result.getBody());
  }

  protected void fromPath_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    String result = consumers.getSCBRestTemplate()
        .getForObject("/fromPath/{1}/{2}/{3}",
            String.class,
            "default",
            "fromValue",
            "fromName");
    Assertions.assertEquals("default,fromValue,fromName", result);
  }

  protected void fromAttribute_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    Map<String, Object> body = new HashMap<>();
    body.put("input", "default");
    body.put("input2", "fromValue");
    body.put("input3", "fromName");
    ResponseEntity<String> result = consumers.getSCBRestTemplate()
        .exchange("/fromAttribute",
            HttpMethod.POST,
            new HttpEntity<>(body),
            String.class);
    Assertions.assertEquals("default,fromValue,fromName", result.getBody());
  }
}
