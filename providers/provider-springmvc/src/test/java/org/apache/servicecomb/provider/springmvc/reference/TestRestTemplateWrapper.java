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

package org.apache.servicecomb.provider.springmvc.reference;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.TRACE;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriTemplateHandler;

public class TestRestTemplateWrapper {

  private final AcceptableRestTemplate underlying = mock(AcceptableRestTemplate.class);

  private final RestTemplateWrapper wrapper = new RestTemplateWrapper();

  private final String url = uniquify("someUrl");

  private final URI uri = URI.create(url);

  private final String param1 = uniquify("param1");

  private final String param2 = uniquify("param2");

  @SuppressWarnings("serial")
  private final Map<String, String> paramsMap = new HashMap<String, String>() {
    {
      put(uniquify("key1"), param1);
      put(uniquify("key2"), param2);
    }
  };

  private final HttpEntity<String> requestEntity = new HttpEntity<>(uniquify("requestBody"));

  private final String response = uniquify("response");

  private final ResponseEntity<List<String>> typedResponse = new ResponseEntity<>(singletonList(response), OK);

  private final ResponseEntity<String> responseEntity = new ResponseEntity<>(response, OK);

  private final List<HttpMethod> httpMethods = asList(GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE);

  @Before
  public void setUp() throws Exception {
    when(underlying.isAcceptable(url)).thenReturn(true);
    when(underlying.isAcceptable(uri)).thenReturn(true);
    wrapper.addAcceptableRestTemplate(1, underlying);
  }

  @Test
  public void headForHeadersWithUnderlyingRestTemplate() {
    HttpHeaders expected = new HttpHeaders();
    HttpHeaders actual;

    when(underlying.headForHeaders(url, param1, param2)).thenReturn(expected);
    actual = wrapper.headForHeaders(url, param1, param2);
    assertThat(actual, is(expected));
    verify(underlying).headForHeaders(url, param1, param2);

    when(underlying.headForHeaders(url, paramsMap)).thenReturn(expected);
    actual = wrapper.headForHeaders(url, paramsMap);
    assertThat(actual, is(expected));
    verify(underlying).headForHeaders(url, paramsMap);

    when(underlying.headForHeaders(uri)).thenReturn(expected);
    actual = wrapper.headForHeaders(uri);
    assertThat(actual, is(expected));
    verify(underlying).headForHeaders(uri);
  }

  @Test
  public void optionsForAllowWithUnderlyingRestTemplate() {
    Set<HttpMethod> expected = new HashSet<>(this.httpMethods);
    Set<HttpMethod> actual;

    when(underlying.optionsForAllow(url, param1, param2)).thenReturn(expected);
    actual = wrapper.optionsForAllow(url, param1, param2);
    assertThat(actual, is(expected));
    verify(underlying).optionsForAllow(url, param1, param2);

    when(underlying.optionsForAllow(url, paramsMap)).thenReturn(expected);
    actual = wrapper.optionsForAllow(url, paramsMap);
    assertThat(actual, is(expected));
    verify(underlying).optionsForAllow(url, paramsMap);

    when(underlying.optionsForAllow(uri)).thenReturn(expected);
    actual = wrapper.optionsForAllow(uri);
    assertThat(actual, is(expected));
    verify(underlying).optionsForAllow(uri);
  }

  @Test
  public void getForObjectWithUnderlyingRestTemplate() {
    String actual;

    when(underlying.getForObject(url, String.class, param1, param2)).thenReturn(response);
    actual = wrapper.getForObject(url, String.class, param1, param2);
    assertThat(actual, is(response));
    verify(underlying).getForObject(url, String.class, param1, param2);

    when(underlying.getForObject(url, String.class, paramsMap)).thenReturn(response);
    actual = wrapper.getForObject(url, String.class, paramsMap);
    assertThat(actual, is(response));
    verify(underlying).getForObject(url, String.class, paramsMap);

    when(underlying.getForObject(uri, String.class)).thenReturn(response);
    actual = wrapper.getForObject(uri, String.class);
    assertThat(actual, is(response));
    verify(underlying).getForObject(uri, String.class);
  }

  @Test
  public void getForEntityWithUnderlyingRestTemplate() {
    ResponseEntity<String> actual;

    when(underlying.getForEntity(url, String.class, param1, param2)).thenReturn(responseEntity);
    actual = wrapper.getForEntity(url, String.class, param1, param2);
    assertThat(actual, is(responseEntity));
    verify(underlying).getForEntity(url, String.class, param1, param2);

    when(underlying.getForEntity(url, String.class, paramsMap)).thenReturn(responseEntity);
    actual = wrapper.getForEntity(url, String.class, paramsMap);
    assertThat(actual, is(responseEntity));
    verify(underlying).getForEntity(url, String.class, paramsMap);

    when(underlying.getForEntity(uri, String.class)).thenReturn(responseEntity);
    actual = wrapper.getForEntity(uri, String.class);
    assertThat(actual, is(responseEntity));
    verify(underlying).getForEntity(uri, String.class);
  }

  @Test
  public void postForObjectWithUnderlyingRestTemplate() {
    String actual;

    when(underlying.postForObject(url, requestEntity, String.class, param1, param2)).thenReturn(response);
    actual = wrapper.postForObject(url, requestEntity, String.class, param1, param2);
    assertThat(actual, is(response));
    verify(underlying).postForObject(url, requestEntity, String.class, param1, param2);

    when(underlying.postForObject(url, requestEntity, String.class, paramsMap)).thenReturn(response);
    actual = wrapper.postForObject(url, requestEntity, String.class, paramsMap);
    assertThat(actual, is(response));
    verify(underlying).postForObject(url, requestEntity, String.class, paramsMap);

    when(underlying.postForObject(uri, requestEntity, String.class)).thenReturn(response);
    actual = wrapper.postForObject(uri, requestEntity, String.class);
    assertThat(actual, is(response));
    verify(underlying).postForObject(uri, requestEntity, String.class);
  }

  @Test
  public void postForEntityWithUnderlyingRestTemplate() {
    ResponseEntity<String> actual;

    when(underlying.postForEntity(url, requestEntity, String.class, param1, param2)).thenReturn(responseEntity);
    actual = wrapper.postForEntity(url, requestEntity, String.class, param1, param2);
    assertThat(actual, is(responseEntity));
    verify(underlying).postForEntity(url, requestEntity, String.class, param1, param2);

    when(underlying.postForEntity(url, requestEntity, String.class, paramsMap)).thenReturn(responseEntity);
    actual = wrapper.postForEntity(url, requestEntity, String.class, paramsMap);
    assertThat(actual, is(responseEntity));
    verify(underlying).postForEntity(url, requestEntity, String.class, paramsMap);

    when(underlying.postForEntity(uri, requestEntity, String.class)).thenReturn(responseEntity);
    actual = wrapper.postForEntity(uri, requestEntity, String.class);
    assertThat(actual, is(responseEntity));
    verify(underlying).postForEntity(uri, requestEntity, String.class);
  }

  @Test
  public void postForLocationWithUnderlyingRestTemplate() {
    URI actual;

    when(underlying.postForLocation(url, requestEntity, param1, param2)).thenReturn(uri);
    actual = wrapper.postForLocation(url, requestEntity, param1, param2);
    assertThat(actual, is(uri));
    verify(underlying).postForLocation(url, requestEntity, param1, param2);

    when(underlying.postForLocation(url, requestEntity, paramsMap)).thenReturn(uri);
    actual = wrapper.postForLocation(url, requestEntity, paramsMap);
    assertThat(actual, is(uri));
    verify(underlying).postForLocation(url, requestEntity, paramsMap);

    when(underlying.postForLocation(uri, requestEntity)).thenReturn(uri);
    actual = wrapper.postForLocation(uri, requestEntity);
    assertThat(actual, is(uri));
    verify(underlying).postForLocation(uri, requestEntity);
  }

  @Test
  public void executeWithUnderlyingRestTemplate() {
    RequestCallback requestCallback = clientHttpRequest -> {
    };
    ResponseExtractor<ResponseEntity<String>> responseExtractor = clientHttpResponse -> responseEntity;

    ResponseEntity<String> actual;

    for (HttpMethod method : httpMethods) {
      when(underlying.execute(url, method, requestCallback, responseExtractor, param1, param2))
          .thenReturn(responseEntity);
      actual = wrapper.execute(url, method, requestCallback, responseExtractor, param1, param2);
      assertThat(actual, is(responseEntity));
      verify(underlying).execute(url, method, requestCallback, responseExtractor, param1, param2);

      when(underlying.execute(url, method, requestCallback, responseExtractor, paramsMap)).thenReturn(responseEntity);
      actual = wrapper.execute(url, method, requestCallback, responseExtractor, paramsMap);
      assertThat(actual, is(responseEntity));
      verify(underlying).execute(url, method, requestCallback, responseExtractor, paramsMap);

      when(underlying.execute(uri, method, requestCallback, responseExtractor)).thenReturn(responseEntity);
      actual = wrapper.execute(uri, method, requestCallback, responseExtractor);
      assertThat(actual, is(responseEntity));
      verify(underlying).execute(uri, method, requestCallback, responseExtractor);
    }
  }

  @Test
  public void exchangeWithUnderlyingRestTemplate() {
    ResponseEntity<String> actual;

    for (HttpMethod method : httpMethods) {
      when(underlying.exchange(url, method, requestEntity, String.class, param1, param2)).thenReturn(responseEntity);
      actual = wrapper.exchange(url, method, requestEntity, String.class, param1, param2);
      assertThat(actual, is(responseEntity));
      verify(underlying).exchange(url, method, requestEntity, String.class, param1, param2);

      when(underlying.exchange(url, method, requestEntity, String.class, paramsMap)).thenReturn(responseEntity);
      actual = wrapper.exchange(url, method, requestEntity, String.class, paramsMap);
      assertThat(actual, is(responseEntity));
      verify(underlying).exchange(url, method, requestEntity, String.class, paramsMap);

      when(underlying.exchange(uri, method, requestEntity, String.class)).thenReturn(responseEntity);
      actual = wrapper.exchange(uri, method, requestEntity, String.class);
      assertThat(actual, is(responseEntity));
      verify(underlying).exchange(uri, method, requestEntity, String.class);

      RequestEntity<String> request = new RequestEntity<>(method, uri);
      when(underlying.exchange(request, String.class)).thenReturn(responseEntity);
      actual = wrapper.exchange(request, String.class);
      assertThat(actual, is(responseEntity));
      verify(underlying).exchange(request, String.class);
    }
  }

  @Test
  public void exchangeUsingParameterizedTypeWithUnderlyingRestTemplate() {
    ParameterizedTypeReference<List<String>> typeReference = new ParameterizedTypeReference<List<String>>() {
    };
    ResponseEntity<List<String>> actual;

    for (HttpMethod method : httpMethods) {
      when(underlying.exchange(url, method, requestEntity, typeReference, param1, param2)).thenReturn(typedResponse);
      actual = wrapper.exchange(url, method, requestEntity, typeReference, param1, param2);
      assertThat(actual, is(typedResponse));
      verify(underlying).exchange(url, method, requestEntity, typeReference, param1, param2);

      when(underlying.exchange(url, method, requestEntity, typeReference, paramsMap)).thenReturn(typedResponse);
      actual = wrapper.exchange(url, method, requestEntity, typeReference, paramsMap);
      assertThat(actual, is(typedResponse));
      verify(underlying).exchange(url, method, requestEntity, typeReference, paramsMap);

      when(underlying.exchange(uri, method, requestEntity, typeReference)).thenReturn(typedResponse);
      actual = wrapper.exchange(uri, method, requestEntity, typeReference);
      assertThat(actual, is(typedResponse));
      verify(underlying).exchange(uri, method, requestEntity, typeReference);

      RequestEntity<String> request = new RequestEntity<>(method, uri);
      when(underlying.exchange(request, typeReference)).thenReturn(typedResponse);
      actual = wrapper.exchange(request, typeReference);
      assertThat(actual, is(typedResponse));
      verify(underlying).exchange(request, typeReference);
    }
  }

  @Test
  public void putWithUnderlyingRestTemplate() {
    wrapper.put(url, requestEntity, param1, param2);
    verify(underlying).put(url, requestEntity, param1, param2);

    wrapper.put(url, requestEntity, paramsMap);
    verify(underlying).put(url, requestEntity, paramsMap);

    wrapper.put(uri, requestEntity);
    verify(underlying).put(uri, requestEntity);
  }

  @Test
  public void deleteWithUnderlyingRestTemplate() {
    wrapper.delete(url, param1, param2);
    verify(underlying).delete(url, param1, param2);

    wrapper.delete(url, paramsMap);
    verify(underlying).delete(url, paramsMap);

    wrapper.delete(uri);
    verify(underlying).delete(uri);
  }

  @Test
  public void setInterceptorsWithUnderlying() {
    ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
    ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
    List<ClientHttpRequestInterceptor> interceptors = asList(interceptor1, interceptor2);

    wrapper.setInterceptors(interceptors);

    assertThat(wrapper.getInterceptors(), contains(interceptor1, interceptor2));
    assertThat(wrapper.defaultRestTemplate.getInterceptors(), contains(interceptor1, interceptor2));
    verify(underlying, never()).setInterceptors(interceptors);
  }

  @Test
  public void doNotSetRequestFactoryWithUnderlying() {
    ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);

    wrapper.setRequestFactory(requestFactory);

    assertThat(wrapper.getRequestFactory(), is(requestFactory));
    assertThat(wrapper.defaultRestTemplate.getRequestFactory(), is(requestFactory));

    verify(underlying, never()).setRequestFactory(requestFactory);
  }

  @Test
  public void setErrorHandlerWithUnderlying() {
    ResponseErrorHandler errorHandler = mock(ResponseErrorHandler.class);

    wrapper.setErrorHandler(errorHandler);

    assertThat(wrapper.getErrorHandler(), is(errorHandler));
    assertThat(wrapper.defaultRestTemplate.getErrorHandler(), is(errorHandler));

    verify(underlying).setErrorHandler(errorHandler);
  }

  @Test
  public void setDefaultUriVariablesWithUnderlying() {
    Map<String, Object> uriVariables = new HashMap<>();

    wrapper.setDefaultUriVariables(uriVariables);

    assertThat(defaultUriVariablesOf(wrapper), is(uriVariables));
    assertThat(defaultUriVariablesOf(wrapper.defaultRestTemplate), is(uriVariables));

    verify(underlying).setDefaultUriVariables(uriVariables);
  }

  @Test
  public void dotNotSetUriTemplateHandlerWithUnderlying() {
    UriTemplateHandler uriTemplateHandler = mock(UriTemplateHandler.class);

    wrapper.setUriTemplateHandler(uriTemplateHandler);

    assertThat(wrapper.getUriTemplateHandler(), is(uriTemplateHandler));
    assertThat(wrapper.defaultRestTemplate.getUriTemplateHandler(), is(uriTemplateHandler));

    verify(underlying, never()).setUriTemplateHandler(uriTemplateHandler);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void setMessageConvertersWithUnderlying() {
    ByteArrayHttpMessageConverter messageConverter = mock(ByteArrayHttpMessageConverter.class);

    wrapper.setMessageConverters(singletonList(messageConverter));

    assertThat(wrapper.getMessageConverters(), contains(messageConverter));
    assertThat(wrapper.defaultRestTemplate.getMessageConverters(), contains(messageConverter));

    verify(underlying, never()).setMessageConverters(singletonList(messageConverter));
  }

  @Test
  public void getsAcceptableRestTemplate() {
    assertThat(wrapper.getRestTemplate(uri), is(underlying));
    assertThat(wrapper.getRestTemplate(url), is(underlying));
  }

  @Test
  public void getsDefaultRestTemplate() {
    reset(underlying);
    assertThat(wrapper.getRestTemplate(uri), is(wrapper.defaultRestTemplate));
    assertThat(wrapper.getRestTemplate(url), is(wrapper.defaultRestTemplate));
  }

  private Map<String, ?> defaultUriVariablesOf(RestTemplate wrapper1) {
    return ((DefaultUriTemplateHandler) wrapper1.getUriTemplateHandler()).getDefaultUriVariables();
  }
}
