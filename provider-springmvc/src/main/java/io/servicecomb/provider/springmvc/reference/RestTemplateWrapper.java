/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.reference;

import java.net.URI;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.common.rest.RestConst;

/**
 * 用于同时支持cse调用和非cse调用
 */
public class RestTemplateWrapper extends RestTemplate {
    private static RestTemplate cseRestTemplate = new CseRestTemplate();

    private boolean isCse(String url) {
        return url.startsWith(RestConst.URI_PREFIX);
    }

    private boolean isCse(URI uri) {
        return RestConst.SCHEME.equals(uri.getScheme());
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Object... urlVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.getForObject(url, responseType, urlVariables);
        }

        return super.getForObject(url, responseType, urlVariables);
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType,
            Map<String, ?> urlVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.getForObject(url, responseType, urlVariables);
        }

        return super.getForObject(url, responseType, urlVariables);
    }

    @Override
    public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.getForObject(url, responseType);
        }

        return super.getForObject(url, responseType);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType,
            Object... urlVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.getForEntity(url, responseType, urlVariables);
        }

        return super.getForEntity(url, responseType, urlVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType,
            Map<String, ?> urlVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.getForEntity(url, responseType, urlVariables);
        }

        return super.getForEntity(url, responseType, urlVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.getForEntity(url, responseType);
        }

        return super.getForEntity(url, responseType);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType,
            Object... uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.postForObject(url, request, responseType, uriVariables);
        }

        return super.postForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType,
            Map<String, ?> uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.postForObject(url, request, responseType, uriVariables);
        }

        return super.postForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.postForObject(url, request, responseType);
        }

        return super.postForObject(url, request, responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
            Object... uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.postForEntity(url, request, responseType, uriVariables);
        }

        return super.postForEntity(url, request, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
            Map<String, ?> uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.postForEntity(url, request, responseType, uriVariables);
        }

        return super.postForEntity(url, request, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(URI url, Object request,
            Class<T> responseType) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.postForEntity(url, request, responseType);
        }

        return super.postForEntity(url, request, responseType);
    }

    @Override
    public void put(String url, Object request, Object... urlVariables) throws RestClientException {
        if (isCse(url)) {
            cseRestTemplate.put(url, request, urlVariables);
            return;
        }

        super.put(url, request, urlVariables);
    }

    @Override
    public void put(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {
        if (isCse(url)) {
            cseRestTemplate.put(url, request, urlVariables);
            return;
        }

        super.put(url, request, urlVariables);
    }

    @Override
    public void put(URI url, Object request) throws RestClientException {
        if (isCse(url)) {
            cseRestTemplate.put(url, request);
            return;
        }

        super.put(url, request);
    }

    @Override
    public void delete(String url, Object... urlVariables) throws RestClientException {
        if (isCse(url)) {
            cseRestTemplate.delete(url, urlVariables);
            return;
        }

        super.delete(url, urlVariables);
    }

    @Override
    public void delete(String url, Map<String, ?> urlVariables) throws RestClientException {
        if (isCse(url)) {
            cseRestTemplate.delete(url, urlVariables);
            return;
        }

        super.delete(url, urlVariables);
    }

    @Override
    public void delete(URI url) throws RestClientException {
        if (isCse(url)) {
            cseRestTemplate.delete(url);
            return;
        }

        super.delete(url);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
            Class<T> responseType, Object... uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        }

        return super.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
            ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        }

        return super.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
            Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        }

        return super.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
            ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        }

        return super.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
            Class<T> responseType) throws RestClientException {
        if (isCse(requestEntity.getUrl())) {
            return cseRestTemplate.exchange(requestEntity, responseType);
        }

        return super.exchange(requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
            ParameterizedTypeReference<T> responseType) throws RestClientException {
        if (isCse(requestEntity.getUrl())) {
            return cseRestTemplate.exchange(requestEntity, responseType);
        }

        return super.exchange(requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
            Class<T> responseType) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.exchange(url, method, requestEntity, responseType);
        }

        return super.exchange(url, method, requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
            ParameterizedTypeReference<T> responseType) throws RestClientException {
        if (isCse(url)) {
            return cseRestTemplate.exchange(url, method, requestEntity, responseType);
        }

        return super.exchange(url, method, requestEntity, responseType);
    }
}
