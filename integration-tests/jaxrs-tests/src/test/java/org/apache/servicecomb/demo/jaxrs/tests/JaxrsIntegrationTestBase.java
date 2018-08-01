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

package org.apache.servicecomb.demo.jaxrs.tests;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.server.User;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Ignore
public class JaxrsIntegrationTestBase {

  private final String baseUrl = "http://127.0.0.1:8080/";

  private final RestTemplate restTemplate = new RestTemplate();

  private final String codeFirstUrl = baseUrl + "codeFirstJaxrs/";

  private final String schemaFirstUrl = baseUrl + "schemaFirstJaxrs/";

  private final String[] urls = {codeFirstUrl, schemaFirstUrl};

  @Test
  public void ableToQueryAtRootBasePath() {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity(baseUrl + "sayHi?name=Mike", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("Hi Mike"));
  }

  @Test
  public void ableToQueryAtRootPath() {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity(baseUrl, String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("Welcome home"));
  }

  @Test
  public void ablePostMap() {
    Map<String, User> users = new HashMap<>();
    users.put("user1", userOfNames("name11", "name12"));
    users.put("user2", userOfNames("name21", "name22"));

    ParameterizedTypeReference<Map<String, User>> reference = new ParameterizedTypeReference<Map<String, User>>() {
    };
    for (String url : urls) {
      ResponseEntity<Map<String, User>> responseEntity = restTemplate.exchange(url + "testUserMap",
          POST,
          jsonRequest(users),
          reference);

      assertThat(responseEntity.getStatusCode(), is(OK));

      Map<String, User> body = responseEntity.getBody();
      assertArrayEquals(body.get("user1").getNames(), new String[] {"name11", "name12"});
      assertArrayEquals(body.get("user2").getNames(), new String[] {"name21", "name22"});
    }
  }

  private User userOfNames(String... names) {
    User user1 = new User();
    user1.setNames(names);
    return user1;
  }

  @Test
  public void ableToConsumeTextPlain() {
    String body = "a=1";

    for (String url : urls) {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(
          url + "textPlain",
          body,
          String.class);

      assertEquals(body, jsonBodyOf(responseEntity, String.class));
    }
  }

  @Test
  public void ableToPostBytes() throws IOException {
    byte[] body = new byte[] {0, 1, 2};

    for (String url : urls) {
      byte[] result = restTemplate.postForObject(
          url + "bytes",
          jsonRequest(RestObjectMapperFactory.getRestObjectMapper().writeValueAsBytes(body)),
          byte[].class);

      result = RestObjectMapperFactory.getRestObjectMapper().readValue(result, byte[].class);

      assertEquals(1, result[0]);
      assertEquals(1, result[1]);
      assertEquals(2, result[2]);
      assertEquals(3, result.length);
    }
  }

  @Test
  public void getsJaxrsResponse() {
    //    String srcMicroserviceName = RegistryUtils.getMicroservice().getServiceName();
    //    String context = String.format("{x-cse-src-microservice=%s}", srcMicroserviceName);

    ResponseEntity<User> responseEntity = restTemplate
        .getForEntity(codeFirstUrl + "response", User.class);

    assertEquals(202, responseEntity.getStatusCode().value());
    assertEquals("User [name=nameA, age=100, index=0]", responseEntity.getBody().toString());
    //      assertEquals("h1v " + context, responseEntity.getHeaders().getFirst("h1"));
    //      assertEquals("h2v " + context, responseEntity.getHeaders().getFirst("h2"));
  }

  @Test
  public void ableToPostDate() throws Exception {
    ZonedDateTime date = ZonedDateTime.now().truncatedTo(SECONDS);
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("date", RestObjectMapperFactory.getRestObjectMapper().convertToString(Date.from(date.toInstant())));

    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);

    int seconds = 1;
    for (String url : urls) {
      Date result = restTemplate.postForObject(url + "addDate?seconds={seconds}",
          new HttpEntity<>(body, headers),
          Date.class,
          seconds);

      assertEquals(Date.from(date.plusSeconds(seconds).toInstant()), result);
    }
  }

  @Test
  public void ableToDeleteWithQueryString() {
    for (String url : urls) {
      ResponseEntity<String> responseEntity = restTemplate.exchange(url + "addstring?s=a&s=b",
          HttpMethod.DELETE,
          null,
          String.class);

      assertEquals("ab", responseEntity.getBody());
    }
  }

  @Test
  public void ableToGetBoolean() {
    for (String url : urls) {
      boolean result = restTemplate.getForObject(url + "istrue", boolean.class);
      assertEquals(true, result);
    }
  }

  @Test
  public void putsEndWithPathParam() {
    for (String url : urls) {
      ResponseEntity<String> responseEntity = restTemplate
          .exchange(url + "sayhi/{name}", PUT, null, String.class, "world");

      assertThat(responseEntity.getStatusCode(), is(ACCEPTED));
      assertEquals("world sayhi", jsonBodyOf(responseEntity, String.class));
    }
  }

  @Test
  public void putsContainingPathParam() {
    for (String url : urls) {
      ResponseEntity<String> responseEntity = restTemplate
          .exchange(url + "sayhi/{name}/v2", PUT, null, String.class, "world");

      assertEquals("world sayhi 2", jsonBodyOf(responseEntity, String.class));
    }
  }

  @Test
  public void ableToPostWithHeader() {
    Person person = new Person();
    person.setName("person name");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.add("prefix", "prefix  prefix");

    HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);
    for (String url : urls) {
      ResponseEntity<String> responseEntity = restTemplate
          .postForEntity(url + "saysomething", requestEntity, String.class);

      assertEquals("prefix  prefix person name", jsonBodyOf(responseEntity, String.class));
    }
  }

  @Test
  public void ableToPostWithHeaderWithIdentifier() {
    Person person = new Person();
    person.setName("person name");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.add("prefix-test", "prefix  prefix");

    HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);
    for (String url : urls) {
      ResponseEntity<String> responseEntity = restTemplate
          .postForEntity(url + "saysomething1", requestEntity, String.class);

      assertEquals("prefix  prefix person name", jsonBodyOf(responseEntity, String.class));
    }
  }

  @Test
  public void ableToPostObjectAsJson() {
    Map<String, String> personFieldMap = new HashMap<>();
    personFieldMap.put("name", "person name from map");

    for (String url : urls) {
      Person person = restTemplate
          .postForObject(url + "sayhello", jsonRequest(personFieldMap), Person.class);
      assertEquals("hello person name from map", person.toString());

      Person input = new Person();
      input.setName("person name from Object");
      person = restTemplate.postForObject(url + "sayhello", jsonRequest(input), Person.class);

      assertEquals("hello person name from Object", person.toString());
    }
  }

  @Test
  public void ableToPostForm() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("a", "5");
    params.add("b", "3");

    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
    for (String url : urls) {
      int result = restTemplate
          .postForObject(url + "add", new HttpEntity<>(params, headers), Integer.class);

      assertEquals(8, result);
    }
  }

  @Test
  public void ableToExchangeCookie() {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "b=3");

    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<Integer> result = restTemplate.exchange(
        codeFirstUrl + "reduce?a={a}",
        GET,
        requestEntity,
        Integer.class,
        params);

    assertThat(result.getBody(), is(2));
  }

  @Test
  public void ableToExchangeRequestContext() {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");

    ResponseEntity<Integer> result = restTemplate.getForEntity(
        schemaFirstUrl + "reduce?a={a}&b={b}",
        Integer.class,
        params);

    assertThat(result.getBody(), is(2));
  }

  @Test
  public void ableToGetAtDefaultPath() {
    for (String url : urls) {
      int result = restTemplate.getForObject(url, Integer.class);

      assertThat(result, is(100));
    }
  }

  private <T> HttpEntity<T> jsonRequest(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  private <T> T jsonBodyOf(ResponseEntity<String> entity, Class<T> aClass) {
    try {
      return RestObjectMapperFactory.getRestObjectMapper().readValue(entity.getBody(), aClass);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read JSON from response " + entity.getBody(), e);
    }
  }
}
