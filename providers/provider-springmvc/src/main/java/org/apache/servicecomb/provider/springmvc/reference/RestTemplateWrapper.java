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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

/**
 * 用于同时支持cse调用和非cse调用
 */
// TODO: 2017/7/1 what we want to expose is RestOperations instead, since some RestTemplate methods are not to be called by users
class RestTemplateWrapper extends RestTemplate {
  private final List<AcceptableRestTemplate> acceptableRestTemplates = new ArrayList<>();

  final RestTemplate defaultRestTemplate = new RestTemplate();

  RestTemplateWrapper() {
    acceptableRestTemplates.add(new CseRestTemplate());
  }

  void addAcceptableRestTemplate(int index, AcceptableRestTemplate restTemplate) {
    acceptableRestTemplates.add(index, restTemplate);
  }

  RestTemplate getRestTemplate(String url) {
    for (AcceptableRestTemplate template : acceptableRestTemplates) {
      if (template.isAcceptable(url)) {
        return template;
      }
    }
    return defaultRestTemplate;
  }

  RestTemplate getRestTemplate(URI uri) {
    for (AcceptableRestTemplate template : acceptableRestTemplates) {
      if (template.isAcceptable(uri)) {
        return template;
      }
    }
    return defaultRestTemplate;
  }

  @Override
  public <T> T getForObject(String url, Class<T> responseType, Object... urlVariables) throws RestClientException {
    return getRestTemplate(url).getForObject(url, responseType, urlVariables);
  }

  @Override
  public <T> T getForObject(String url, Class<T> responseType,
      Map<String, ?> urlVariables) throws RestClientException {
    return getRestTemplate(url).getForObject(url, responseType, urlVariables);
  }

  @Override
  public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
    return getRestTemplate(url).getForObject(url, responseType);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType,
      Object... urlVariables) throws RestClientException {
    return getRestTemplate(url).getForEntity(url, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType,
      Map<String, ?> urlVariables) throws RestClientException {
    return getRestTemplate(url).getForEntity(url, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
    return getRestTemplate(url).getForEntity(url, responseType);
  }

  @Override
  public <T> T postForObject(String url, Object request, Class<T> responseType,
      Object... uriVariables) throws RestClientException {
    return getRestTemplate(url).postForObject(url, request, responseType, uriVariables);
  }

  @Override
  public <T> T postForObject(String url, Object request, Class<T> responseType,
      Map<String, ?> uriVariables) throws RestClientException {
    return getRestTemplate(url).postForObject(url, request, responseType, uriVariables);
  }

  @Override
  public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
    return getRestTemplate(url).postForObject(url, request, responseType);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
      Object... uriVariables) throws RestClientException {
    return getRestTemplate(url).postForEntity(url, request, responseType, uriVariables);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
      Map<String, ?> uriVariables) throws RestClientException {
    return getRestTemplate(url).postForEntity(url, request, responseType, uriVariables);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity(URI url, Object request,
      Class<T> responseType) throws RestClientException {
    return getRestTemplate(url).postForEntity(url, request, responseType);
  }

  @Override
  public void put(String url, Object request, Object... urlVariables) throws RestClientException {
    getRestTemplate(url).put(url, request, urlVariables);
  }

  @Override
  public void put(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {
    getRestTemplate(url).put(url, request, urlVariables);
  }

  @Override
  public void put(URI url, Object request) throws RestClientException {
    getRestTemplate(url).put(url, request);
  }

  @Override
  public void delete(String url, Object... urlVariables) throws RestClientException {
    getRestTemplate(url).delete(url, urlVariables);
  }

  @Override
  public void delete(String url, Map<String, ?> urlVariables) throws RestClientException {
    getRestTemplate(url).delete(url, urlVariables);
  }

  @Override
  public void delete(URI url) throws RestClientException {
    getRestTemplate(url).delete(url);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
      Class<T> responseType, Object... uriVariables) throws RestClientException {
    return getRestTemplate(url).exchange(url, method, requestEntity, responseType, uriVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
      ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
    return getRestTemplate(url).exchange(url, method, requestEntity, responseType, uriVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
      Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
    return getRestTemplate(url).exchange(url, method, requestEntity, responseType, uriVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
      ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
    return getRestTemplate(url).exchange(url, method, requestEntity, responseType, uriVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
      Class<T> responseType) throws RestClientException {
    return getRestTemplate(requestEntity.getUrl()).exchange(requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
      ParameterizedTypeReference<T> responseType) throws RestClientException {
    return getRestTemplate(requestEntity.getUrl()).exchange(requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
      Class<T> responseType) throws RestClientException {
    return getRestTemplate(url).exchange(url, method, requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
      ParameterizedTypeReference<T> responseType) throws RestClientException {
    return getRestTemplate(url).exchange(url, method, requestEntity, responseType);
  }

  @Override
  public HttpHeaders headForHeaders(String url, Object... urlVariables) throws RestClientException {
    return getRestTemplate(url).headForHeaders(url, urlVariables);
  }

  @Override
  public HttpHeaders headForHeaders(String url, Map<String, ?> urlVariables) throws RestClientException {
    return getRestTemplate(url).headForHeaders(url, urlVariables);
  }

  @Override
  public HttpHeaders headForHeaders(URI url) throws RestClientException {
    return getRestTemplate(url).headForHeaders(url);
  }

  @Override
  public URI postForLocation(String url, Object request, Object... urlVariables) throws RestClientException {
    return getRestTemplate(url).postForLocation(url, request, urlVariables);
  }

  @Override
  public URI postForLocation(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {
    return getRestTemplate(url).postForLocation(url, request, urlVariables);
  }

  @Override
  public URI postForLocation(URI url, Object request) throws RestClientException {
    return getRestTemplate(url).postForLocation(url, request);
  }

  @Override
  public Set<HttpMethod> optionsForAllow(String url, Object... urlVariables) throws RestClientException {
    return getRestTemplate(url).optionsForAllow(url, urlVariables);
  }

  @Override
  public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> urlVariables) throws RestClientException {
    return getRestTemplate(url).optionsForAllow(url, urlVariables);
  }

  @Override
  public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
    return getRestTemplate(url).optionsForAllow(url);
  }

  @Override
  public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
      ResponseExtractor<T> responseExtractor, Object... urlVariables) throws RestClientException {
    return getRestTemplate(url).execute(url, method, requestCallback, responseExtractor, urlVariables);
  }

  @Override
  public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
      ResponseExtractor<T> responseExtractor, Map<String, ?> urlVariables) throws RestClientException {
    return getRestTemplate(url).execute(url, method, requestCallback, responseExtractor, urlVariables);
  }

  @Override
  public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback,
      ResponseExtractor<T> responseExtractor) throws RestClientException {
    return getRestTemplate(url).execute(url, method, requestCallback, responseExtractor);
  }

  @Override
  public void setInterceptors(List<ClientHttpRequestInterceptor> interceptors) {
    super.setInterceptors(interceptors);
    defaultRestTemplate.setInterceptors(interceptors);
  }

  @Override
  public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
    super.setRequestFactory(requestFactory);
    defaultRestTemplate.setRequestFactory(requestFactory);
  }

  @Override
  public void setErrorHandler(ResponseErrorHandler errorHandler) {
    super.setErrorHandler(errorHandler);
    acceptableRestTemplates.forEach(template -> template.setErrorHandler(errorHandler));
    defaultRestTemplate.setErrorHandler(errorHandler);
  }

  @Override
  public void setDefaultUriVariables(Map<String, ?> defaultUriVariables) {
    super.setDefaultUriVariables(defaultUriVariables);
    acceptableRestTemplates.forEach(template -> template.setDefaultUriVariables(defaultUriVariables));
    defaultRestTemplate.setDefaultUriVariables(defaultUriVariables);
  }

  @Override
  public void setUriTemplateHandler(UriTemplateHandler handler) {
    super.setUriTemplateHandler(handler);
    defaultRestTemplate.setUriTemplateHandler(handler);
  }

  @Override
  public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
    super.setMessageConverters(messageConverters);
    defaultRestTemplate.setMessageConverters(messageConverters);
  }
}
