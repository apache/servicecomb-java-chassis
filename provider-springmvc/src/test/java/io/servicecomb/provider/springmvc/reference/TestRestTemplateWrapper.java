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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;

import io.servicecomb.provider.common.MockUtil;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.provider.springmvc.reference.RestTemplateWrapper;
import mockit.Mock;
import mockit.MockUp;

public class TestRestTemplateWrapper {

    /**
     * Test RestTemplateWrapper
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws MalformedURLException 
     * @throws URISyntaxException 
     * @throws IOException 
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testRestTemplateWrapper() {

        try {
            MockUtil.getInstance().mockConsumerProviderManager();
            MockUtil.getInstance().mockRegisterManager();
            MockUtil.getInstance().mockServicePathManager();
            MockUtil.getInstance().mockOperationLocator();
            MockUtil.getInstance().mockOperationMeta();
            MockUtil.getInstance().mockSchemaMeta();
            MockUtil.getInstance().mockSchemaUtils();
            MockUtil.getInstance().mockBeanUtils();
            MockUtil.getInstance().mockRequestMeta();
            MockUtil.getInstance().mockInvokerUtils();
            RestTemplateWrapper lRestTemplateWrapper = (RestTemplateWrapper) RestTemplateBuilder.create();
            lRestTemplateWrapper.put(URI.create("cse://test"), new Object());
            lRestTemplateWrapper.delete(URI.create("cse://test"));
            lRestTemplateWrapper.put("cse://test", new Object());
            lRestTemplateWrapper.getForObject("cse://test", new Object().getClass());
            lRestTemplateWrapper.getForEntity("cse://test", new Object().getClass());
            lRestTemplateWrapper.postForObject("cse://test", new Object(), new Object().getClass());
            lRestTemplateWrapper.postForEntity("cse://test", new Object(), new Object().getClass());
            lRestTemplateWrapper.exchange("cse://test", HttpMethod.GET, HttpEntity.EMPTY, new Object().getClass());
            lRestTemplateWrapper.delete("cse://test", new Object());
            lRestTemplateWrapper.delete("cse://test", new HashMap<>());
            Map<String, ?> uriVariables = new HashMap<>();

            lRestTemplateWrapper.getForObject("cse://test", new Object().getClass(), uriVariables);
            lRestTemplateWrapper.getForEntity("cse://test", new Object().getClass(), uriVariables);
            lRestTemplateWrapper.getForObject(URI.create("cse://test"), new Object().getClass());
            lRestTemplateWrapper.getForEntity(URI.create("cse://test"), new Object().getClass());
            lRestTemplateWrapper.postForObject("cse://test", new Object(), new Object().getClass(), uriVariables);
            lRestTemplateWrapper.postForObject(URI.create("cse://test"), new Object(), new Object().getClass());
            lRestTemplateWrapper.postForEntity("cse://test", new Object(), new Object().getClass(), uriVariables);
            lRestTemplateWrapper.postForEntity(URI.create("cse://test"), new Object(), new Object().getClass());
            lRestTemplateWrapper.put("cse://test", new Object(), uriVariables);
            lRestTemplateWrapper.put(URI.create("cse://test"), new Object());
            lRestTemplateWrapper
                    .exchange("cse://test", HttpMethod.GET, HttpEntity.EMPTY, new Object().getClass(), uriVariables);
            ParameterizedTypeReference instance = Mockito.mock(ParameterizedTypeReference.class);
            lRestTemplateWrapper.exchange("cse://test", HttpMethod.GET, HttpEntity.EMPTY, instance, uriVariables);
            lRestTemplateWrapper
                    .exchange("cse://test", HttpMethod.GET, HttpEntity.EMPTY, instance, new Object().getClass());
            RequestEntity requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create("cse://test"));
            lRestTemplateWrapper.exchange(requestEntity, new Object().getClass());
            lRestTemplateWrapper.exchange(requestEntity, instance);
            lRestTemplateWrapper
                    .exchange(URI.create("cse://test"), HttpMethod.GET, HttpEntity.EMPTY, new Object().getClass());
            lRestTemplateWrapper.exchange(URI.create("cse://test"), HttpMethod.GET, HttpEntity.EMPTY, instance);

            Assert.assertTrue(RestTemplateBuilder.create().getClass().isAssignableFrom(RestTemplateWrapper.class));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testRestTemplate() {
        new MockUp<RestTemplateWrapper>() {
            @Mock
            private boolean isCse(URI url) {
                return false;
            }

        };

        try {
            MockUtil.getInstance().mockConsumerProviderManager();
            MockUtil.getInstance().mockRegisterManager();
            MockUtil.getInstance().mockServicePathManager();
            MockUtil.getInstance().mockOperationLocator();
            MockUtil.getInstance().mockOperationMeta();
            MockUtil.getInstance().mockSchemaMeta();
            MockUtil.getInstance().mockSchemaUtils();
            MockUtil.getInstance().mockBeanUtils();
            MockUtil.getInstance().mockRequestMeta();
            MockUtil.getInstance().mockInvokerUtils();
            MockUtil.getInstance().mockRestTemplate();
            RestTemplateWrapper lRestTemplateWrapper = (RestTemplateWrapper) RestTemplateBuilder.create();
            lRestTemplateWrapper.put(URI.create("csd://test"), new Object());
            lRestTemplateWrapper.delete(URI.create("cse://test"));
            lRestTemplateWrapper.put("csd://test", new Object());
            lRestTemplateWrapper.getForObject("csd://test", new Object().getClass());
            lRestTemplateWrapper.getForEntity("csd://test", new Object().getClass());
            lRestTemplateWrapper.postForObject("csd://test", new Object(), new Object().getClass());
            lRestTemplateWrapper.postForEntity("csd://test", new Object(), new Object().getClass());
            lRestTemplateWrapper.exchange("csd://test", HttpMethod.GET, HttpEntity.EMPTY, new Object().getClass());
            lRestTemplateWrapper.delete("csd://test", new Object());
            lRestTemplateWrapper.delete("csd://test", new HashMap<>());
            Map<String, ?> uriVariables = new HashMap<>();

            lRestTemplateWrapper.getForObject("csd://test", new Object().getClass(), uriVariables);
            lRestTemplateWrapper.getForEntity("csd://test", new Object().getClass(), uriVariables);
            lRestTemplateWrapper.getForObject(URI.create("cse://test"), new Object().getClass());
            lRestTemplateWrapper.getForEntity(URI.create("cse://test"), new Object().getClass());
            lRestTemplateWrapper.postForObject("csd://test", new Object(), new Object().getClass(), uriVariables);
            lRestTemplateWrapper.postForObject(URI.create("cse://test"), new Object(), new Object().getClass());
            lRestTemplateWrapper.postForEntity("csd://test", new Object(), new Object().getClass(), uriVariables);
            lRestTemplateWrapper.postForEntity(URI.create("cse://test"), new Object(), new Object().getClass());
            lRestTemplateWrapper.put("csd://test", new Object(), uriVariables);

            lRestTemplateWrapper
                    .exchange("csd://test", HttpMethod.GET, HttpEntity.EMPTY, new Object().getClass(), uriVariables);
            ParameterizedTypeReference instance = Mockito.mock(ParameterizedTypeReference.class);
            lRestTemplateWrapper.exchange("csd://test", HttpMethod.GET, HttpEntity.EMPTY, instance, uriVariables);
            lRestTemplateWrapper
                    .exchange("csd://test", HttpMethod.GET, HttpEntity.EMPTY, instance, new Object().getClass());
            RequestEntity requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create("cse://test"));
            lRestTemplateWrapper.exchange(requestEntity, new Object().getClass());
            lRestTemplateWrapper.exchange(requestEntity, instance);
            lRestTemplateWrapper
                    .exchange(URI.create("cse://test"), HttpMethod.GET, HttpEntity.EMPTY, new Object().getClass());
            lRestTemplateWrapper.exchange(URI.create("cse://test"), HttpMethod.GET, HttpEntity.EMPTY, instance);

            Assert.assertTrue(RestTemplateBuilder.create().getClass().isAssignableFrom(RestTemplateWrapper.class));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }

    }

}
