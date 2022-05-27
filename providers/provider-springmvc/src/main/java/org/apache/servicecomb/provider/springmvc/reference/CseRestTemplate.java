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

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

public class CseRestTemplate extends AcceptableRestTemplate {
  public CseRestTemplate() {
    setMessageConverters(Arrays.asList(new CseHttpMessageConverter()));
    setRequestFactory(new CseClientHttpRequestFactory());
    setUriTemplateHandler(new CseUriTemplateHandler());
  }

  // GET

  @Override
  @Nullable
  public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
    RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
  }

  @Override
  @Nullable
  public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
    RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
  }

  @Override
  @Nullable
  public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
    RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.GET, requestCallback, responseExtractor);
  }

  // HEAD
  // no override

  // POST

  @Override
  @Nullable
  public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType,
      Object... uriVariables) throws RestClientException {

    RequestCallback requestCallback = httpEntityCallback(request, responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
  }

  @Override
  @Nullable
  public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType,
      Map<String, ?> uriVariables) throws RestClientException {

    RequestCallback requestCallback = httpEntityCallback(request, responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
  }

  @Override
  @Nullable
  public <T> T postForObject(URI url, @Nullable Object request, Class<T> responseType)
      throws RestClientException {

    RequestCallback requestCallback = httpEntityCallback(request, responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.POST, requestCallback, responseExtractor);
  }

  // PUT
  // no override

  // PATCH

  @Override
  @Nullable
  public <T> T patchForObject(String url, @Nullable Object request, Class<T> responseType,
      Object... uriVariables) throws RestClientException {

    RequestCallback requestCallback = httpEntityCallback(request, responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
  }

  @Override
  @Nullable
  public <T> T patchForObject(String url, @Nullable Object request, Class<T> responseType,
      Map<String, ?> uriVariables) throws RestClientException {

    RequestCallback requestCallback = httpEntityCallback(request, responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
  }

  @Override
  @Nullable
  public <T> T patchForObject(URI url, @Nullable Object request, Class<T> responseType)
      throws RestClientException {

    RequestCallback requestCallback = httpEntityCallback(request, responseType);
    CseHttpMessageConverterExtractor<T> responseExtractor =
        new CseHttpMessageConverterExtractor<>();
    return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor);
  }

  // DELETE
  // no override

  // OPTIONS
  // no override

  // exchange
  // no override

  @Override
  public <T> ResponseExtractor<ResponseEntity<T>> responseEntityExtractor(Type responseType) {
    return new CseResponseEntityResponseExtractor<>(responseType);
  }

  @Override
  public <T> RequestCallback httpEntityCallback(Object requestBody) {
    RequestCallback callback = super.httpEntityCallback(requestBody);
    CseRequestCallback cseCallback = new CseRequestCallback(requestBody, callback, null);
    return cseCallback;
  }

  @Override
  public <T> RequestCallback httpEntityCallback(Object requestBody, Type responseType) {
    RequestCallback callback = super.httpEntityCallback(requestBody, responseType);
    CseRequestCallback cseCallback = new CseRequestCallback(requestBody, callback, responseType);
    return cseCallback;
  }

  @Override
  public boolean isAcceptable(String uri) {
    return uri.startsWith(RestConst.URI_PREFIX) || uri.startsWith(RestConst.URI_PREFIX_NEW);
  }

  @Override
  public boolean isAcceptable(URI uri) {
    return RestConst.SCHEME.equals(uri.getScheme()) || RestConst.SCHEME_NEW.equals(uri.getScheme());
  }
}
