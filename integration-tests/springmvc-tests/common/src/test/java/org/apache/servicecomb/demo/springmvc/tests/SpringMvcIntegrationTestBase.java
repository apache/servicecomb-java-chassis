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

package org.apache.servicecomb.demo.springmvc.tests;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.provider.springmvc.reference.async.CseAsyncRestTemplate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Disabled
@SuppressWarnings("deprecation")
// TODO : upgrade to spring 5 will having warning's , we'll fix it later
public class SpringMvcIntegrationTestBase {
  @TempDir
  public static Path folder;

  private final String baseUrl = "http://127.0.0.1:8080/";

  private final RestTemplate restTemplate = new RestTemplate();

  private final org.springframework.web.client.AsyncRestTemplate asyncRestTemplate = new org.springframework.web.client.AsyncRestTemplate();

  private final String codeFirstUrl = baseUrl + "codeFirstSpringmvc/";

  private final String controllerUrl = baseUrl + "springmvc/controller/";

  @Test
  public void ableToQueryAtRootBasePath() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity(baseUrl + "sayHi?name=Mike", String.class);

    MatcherAssert.assertThat(responseEntity.getStatusCode(), is(OK));
    MatcherAssert.assertThat(responseEntity.getBody(), is("Hi Mike"));

    responseEntity = restTemplate
        .getForEntity(baseUrl + "sayHi?name={name}", String.class, "小 强");

    MatcherAssert.assertThat(responseEntity.getStatusCode(), is(OK));
    MatcherAssert.assertThat(responseEntity.getBody(), is("Hi 小 强"));

    //integration test for AsyncRestTemplate
    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .getForEntity(baseUrl + "sayHi?name=Mike", String.class);
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getStatusCode(), is(OK));
    MatcherAssert.assertThat(futureResponse.getBody(), is("Hi Mike"));

    listenableFuture = asyncRestTemplate.getForEntity(baseUrl + "sayHi?name={name}", String.class, "小 强");
    futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getStatusCode(), is(OK));
    MatcherAssert.assertThat(futureResponse.getBody(), is("Hi 小 强"));
  }

  @Test
  public void ableToQueryAtRootPath() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity(baseUrl, String.class);

    MatcherAssert.assertThat(responseEntity.getStatusCode(), is(OK));
    MatcherAssert.assertThat(responseEntity.getBody(), is("Welcome home"));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate.getForEntity(baseUrl, String.class);
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getStatusCode(), is(OK));
    MatcherAssert.assertThat(futureResponse.getBody(), is("Welcome home"));
  }

  @Test
  public void ableToQueryAtNonRootPath() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity(baseUrl + "french/bonjour?name=Mike", String.class);

    MatcherAssert.assertThat(responseEntity.getStatusCode(), is(OK));
    MatcherAssert.assertThat(responseEntity.getBody(), is("Bonjour Mike"));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .getForEntity(baseUrl + "french/bonjour?name=Mike", String.class);
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getStatusCode(), is(OK));
    MatcherAssert.assertThat(futureResponse.getBody(), is("Bonjour Mike"));
  }

  @Test
  public void ableToPostMap() throws Exception {
    Map<String, User> users = new HashMap<>();
    users.put("user1", userOfNames("name11", "name12"));
    users.put("user2", userOfNames("name21", "name22"));

    ParameterizedTypeReference<Map<String, User>> reference = new ParameterizedTypeReference<Map<String, User>>() {
    };
    ResponseEntity<Map<String, User>> responseEntity = restTemplate.exchange(codeFirstUrl + "testUserMap",
        POST,
        jsonRequest(users),
        reference);

    MatcherAssert.assertThat(responseEntity.getStatusCode(), is(OK));

    Map<String, User> body = responseEntity.getBody();
    Assertions.assertArrayEquals(body.get("user1").getNames(), new String[] {"name11", "name12"});
    Assertions.assertArrayEquals(body.get("user2").getNames(), new String[] {"name21", "name22"});

    ListenableFuture<ResponseEntity<Map<String, User>>> listenableFuture = asyncRestTemplate
        .exchange(codeFirstUrl + "testUserMap",
            POST,
            jsonRequest(users),
            reference);
    ResponseEntity<Map<String, User>> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getStatusCode(), is(OK));
    body = futureResponse.getBody();
    Assertions.assertArrayEquals(body.get("user1").getNames(), new String[] {"name11", "name12"});
    Assertions.assertArrayEquals(body.get("user2").getNames(), new String[] {"name21", "name22"});
  }

  private User userOfNames(String... names) {
    User user1 = new User();
    user1.setNames(names);
    return user1;
  }

  @Test
  public void ableToConsumeTextPlain() throws Exception {
    String body = "a=1";
    String result = restTemplate.postForObject(
        codeFirstUrl + "textPlain",
        body,
        String.class);

    MatcherAssert.assertThat(jsonOf(result, String.class), is(body));

    HttpEntity<?> entity = new HttpEntity<>(body);
    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "textPlain", entity, String.class);
    ResponseEntity<String> responseEntity = listenableFuture.get();
    MatcherAssert.assertThat(jsonOf(responseEntity.getBody(), String.class), is(body));
  }

  @Test
  public void ableToPostBytes() throws Exception {
    byte[] body = new byte[] {0, 1, 2};

    byte[] result = restTemplate.postForObject(
        codeFirstUrl + "bytes",
        jsonRequest(RestObjectMapperFactory.getRestObjectMapper().writeValueAsBytes(body)),
        byte[].class);

    result = RestObjectMapperFactory.getRestObjectMapper().readValue(result, byte[].class);

    Assertions.assertEquals(1, result[0]);
    Assertions.assertEquals(1, result[1]);
    Assertions.assertEquals(2, result[2]);
    Assertions.assertEquals(3, result.length);

    ListenableFuture<ResponseEntity<byte[]>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "bytes",
            jsonRequest(RestObjectMapperFactory.getRestObjectMapper().writeValueAsBytes(body)),
            byte[].class);
    ResponseEntity<byte[]> responseEntity = listenableFuture.get();
    result = RestObjectMapperFactory.getRestObjectMapper().readValue(responseEntity.getBody(), byte[].class);
    Assertions.assertEquals(1, result[0]);
    Assertions.assertEquals(1, result[1]);
    Assertions.assertEquals(2, result[2]);
    Assertions.assertEquals(3, result.length);
  }

  @Test
  public void ableToUploadFile() throws Exception {
    String file1Content = "hello world";
    String file2Content = "bonjour";
    String username = "mike";

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file1", new FileSystemResource(newFile(file1Content).getAbsolutePath()));
    map.add("someFile", new FileSystemResource(newFile(file2Content).getAbsolutePath()));
    map.add("name", username);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = restTemplate.postForObject(
        codeFirstUrl + "upload",
        new HttpEntity<>(map, headers),
        String.class);

    MatcherAssert.assertThat(result, is(file1Content + file2Content + username));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "upload",
            new HttpEntity<>(map, headers),
            String.class);
    ResponseEntity<String> responseEntity = listenableFuture.get();
    MatcherAssert.assertThat(responseEntity.getBody(), is(file1Content + file2Content + username));
  }

  @Test
  public void ableToUploadFileFromConsumer() throws Exception {
    String file1Content = "hello world";
    String file2Content = "bonjour";
    String username = "mike";

    Map<String, Object> map = new HashMap<>();
    map.put("file1", new FileSystemResource(newFile(file1Content).getAbsolutePath()));
    map.put("someFile", new FileSystemResource(newFile(file2Content).getAbsolutePath()));
    map.put("name", username);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = RestTemplateBuilder.create().postForObject(
        "cse://springmvc-tests/codeFirstSpringmvc/upload",
        new HttpEntity<>(map, headers),
        String.class);

    MatcherAssert.assertThat(result, is(file1Content + file2Content + username));
    org.springframework.web.client.AsyncRestTemplate cseAsyncRestTemplate = new CseAsyncRestTemplate();
    ListenableFuture<ResponseEntity<String>> listenableFuture = cseAsyncRestTemplate
        .postForEntity("cse://springmvc-tests/codeFirstSpringmvc/upload",
            new HttpEntity<>(map, headers),
            String.class);
    ResponseEntity<String> responseEntity = listenableFuture.get();
    MatcherAssert.assertThat(responseEntity.getBody(), is(file1Content + file2Content + username));
  }

  @Test
  public void ableToUploadFileWithoutAnnotation() throws Exception {
    String file1Content = "hello world";
    String file2Content = "bonjour";
    String username = "mike";

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file1", new FileSystemResource(newFile(file1Content).getAbsolutePath()));
    map.add("file2", new FileSystemResource(newFile(file2Content).getAbsolutePath()));
    map.add("name", username);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = restTemplate.postForObject(
        codeFirstUrl + "uploadWithoutAnnotation",
        new HttpEntity<>(map, headers),
        String.class);

    MatcherAssert.assertThat(result, is(file1Content + file2Content + username));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "uploadWithoutAnnotation",
            new HttpEntity<>(map, headers),
            String.class);
    ResponseEntity<String> responseEntity = listenableFuture.get();
    MatcherAssert.assertThat(responseEntity.getBody(), is(file1Content + file2Content + username));
  }

  @Test
  public void blowsUpWhenFileNameDoesNotMatch() throws Exception {
    String file1Content = "hello world";
    String file2Content = "bonjour";

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file1", new FileSystemResource(newFile(file1Content).getAbsolutePath()));
    map.add("unmatched name", new FileSystemResource(newFile(file2Content).getAbsolutePath()));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<String> response = null;
    try {
      response = restTemplate
          .postForEntity(codeFirstUrl + "uploadWithoutAnnotation", new HttpEntity<>(map, headers), String.class);
      Assertions.assertEquals("required is true, throw exception", "but not throw exception");
    } catch (HttpClientErrorException e) {
      Assertions.assertEquals(400, e.getRawStatusCode());
      Assertions.assertEquals("Bad Request", e.getStatusCode().getReasonPhrase());
    }
  }

  @Test
  public void ableToPostDate() throws Exception {
    ZonedDateTime date = ZonedDateTime.now().truncatedTo(SECONDS);
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("date", RestObjectMapperFactory.getRestObjectMapper().convertToString(Date.from(date.toInstant())));

    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);

    int seconds = 1;
    Date result = restTemplate.postForObject(codeFirstUrl + "addDate?seconds={seconds}",
        new HttpEntity<>(body, headers),
        Date.class,
        seconds);

    MatcherAssert.assertThat(result, is(Date.from(date.plusSeconds(seconds).toInstant())));

    ListenableFuture<ResponseEntity<Date>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "addDate?seconds={seconds}",
            new HttpEntity<>(body, headers),
            Date.class,
            seconds);
    ResponseEntity<Date> dateResponseEntity = listenableFuture.get();
    MatcherAssert.assertThat(dateResponseEntity.getBody(), is(Date.from(date.plusSeconds(seconds).toInstant())));
  }

  @Test
  public void ableToDeleteWithQueryString() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate.exchange(codeFirstUrl + "addstring?s=a&s=b",
        HttpMethod.DELETE,
        null,
        String.class);

    MatcherAssert.assertThat(responseEntity.getBody(), is("ab"));
    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .exchange(codeFirstUrl + "addstring?s=a&s=b", HttpMethod.DELETE, null, String.class);
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getBody(), is("ab"));
  }

  @Test
  public void ableToGetBoolean() throws Exception {
    boolean result = restTemplate.getForObject(codeFirstUrl + "istrue", boolean.class);

    MatcherAssert.assertThat(result, is(true));

    ListenableFuture<ResponseEntity<Boolean>> listenableFuture = asyncRestTemplate
        .getForEntity(codeFirstUrl + "istrue", boolean.class);
    ResponseEntity<Boolean> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getBody(), is(true));
  }

  @Test
  public void putsEndWithPathParam() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate
        .exchange(codeFirstUrl + "sayhi/{name}", PUT, null, String.class, "world");

    MatcherAssert.assertThat(responseEntity.getStatusCode(), is(ACCEPTED));
    MatcherAssert.assertThat(jsonOf(responseEntity.getBody(), String.class), is("world sayhi"));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .exchange(codeFirstUrl + "sayhi/{name}", PUT, null, String.class, "world");
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getStatusCode(), is(ACCEPTED));
    MatcherAssert.assertThat(jsonOf(futureResponse.getBody(), String.class), is("world sayhi"));
  }

  @Test
  public void putsContainingPathParam() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate
        .exchange(codeFirstUrl + "sayhi/{name}/v2", PUT, null, String.class, "world");

    MatcherAssert.assertThat(jsonOf(responseEntity.getBody(), String.class), is("world sayhi 2"));
    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .exchange(codeFirstUrl + "sayhi/{name}/v2", PUT, null, String.class, "world");
    responseEntity = listenableFuture.get();
    MatcherAssert.assertThat(jsonOf(responseEntity.getBody(), String.class), is("world sayhi 2"));
  }

  @Test
  public void ableToPostWithHeader() throws Exception {
    Person person = new Person();
    person.setName("person name");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.add("prefix", "prefix  prefix");

    HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);
    ResponseEntity<String> responseEntity = restTemplate
        .postForEntity(codeFirstUrl + "saysomething", requestEntity, String.class);

    MatcherAssert.assertThat(jsonOf(responseEntity.getBody(), String.class), is("prefix  prefix person name"));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "saysomething", requestEntity, String.class);
    responseEntity = listenableFuture.get();
    MatcherAssert.assertThat(jsonOf(responseEntity.getBody(), String.class), is("prefix  prefix person name"));
  }

  @Test
  public void ableToPostObjectAsJson() throws Exception {
    Map<String, String> personFieldMap = new HashMap<>();
    personFieldMap.put("name", "person name from map");

    Person person = restTemplate
        .postForObject(codeFirstUrl + "sayhello", jsonRequest(personFieldMap), Person.class);
    MatcherAssert.assertThat(person.toString(), is("hello person name from map"));

    Person input = new Person();
    input.setName("person name from Object");
    person = restTemplate.postForObject(codeFirstUrl + "sayhello", jsonRequest(input), Person.class);

    MatcherAssert.assertThat(person.toString(), is("hello person name from Object"));

    ListenableFuture<ResponseEntity<Person>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "sayhello", jsonRequest(personFieldMap), Person.class);
    ResponseEntity<Person> futureResponse = listenableFuture.get();
    person = futureResponse.getBody();
    MatcherAssert.assertThat(person.toString(), is("hello person name from map"));

    listenableFuture = asyncRestTemplate.postForEntity(codeFirstUrl + "sayhello", jsonRequest(input), Person.class);
    futureResponse = listenableFuture.get();
    person = futureResponse.getBody();
    MatcherAssert.assertThat(person.toString(), is("hello person name from Object"));
  }

  @Test
  public void ableToPostForm() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("a", "5");
    params.add("b", "3");

    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
    int result = restTemplate
        .postForObject(codeFirstUrl + "add", new HttpEntity<>(params, headers), Integer.class);

    MatcherAssert.assertThat(result, is(8));

    ListenableFuture<ResponseEntity<Integer>> listenableFuture = asyncRestTemplate
        .postForEntity(codeFirstUrl + "add", new HttpEntity<>(params, headers), Integer.class);
    ResponseEntity<Integer> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getBody(), is(8));
  }

  @Test
  public void ableToExchangeCookie() throws Exception {
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

    MatcherAssert.assertThat(result.getBody(), is(2));

    ListenableFuture<ResponseEntity<Integer>> listenableFuture = asyncRestTemplate
        .exchange(codeFirstUrl + "reduce?a={a}",
            GET,
            requestEntity,
            Integer.class,
            params);
    result = listenableFuture.get();
    MatcherAssert.assertThat(result.getBody(), is(2));
  }

  @Test
  public void getsEndWithRequestVariables() throws Exception {
    int result = restTemplate.getForObject(
        controllerUrl + "add?a={a}&b={b}",
        Integer.class,
        3,
        4);

    MatcherAssert.assertThat(result, is(7));
    ListenableFuture<ResponseEntity<Integer>> listenableFuture = asyncRestTemplate
        .getForEntity(controllerUrl + "add?a={a}&b={b}",
            Integer.class,
            3,
            4);
    ResponseEntity<Integer> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getBody(), is(7));
  }

  @Test
  public void postsEndWithPathParam() throws Exception {
    String result = restTemplate.postForObject(
        controllerUrl + "sayhello/{name}",
        null,
        String.class,
        "world");

    MatcherAssert.assertThat(jsonOf(result, String.class), is("hello world"));

    List<HttpMessageConverter<?>> convertersOld = restTemplate.getMessageConverters();
    List<HttpMessageConverter<?>> converters = new ArrayList<>();
    converters.add(new MappingJackson2HttpMessageConverter());
    restTemplate.setMessageConverters(converters);
    result = restTemplate.postForObject(
        controllerUrl + "sayhello/{name}",
        null,
        String.class,
        "中 国");

    MatcherAssert.assertThat(result, is("hello 中 国"));
    restTemplate.setMessageConverters(convertersOld);

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .postForEntity(controllerUrl + "sayhello/{name}",
            null,
            String.class,
            "world");
    ResponseEntity<String> futureResonse = listenableFuture.get();
    MatcherAssert.assertThat(jsonOf(futureResonse.getBody(), String.class), is("hello world"));
    asyncRestTemplate.setMessageConverters(converters);
    listenableFuture = asyncRestTemplate.postForEntity(controllerUrl + "sayhello/{name}",
        null,
        String.class,
        "中 国");
    futureResonse = listenableFuture.get();
    MatcherAssert.assertThat(futureResonse.getBody(), is("hello 中 国"));
    asyncRestTemplate.setMessageConverters(convertersOld);
  }

  @Test
  public void ableToPostObjectAsJsonWithRequestVariable() throws Exception {
    Person input = new Person();
    input.setName("world");

    String result = restTemplate.postForObject(
        controllerUrl + "saysomething?prefix={prefix}",
        jsonRequest(input),
        String.class,
        "hello");

    MatcherAssert.assertThat(jsonOf(result, String.class), is("hello world"));

    List<HttpMessageConverter<?>> convertersOld = restTemplate.getMessageConverters();
    List<HttpMessageConverter<?>> converters = new ArrayList<>();
    converters.add(new MappingJackson2HttpMessageConverter());
    restTemplate.setMessageConverters(converters);
    input = new Person();
    input.setName("中国");

    result = restTemplate.postForObject(
        controllerUrl + "saysomething?prefix={prefix}",
        jsonRequest(input),
        String.class,
        "hello");

    MatcherAssert.assertThat(result, is("hello 中国"));
    restTemplate.setMessageConverters(convertersOld);

    input.setName("world");
    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .postForEntity(controllerUrl + "saysomething?prefix={prefix}",
            jsonRequest(input),
            String.class,
            "hello");
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(jsonOf(futureResponse.getBody(), String.class), is("hello world"));

    asyncRestTemplate.setMessageConverters(converters);
    input.setName("中国");
    listenableFuture = asyncRestTemplate.postForEntity(controllerUrl + "saysomething?prefix={prefix}",
        jsonRequest(input),
        String.class,
        "hello");
    futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(futureResponse.getBody(), is("hello 中国"));
    asyncRestTemplate.setMessageConverters(convertersOld);
  }

  @Test
  public void ensureServerWorksFine() throws Exception {
    String result = restTemplate.getForObject(
        controllerUrl + "sayhi?name=world",
        String.class);

    MatcherAssert.assertThat(jsonOf(result, String.class), is("hi world [world]"));
    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate
        .getForEntity(controllerUrl + "sayhi?name=world",
            String.class);
    ResponseEntity<String> futureResponse = listenableFuture.get();
    MatcherAssert.assertThat(jsonOf(futureResponse.getBody(), String.class), is("hi world [world]"));
  }

  @Test
  public void ensureServerBlowsUp() {
    try {
      restTemplate
          .getForEntity(controllerUrl + "sayhi?name=throwexception", String.class);
      Assertions.fail();
    } catch (HttpServerErrorException e) {
      MatcherAssert.assertThat(e.getRawStatusCode(), is(500));
      MatcherAssert.assertThat(e.getResponseBodyAsString(),
          Matchers.isOneOf(
              "{\"message\":\"Unexpected exception when processing the request.\"}",
              "{\"code\":\"SCB.50000000\",\"message\":\"Unexpected exception when processing.\"}"));
    }
  }

  @Test
  public void ableToSetCustomHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("name", "world");

    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> result = restTemplate.exchange(
        controllerUrl + "sayhei",
        GET,
        requestEntity,
        String.class);

    MatcherAssert.assertThat(jsonOf(result.getBody(), String.class), is("hei world"));

    ListenableFuture<ResponseEntity<String>> listenableFuture = asyncRestTemplate.exchange(controllerUrl + "sayhei",
        GET,
        requestEntity,
        String.class);
//    ResponseEntity<String> responseEntity = listenableFuture.get();
    listenableFuture.addCallback(
        new ListenableFutureCallback<ResponseEntity<String>>() {
          @Override
          public void onFailure(Throwable ex) {
          }

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            MatcherAssert.assertThat(jsonOf(result.getBody(), String.class), is("hei world"));
          }
        }
    );
  }

  private <T> HttpEntity<T> jsonRequest(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  private <T> T jsonOf(String json, Class<T> aClass) {
    try {
      return RestObjectMapperFactory.getRestObjectMapper().readValue(json, aClass);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to read JSON from " + json + ", Exception is: " + e);
    }
  }

  private File newFile(String fileContent) throws IOException {
    Path file = folder;
    Path tempFile = Files.createFile(file.resolve("scb-" + UUID.randomUUID()));
    Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
    return tempFile.toFile();
  }
}
