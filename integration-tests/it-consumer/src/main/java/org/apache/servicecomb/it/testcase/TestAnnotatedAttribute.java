package org.apache.servicecomb.it.testcase;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TestAnnotatedAttribute {
  interface AnnotatedAttributeIntf {
    String fromHeader(String inputs);
  }

  private static Consumers<AnnotatedAttributeIntf> consumersSpringmvc =
      new Consumers<>("annotatedAttributeSpringmvc", AnnotatedAttributeIntf.class);

  @BeforeClass
  public static void classSetup() {
    consumersSpringmvc.init(ITJUnitUtils.getTransport());
  }

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
  public void fromPath_springmvc_rt() {
    fromPath_rt(consumersSpringmvc);
  }

  @Test
  public void fromPart_springmvc_rt() {
    fromPart_rt(consumersSpringmvc);
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
    assertEquals("default,fromValue,fromName", response.getBody());
  }

  protected void fromQuery_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    String result = consumers.getSCBRestTemplate()
        .getForObject("/fromQuery?input={1}&input2={2}&input3={3}",
            String.class,
            "default",
            "fromValue",
            "fromName");
    assertEquals("default,fromValue,fromName", result);
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
    assertEquals("default,fromValue,fromName", result.getBody());
  }

  protected void fromPath_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    String result = consumers.getSCBRestTemplate()
        .getForObject("/fromPath/{1}/{2}/{3}",
            String.class,
            "default",
            "fromValue",
            "fromName");
    assertEquals("default,fromValue,fromName", result);
  }

  protected void fromPart_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("input", "default");
    map.add("input2", "fromValue");
    map.add("input3", "fromName");

    String result = consumers.getSCBRestTemplate()
        .postForObject("/fromPart",
            new HttpEntity<>(map),
            String.class);
    assertEquals("default,fromValue,fromName", result);
  }

  protected void fromAttribute_rt(Consumers<AnnotatedAttributeIntf> consumers) {
    Map<String, Object> body = new HashMap<>();
    body.put("input", "default");
    body.put("input2", "fromValue");
    body.put("input3", "fromName");
    ResponseEntity<Date> result = consumers.getSCBRestTemplate()
        .exchange("/fromAttribute",
            HttpMethod.POST,
            new HttpEntity<>(body),
            Date.class);
    assertEquals("default,fromValue,fromName", result.getBody());
  }
}
