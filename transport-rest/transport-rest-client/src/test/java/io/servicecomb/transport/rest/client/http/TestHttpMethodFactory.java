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

package io.servicecomb.transport.rest.client.http;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.transport.rest.client.http.HttpMethodFactory;
import io.servicecomb.transport.rest.client.http.VertxHttpMethod;

public class TestHttpMethodFactory {

    @Test
    public void testHttpMethodFactory() {
        boolean status = true;
        try {
            VertxHttpMethod method = HttpMethodFactory.findHttpMethodInstance("httpMethod");
            Assert.assertNotNull(method);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertFalse(status);

    }
}
