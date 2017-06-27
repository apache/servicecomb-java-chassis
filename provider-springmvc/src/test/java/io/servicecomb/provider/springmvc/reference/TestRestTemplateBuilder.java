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
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class TestRestTemplateBuilder {

    @Test
    public void testRestTemplateBuilder() {
        Assert.assertEquals(RestTemplateWrapper.class, RestTemplateBuilder.create().getClass());
    }

    class MyAcceptableRestTemplate extends AcceptableRestTemplate {

        @Override
        boolean isAcceptable(String uri) {
            return uri.startsWith("http://");
        }

        @Override
        boolean isAcceptable(URI uri) {
            return uri.getScheme().equals("http");
        }

        @Override
        public void delete(String url, Object... urlVariables) throws RestClientException {
            throw new RestClientException("test error.");
        }
    }

    @Test
    public void testRestTemplateBuilderResttemplate() {
        RestTemplateBuilder.addAcceptableRestTemplate(new MyAcceptableRestTemplate());
        RestTemplate template = RestTemplateBuilder.create();
        try {
            template.delete("http://test");
            Assert.assertFalse(true);
        } catch (RestClientException e) {
            Assert.assertEquals(e.getMessage(), "test error.");
        }

        try {
            template.delete("https://testtesttest");
            Assert.assertFalse(true);
        } catch (RestClientException e) {
            Assert.assertTrue(e.getCause() instanceof UnknownHostException);
        }
    }

}
